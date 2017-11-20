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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

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
				Hover hoverResult = provideHover(doc, offset);
				if (hoverResult != null) {
					return CompletableFuture.completedFuture(hoverResult);
				}
			}
			catch (Exception e) {
			}
		}

		return SimpleTextDocumentService.NO_HOVER;
	}

	public Range[] getLiveHoverHints(final TextDocument document, final SpringBootApp[] runningBootApps) {
		List<Range> result = new ArrayList<>();

		try {
			CompilationUnit cu = server.getCompilationUnitCache().getCompilationUnit(document);
			if (cu != null) {
				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(SingleMemberAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						} catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}

					@Override
					public boolean visit(NormalAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						} catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}

					@Override
					public boolean visit(MarkerAnnotation node) {
						try {
							extractLiveHints(node, document, runningBootApps, result);
						} catch (Exception e) {
							Log.log(e);
						}

						return super.visit(node);
					}
				});
			}
		} catch (Exception e) {
			Log.log(e);
		}

		return result.toArray(new Range[result.size()]);
	}

	protected void extractLiveHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps, List<Range> result) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			String qualifiedName = type.getQualifiedName();
			if (qualifiedName != null) {
				HoverProvider provider = this.hoverProviders.get(qualifiedName);
				if (provider != null) {
					if (runningApps.length>0) {
						getProject(doc).ifPresent(project -> {
							if (hasActuatorDependency(project)) {
								Collection<Range> hints = provider.getLiveHoverHints(annotation, doc, runningApps);
								if (hints!=null) {
									result.addAll(hints);
								}
							} else {
								//Do nothing... we don't want a highlight for the 'no actuator warning'
								//ASTUtils.nameRange(doc, annotation).ifPresent(result::add);
							}
						});
					}
				}
			}
		}
	}

	private Hover provideHover(TextDocument document, int offset) throws Exception {
		IJavaProject project = getProject(document).orElse(null);
		if (project!=null) {
			CompilationUnit cu = server.getCompilationUnitCache().getCompilationUnit(document);
			ASTNode node = NodeFinder.perform(cu, offset, 0);
			if (node != null) {
				return provideHoverForAnnotation(node, offset, document, project);
			}
		}
		return null;
	}

	private Hover provideHoverForAnnotation(ASTNode node, int offset, TextDocument doc, IJavaProject project) {
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
						if (runningApps.length>0) {
							if (hasActuatorDependency(project)) {
								return provider.provideHover(node, annotation, type, offset, doc, project, runningApps);
							} else {
								DocumentRegion region = ASTUtils.nameRegion(doc, annotation);
								if (region.containsOffset(offset)) {
									return actuatorWarning(project);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private Hover actuatorWarning(IJavaProject project) {
		String hoverText =
				"**No live hover information available**.\n"+
				"\n" +
				"Live hover providers use various `spring-boot-actuator` endpoints to retrieve information. "+
				"Consider adding `spring-boot-actuator` as a dependency to your project `"+project.getElementName()+"`";
		return new Hover(ImmutableList.of(Either.forLeft(hoverText)));
	}

	private boolean hasActuatorDependency(IJavaProject project) {
		try {
			IClasspath classpath = project.getClasspath();
			if (classpath!=null) {
				return classpath.getClasspathEntries().stream().anyMatch(cpe -> {
					String name = cpe.getFileName().toString();
					return name.startsWith("spring-boot-actuator-");
				});
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}

	private Optional<IJavaProject> getProject(IDocument doc) {
		return this.projectFinder.find(new TextDocumentIdentifier(doc.getUri()));
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
