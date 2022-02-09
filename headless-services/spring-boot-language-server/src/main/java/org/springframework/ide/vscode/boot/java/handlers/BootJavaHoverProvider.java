/*******************************************************************************
 * Copyright (c) 2017, 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.beans.Statement;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class BootJavaHoverProvider implements HoverHandler {

	private static Logger logger = LoggerFactory.getLogger(BootJavaHoverProvider.class);

	private final JavaProjectFinder projectFinder;
	private final BootJavaLanguageServerComponents server;
	private final AnnotationHierarchyAwareLookup<HoverProvider> hoverProviders;
	private final SpringProcessLiveDataProvider liveDataProvider;

	public BootJavaHoverProvider(BootJavaLanguageServerComponents server, JavaProjectFinder projectFinder,
			AnnotationHierarchyAwareLookup<HoverProvider> specificProviders, SpringProcessLiveDataProvider liveDataProvider) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.hoverProviders = specificProviders;
		this.liveDataProvider = liveDataProvider;
	}
	
	@Override
	public Hover handle(CancelChecker cancelToken, HoverParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.getLatestSnapshot(params);
		
		if (doc != null) {
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (server.getInterestingLanguages().contains(doc.getLanguageId())) {
				try {
					int offset = doc.toOffset(params.getPosition());
					Hover hoverResult = provideHover(cancelToken, doc, offset);
					if (hoverResult != null) {
						return hoverResult;
					}
				}
				catch (Exception e) {
				}
			}
		}

		return SimpleTextDocumentService.NO_HOVER;
	}

	public CodeLens[] getLiveHoverHints(final TextDocument document, final IJavaProject project) {
		final SpringProcessLiveData[] processLiveData = this.liveDataProvider.getLatestLiveData();
		
		if (processLiveData.length == 0) return new CodeLens[0];
		if (project == null) return new CodeLens[0];

		return server.getCompilationUnitCache().withCompilationUnit(project, URI.create(document.getUri()), cu -> {
			Collection<CodeLens> result = new LinkedHashSet<>();
			try {
				if (cu != null) {
					new JavaIsoVisitor<Collection<CodeLens>>() {
						
						public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, Collection<CodeLens> p) {
							try {
								extractLiveHintsForType(classDecl, document, project, processLiveData, result);
							}
							catch (Exception e) {
								logger.error("error extracting live hint information for docURI '" + document.getUri() + "' - on node: " + classDecl.printTrimmed(), e);
							}
							return super.visitClassDeclaration(classDecl, p);
						};
						
						public Annotation visitAnnotation(Annotation annotation, Collection<CodeLens> p) {
							try {
								extractLiveHintsForAnnotation(annotation, document, project, processLiveData, result);
							} catch (Exception e) {
								logger.error("error extracting live hint information for docURI '" + document.getUri() + "' - on node: " + annotation.printTrimmed(), e);
							}
							return super.visitAnnotation(annotation, p);
						};
						
						public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, Collection<CodeLens> p) {
							try {
								extractLiveHintsForMethod(method, document, project, processLiveData, result);
							} catch (Exception e) {
								logger.error("error extracting live hint information for docURI '" + document.getUri() + "' - on node: " + method.printTrimmed(), e);
							}
							return super.visitMethodDeclaration(method, p);
						};
						
					}.visitNonNull(cu, result);
				}
			} catch (Exception e) {
				logger.error("error extracting live hint information for docURI '" + document.getUri(), e);
			}
			return result.toArray(new CodeLens[result.size()]);
		});
	}

	protected void extractLiveHintsForMethod(MethodDeclaration methodDeclaration, TextDocument doc, IJavaProject project,
			SpringProcessLiveData[] processLiveData, Collection<CodeLens> result) {
		Collection<HoverProvider> providers = this.hoverProviders.getAll();
		for (HoverProvider provider : providers) {
			Collection<CodeLens> hints = provider.getLiveHintCodeLenses(project, methodDeclaration, doc, processLiveData);
			if (hints!=null) {
				result.addAll(hints);
			}
		}
	}

	protected void extractLiveHintsForType(ClassDeclaration typeDeclaration, TextDocument doc, IJavaProject project,
			SpringProcessLiveData[] processLiveData, Collection<CodeLens> result) {
		Collection<HoverProvider> providers = this.hoverProviders.getAll();
		for (HoverProvider provider : providers) {
			Collection<CodeLens> hints = provider.getLiveHintCodeLenses(project, typeDeclaration, doc, processLiveData);
			if (hints!=null) {
				result.addAll(hints);
			}
		}
	}

	protected void extractLiveHintsForAnnotation(Annotation annotation, TextDocument doc, IJavaProject project,
			SpringProcessLiveData[] processLiveData, Collection<CodeLens> result) {
		FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
		if (type != null) {
			for (HoverProvider provider : this.hoverProviders.get(type)) {
				Collection<CodeLens> hints = provider.getLiveHintCodeLenses(project, annotation, doc, processLiveData);
				if (hints!=null) {
					result.addAll(hints);
				}
			}
		}
	}

	private Hover provideHover(CancelChecker cancelToken, TextDocument document, int offset) throws Exception {
		final SpringProcessLiveData[] processLiveData = this.liveDataProvider.getLatestLiveData();

		cancelToken.checkCanceled();

		IJavaProject project = getProject(document).orElse(null);
		if (project != null) {
			return server.getCompilationUnitCache().withCompilationUnit(project, URI.create(document.getUri()), cu -> {
				
				cancelToken.checkCanceled();
				
				J node = ORAstUtils.findAstNodeAt(cu, offset);
				if (node != null) {
					return provideHover(cancelToken, node, offset, document, project, processLiveData);
				}
				return null;
			});
		}
		return null;
	}

	private Hover provideHover(CancelChecker cancelToken, J node, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {

		// look for spring annotations first
		Annotation annotationNode = ORAstUtils.findNode(node, Annotation.class);
		if (annotationNode != null) {
			return provideHoverForAnnotation(cancelToken, node, annotationNode, offset, doc, project, processLiveData);
		}

		// then do additional AST node coverage
		if (node instanceof Statement) {
			J parent = ORAstUtils.getParent(node);
			if (parent instanceof ClassDeclaration) {
				return provideHoverForTypeDeclaration(cancelToken, node, (ClassDeclaration) parent, offset, doc, project, processLiveData);
			} else if (parent instanceof MethodDeclaration) {
				return provideHoverForMethodDeclaration(cancelToken, (MethodDeclaration) parent, offset, doc, project, processLiveData);
			} else if (parent instanceof VariableDeclarations && ORAstUtils.getParent(parent) instanceof MethodDeclaration) {
				return provideHoverForMethodParameter(cancelToken, (VariableDeclarations) parent, offset, doc, project, processLiveData);
			}
		}
		return null;
	}

	private Hover provideHoverForMethodParameter(CancelChecker cancelToken, VariableDeclarations parameter, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {
			for (HoverProvider provider : this.hoverProviders.getAll()) {
				
				cancelToken.checkCanceled();
				
				Hover hover = provider.provideMethodParameterHover(parameter, offset, doc, project, processLiveData);
				if (hover != null) {
					return hover;
				}
			}
		}
		return null;
	}

	private Hover provideHoverForMethodDeclaration(CancelChecker cancelToken, MethodDeclaration methodDeclaration, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {
			for (HoverProvider provider : this.hoverProviders.getAll()) {
				
				cancelToken.checkCanceled();
				
				Hover hover = provider.provideHover(methodDeclaration, offset, doc, project, processLiveData);
				if (hover != null) {
					//TODO: compose multiple hovers somehow instead of just returning the first one?
					return hover;
				}
			}
		}
		return null;
	}

	private Hover provideHoverForAnnotation(CancelChecker cancelToken, J exactNode, Annotation annotation, int offset, TextDocument doc, IJavaProject project,
			SpringProcessLiveData[] processLiveData) {
		FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
		if (type != null) {
			logger.debug("Hover requested for "+type.getClassName());

			if (processLiveData.length > 0) {

				for (HoverProvider provider : this.hoverProviders.get(type)) {
					
					cancelToken.checkCanceled();
					
					Hover hover = provider.provideHover(exactNode, annotation, offset, doc, project, processLiveData);
					if (hover != null) {
						logger.debug("Hover found: "+hover);
						//TODO: compose multiple hovers somehow instead of just returning the first one?
						return hover;
					}
					logger.debug("NO Hover!");
				}

				//Only reaching here if we didn't get a hover.
				if (!SpringProjectUtil.hasBootActuators(project)) {
					DocumentRegion region = ORAstUtils.nameRegion(doc, annotation);
					if (region.containsOffset(offset)) {
						return liveHoverWarning(project);
					}
				}
			}
		}
		return null;
	}

	private Hover provideHoverForTypeDeclaration(CancelChecker cancelToken, J exactNode, ClassDeclaration typeDeclaration, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {

			for (HoverProvider provider : this.hoverProviders.getAll()) {
				
				cancelToken.checkCanceled();
				
				Hover hover = provider.provideHover(exactNode, typeDeclaration, offset, doc, project, processLiveData);
				if (hover!=null) {
					//TODO: compose multiple hovers somehow instead of just returning the first one?
					return hover;
				}
			}
		}
		return null;
	}

	private Hover liveHoverWarning(IJavaProject project) {
		String hoverText =
				"**No live hover information available**.\n"+
				"\n" +
				"Live hover providers use either `spring-boot-actuator` endpoints to retrieve information or the Spring live beans option. "+
				"Consider adding `spring-boot-actuator` as a dependency to your project `"+project.getElementName()+"` or enable the live beans option in your launch configuration.";
		return new Hover(ImmutableList.of(Either.forLeft(hoverText)));
	}

	private Optional<IJavaProject> getProject(IDocument doc) {
		return this.projectFinder.find(new TextDocumentIdentifier(doc.getUri()));
	}

}
