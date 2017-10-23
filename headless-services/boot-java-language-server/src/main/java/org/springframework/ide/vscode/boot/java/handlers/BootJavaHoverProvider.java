/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaHoverProvider implements HoverHandler {

	private JavaProjectFinder projectFinder;
	private BootJavaLanguageServer server;
	private Map<String, HoverProvider> hoverProviders;
	private RunningAppProvider runningAppProvider;

	public BootJavaHoverProvider(BootJavaLanguageServer server, JavaProjectFinder projectFinder, Map<String, HoverProvider> specificProviders, RunningAppProvider runningAppProvider) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.hoverProviders = specificProviders;
		this.runningAppProvider = runningAppProvider;
	}

	@Override
	public CompletableFuture<Hover> handle(TextDocumentPositionParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		if (documents.get(params) != null) {
			TextDocument doc = documents.get(params).copy();
			try {
				int offset = doc.toOffset(params.getPosition());
				CompletableFuture<Hover> hoverResult = provideHover(doc, offset);
				if (hoverResult != null) {
					return hoverResult;
				}
			}
			catch (Exception e) {
			}
		}

		return SimpleTextDocumentService.NO_HOVER;
	}

	public Range[] getLiveHoverHints(final TextDocument document, final SpringBootApp[] runningBootApps) {
		List<Range> result = new ArrayList<>();

		getProject(document).ifPresent(project -> {
			try {
				CompilationUnit cu = parse(document, project);

				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(SingleMemberAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						}
						catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}

					@Override
					public boolean visit(NormalAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						}
						catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}

					@Override
					public boolean visit(MarkerAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						}
						catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}
				});
			} catch (Exception e) {
				Log.log(e);
			}
		});

		return result.toArray(new Range[result.size()]);
	}

	protected void extractLiveHints(Annotation annotation, TextDocument document, SpringBootApp[] runningApps, List<Range> result) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			String qualifiedName = type.getQualifiedName();
			if (qualifiedName != null) {
				HoverProvider provider = this.hoverProviders.get(qualifiedName);
				if (provider != null) {
					Range range = provider.getLiveHoverHint(annotation, document, runningApps);
					if (range != null) {
						result.add(range);
					}
				}
			}
		}
	}

	private CompletableFuture<Hover> provideHover(TextDocument document, int offset) throws Exception {
		IJavaProject project = getProject(document).orElse(null);
		if (project!=null) {
			CompilationUnit cu = parse(document, project);
			ASTNode node = NodeFinder.perform(cu, offset, 0);
			if (node != null) {
				return provideHoverForAnnotation(node, offset, document, project);
			}
		}
		return null;
	}

	private CompilationUnit parse(TextDocument document, IJavaProject project)
			throws Exception, BadLocationException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] classpathEntries = getClasspathEntries(project);
		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(document.get(0, document.getLength()).toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu;
	}

	private CompletableFuture<Hover> provideHoverForAnnotation(ASTNode node, int offset, TextDocument doc, IJavaProject project) {
		Annotation annotation = null;

		while (node != null && !(node instanceof Annotation)) {
			node = node.getParent();
		}

		if (node != null) {
			annotation = (Annotation) node;
			ITypeBinding type = annotation.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null) {
					HoverProvider provider = this.hoverProviders.get(qualifiedName);
					if (provider != null) {
						SpringBootApp[] runningApps = getRunningSpringApps(project);
						return provider.provideHover(node, annotation, type, offset, doc, runningApps);
					}
				}
			}
		}

		return null;
	}

	private Optional<IJavaProject> getProject(IDocument doc) {
		return this.projectFinder.find(new TextDocumentIdentifier(doc.getUri()));
	}

	private String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

	private SpringBootApp[] getRunningSpringApps(IJavaProject project) {
		try {
			return runningAppProvider.getAllRunningSpringApps().toArray(new SpringBootApp[0]);
		} catch (Exception e) {
			Log.log(e);
			return new SpringBootApp[0];
		}
	}

}
