/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class BootJavaHoverProvider implements HoverHandler {

	private JavaProjectFinder projectFinder;
	private BootJavaLanguageServerComponents server;
	private AnnotationHierarchyAwareLookup<HoverProvider> hoverProviders;
	private RunningAppProvider runningAppProvider;

	public BootJavaHoverProvider(BootJavaLanguageServerComponents server, JavaProjectFinder projectFinder, AnnotationHierarchyAwareLookup<HoverProvider> specificProviders, RunningAppProvider runningAppProvider) {
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
		return server.getCompilationUnitCache().withCompilationUnit(document, cu -> {
			Collection<Range> result = new HashSet<>();
			try {
				if (cu != null) {
					cu.accept(new ASTVisitor() {

						@Override
						public boolean visit(TypeDeclaration node) {
							try {
								extractLiveHintsForType(node, document, runningBootApps, result);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							return super.visit(node);
						}

						@Override
						public boolean visit(SingleMemberAnnotation node) {
							try {
								extractLiveHintsForAnnotation(node, document, runningBootApps, result);
							} catch (Exception e) {
								Log.log(e);
							}

							return super.visit(node);
						}

						@Override
						public boolean visit(NormalAnnotation node) {
							try {
								extractLiveHintsForAnnotation(node, document, runningBootApps, result);
							} catch (Exception e) {
								Log.log(e);
							}

							return super.visit(node);
						}

						@Override
						public boolean visit(MarkerAnnotation node) {
							try {
								extractLiveHintsForAnnotation(node, document, runningBootApps, result);
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
		});
	}

	protected void extractLiveHintsForType(TypeDeclaration typeDeclaration, TextDocument doc, SpringBootApp[] runningApps, Collection<Range> result) {
		Collection<HoverProvider> providers = this.hoverProviders.getAll();
		if (!providers.isEmpty()) {
			for (HoverProvider provider : providers) {
				getProject(doc).ifPresent(project -> {
					if (hasActuatorDependency(project)) {
						Collection<Range> hints = provider.getLiveHoverHints(typeDeclaration, doc, runningApps);
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

	protected void extractLiveHintsForAnnotation(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps, Collection<Range> result) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			if (runningApps.length > 0) {
				for (HoverProvider provider : this.hoverProviders.get(type)) {
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

	private Hover provideHover(TextDocument document, int offset) throws Exception {
		IJavaProject project = getProject(document).orElse(null);
		if (project!=null) {
			return server.getCompilationUnitCache().withCompilationUnit(document, cu -> {
				ASTNode node = NodeFinder.perform(cu, offset, 0);
				if (node != null) {
					return provideHover(node, offset, document, project);
				}
				return null;
			});
		}
		return null;
	}

	private Hover provideHover(ASTNode node, int offset, TextDocument doc, IJavaProject project) {

		// look for spring annotations first
		ASTNode annotationNode = node;
		while (annotationNode != null && !(annotationNode instanceof Annotation)) {
			annotationNode = annotationNode.getParent();
		}
		if (annotationNode != null) {
			return provideHoverForAnnotation(node, (Annotation) annotationNode, offset, doc, project);
		}

		// then do additional AST node coverage
		if (node instanceof SimpleName && node.getParent() instanceof TypeDeclaration) {
			return provideHoverForTypeDeclaration(node, (TypeDeclaration) node.getParent(), offset, doc, project);
		}

		return null;
	}

	private Hover provideHoverForAnnotation(ASTNode exactNode, Annotation annotation, int offset, TextDocument doc, IJavaProject project) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			SpringBootApp[] runningApps = getRunningSpringApps(project);
			if (runningApps.length > 0) {
				for (HoverProvider provider : this.hoverProviders.get(type)) {
					Hover hover = provider.provideHover(exactNode, annotation, type, offset, doc, project, runningApps);
					if (hover!=null) {
						//TODO: compose multiple hovers somehow instead of just returning the first one?
						return hover;
					}
				}
				//Only reaching here if we didn't get a hover.
				if (!hasActuatorDependency(project)) {
					DocumentRegion region = ASTUtils.nameRegion(doc, annotation);
					if (region.containsOffset(offset)) {
						return actuatorWarning(project);
					}
				}
			}
		}
		return null;
	}

	private Hover provideHoverForTypeDeclaration(ASTNode exactNode, TypeDeclaration typeDeclaration, int offset, TextDocument doc, IJavaProject project) {
		SpringBootApp[] runningApps = getRunningSpringApps(project);
		if (runningApps.length > 0) {
			ITypeBinding type = typeDeclaration.resolveBinding();

			for (HoverProvider provider : this.hoverProviders.getAll()) {
				Hover hover = provider.provideHover(exactNode, typeDeclaration, type, offset, doc, project, runningApps);
				if (hover!=null) {
					//TODO: compose multiple hovers somehow instead of just returning the first one?
					return hover;
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
