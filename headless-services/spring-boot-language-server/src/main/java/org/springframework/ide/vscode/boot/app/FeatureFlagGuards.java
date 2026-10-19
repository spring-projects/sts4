/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.boot.java.JdtAstDocHighlightsProvider;
import org.springframework.ide.vscode.boot.java.JdtInlayHintsProvider;
import org.springframework.ide.vscode.boot.java.JdtSemanticTokensProvider;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensHandler;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.InlayHintHandler;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;
import org.springframework.ide.vscode.commons.util.Collector;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.IJavaLocationLinksProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.boot.java.codeaction.JdtAstCodeActionProvider;
import org.springframework.ide.vscode.boot.java.handlers.CodeLensProvider;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.handlers.JavaCodeActionHandler;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ICollector;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * Decorators that only delegate to the wrapped provider while the given {@link FeatureFlag} is turned on.
 * The flag is checked on every request, so changes to the setting apply right away.
 *
 * @author Martin Lippert
 */
public final class FeatureFlagGuards {

	private FeatureFlagGuards() {
	}

	public static CompletionProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, CompletionProvider delegate) {
		return new CompletionProvider() {

			@Override
			public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc, Collection<ICompletionProposal> completions) {
				if (config.isFeatureEnabled(feature)) {
					delegate.provideCompletions(node, annotation, type, offset, doc, completions);
				}
			}

			@Override
			public void provideCompletions(ASTNode node, int offset, TextDocument doc, Collection<ICompletionProposal> completions) {
				if (config.isFeatureEnabled(feature)) {
					delegate.provideCompletions(node, offset, doc, completions);
				}
			}
		};
	}

	public static AnnotationAttributeCompletionProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, AnnotationAttributeCompletionProvider delegate) {
		return (project, node) -> config.isFeatureEnabled(feature) ? delegate.getCompletionCandidates(project, node) : List.<AnnotationAttributeProposal>of();
	}

	public static CodeLensProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, CodeLensProvider delegate) {
		return (cancelToken, document, cu, resultAccumulator) -> {
			if (config.isFeatureEnabled(feature)) {
				delegate.provideCodeLenses(cancelToken, document, cu, resultAccumulator);
			}
		};
	}

	public static IJavaLocationLinksProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, IJavaLocationLinksProvider delegate) {
		return (cancelToken, project, docId, cu, node, offset) -> config.isFeatureEnabled(feature)
				? delegate.getLocationLinks(cancelToken, project, docId, cu, node, offset)
				: List.<LocationLink>of();
	}

	public static ReferenceProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, ReferenceProvider delegate) {
		return new ReferenceProvider() {

			@Override
			public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, ASTNode node, Annotation annotation, ITypeBinding type, int offset) {
				return config.isFeatureEnabled(feature) ? delegate.provideReferences(cancelToken, project, node, annotation, type, offset) : List.of();
			}

			@Override
			public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, TextDocument doc, ASTNode node, int offset) {
				return config.isFeatureEnabled(feature) ? delegate.provideReferences(cancelToken, project, doc, node, offset) : List.of();
			}
		};
	}

	public static JavaCodeActionHandler onlyWhen(BootJavaConfig config, FeatureFlag feature, JavaCodeActionHandler delegate) {
		return (project, cancelToken, capabilities, context, doc, region) -> config.isFeatureEnabled(feature)
				? delegate.handle(project, cancelToken, capabilities, context, doc, region)
				: List.<Either<Command, CodeAction>>of();
	}

	public static JdtAstCodeActionProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, JdtAstCodeActionProvider delegate) {
		return new JdtAstCodeActionProvider() {

			@Override
			public boolean isApplicable(IJavaProject project) {
				return config.isFeatureEnabled(feature) && delegate.isApplicable(project);
			}

			@Override
			public Optional<ASTVisitor> createVisitor(CancelChecker cancelToken, IJavaProject project, URI docURI, CompilationUnit cu, TextDocument doc,
					IRegion region, ICollector<CodeAction> collector) {
				return delegate.createVisitor(cancelToken, project, docURI, cu, doc, region, collector);
			}
		};
	}

	public static HighlightProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, HighlightProvider delegate) {
		return (cancelToken, document, position, resultAccumulator) -> {
			if (config.isFeatureEnabled(feature)) {
				delegate.provideHighlights(cancelToken, document, position, resultAccumulator);
			}
		};
	}

	public static JdtAstDocHighlightsProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, JdtAstDocHighlightsProvider delegate) {
		return (project, doc, cu, node, offset) -> config.isFeatureEnabled(feature)
				? delegate.getDocHighlights(project, doc, cu, node, offset)
				: List.<DocumentHighlight>of();
	}

	public static InlayHintHandler onlyWhen(BootJavaConfig config, FeatureFlag feature, InlayHintHandler delegate) {
		return (doc, range, cancelChecker) -> config.isFeatureEnabled(feature)
				? delegate.handle(doc, range, cancelChecker)
				: List.<InlayHint>of();
	}

	public static JdtInlayHintsProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, JdtInlayHintsProvider delegate) {
		return new JdtInlayHintsProvider() {

			@Override
			public boolean isApplicable(IJavaProject project) {
				return config.isFeatureEnabled(feature) && delegate.isApplicable(project);
			}

			@Override
			public ASTVisitor getInlayHintsComputer(IJavaProject project, TextDocument doc, CompilationUnit cu, Collector<InlayHint> collector) {
				return delegate.getInlayHintsComputer(project, doc, cu, collector);
			}
		};
	}

	public static JdtSemanticTokensProvider onlyWhen(BootJavaConfig config, FeatureFlag feature, JdtSemanticTokensProvider delegate) {
		return new JdtSemanticTokensProvider() {

			@Override
			public List<String> getTokenTypes() {
				return delegate.getTokenTypes();
			}

			@Override
			public List<String> getTokenModifiers() {
				return delegate.getTokenModifiers();
			}

			@Override
			public boolean isApplicable(IJavaProject project) {
				return config.isFeatureEnabled(feature) && delegate.isApplicable(project);
			}

			@Override
			public ASTVisitor getTokensComputer(IJavaProject project, TextDocument doc, CompilationUnit cu, Collector<SemanticTokenData> collector) {
				return delegate.getTokensComputer(project, doc, cu, collector);
			}
		};
	}

	public static SemanticTokensHandler onlyWhen(BootJavaConfig config, FeatureFlag feature, SemanticTokensHandler delegate) {
		return new SemanticTokensHandler() {

			@Override
			public SemanticTokensWithRegistrationOptions getCapability() {
				return delegate.getCapability();
			}

			@Override
			public List<SemanticTokenData> semanticTokensFull(TextDocument doc, CancelChecker cancelChecker) {
				return config.isFeatureEnabled(feature) ? delegate.semanticTokensFull(doc, cancelChecker) : null;
			}

			@Override
			public List<SemanticTokenData> semanticTokensRange(TextDocument doc, Range range, CancelChecker cancelChecker) {
				return config.isFeatureEnabled(feature) ? delegate.semanticTokensRange(doc, range, cancelChecker) : null;
			}
		};
	}

	public static DocumentSymbolHandler onlyWhen(BootJavaConfig config, FeatureFlag feature, DocumentSymbolHandler delegate) {
		return params -> config.isFeatureEnabled(feature) ? delegate.handle(params) : List.<DocumentSymbol>of();
	}

	public static WorkspaceSymbolHandler onlyWhen(BootJavaConfig config, FeatureFlag feature, WorkspaceSymbolHandler delegate) {
		return params -> config.isFeatureEnabled(feature) ? delegate.handle(params) : List.<WorkspaceSymbol>of();
	}

}
