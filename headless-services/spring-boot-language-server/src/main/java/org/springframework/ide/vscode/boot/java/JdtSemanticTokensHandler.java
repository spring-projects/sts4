/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensHandler;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensUtils;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

public class JdtSemanticTokensHandler implements SemanticTokensHandler {
	
	private final CompilationUnitCache cuCache;
	private final JavaProjectFinder projectFinder;
	private final Collection<JdtSemanticTokensProvider> tokenProviders;
	private final SemanticTokensLegend legend;
	
	public JdtSemanticTokensHandler(CompilationUnitCache cuCache, JavaProjectFinder projectFinder, Collection<JdtSemanticTokensProvider> tokenProviders) {
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
		this.tokenProviders = tokenProviders;
		this.legend = new SemanticTokensLegend(
				tokenProviders.stream().flatMap(tp -> tp.getTokenTypes().stream()).distinct().collect(Collectors.toList()),
				tokenProviders.stream().flatMap(tp -> tp.getTokenModifiers().stream()).distinct().collect(Collectors.toList())
		);
	}

	@Override
	public SemanticTokensWithRegistrationOptions getCapability() {
		SemanticTokensWithRegistrationOptions capabilities = new SemanticTokensWithRegistrationOptions();
		DocumentFilter documentFilter = new DocumentFilter();
		documentFilter.setLanguage(LanguageId.JAVA.getId());
		capabilities.setDocumentSelector(List.of(documentFilter));
		capabilities.setFull(true);
		capabilities.setLegend(legend);
		return capabilities;
	}

	@Override
	public SemanticTokens semanticTokensFull(SemanticTokensParams params, CancelChecker cancelChecker) {
		Optional<IJavaProject> optProject = projectFinder.find(params.getTextDocument());
		if (optProject.isPresent()) {
			IJavaProject jp = optProject.get();
			List<JdtSemanticTokensProvider> applicableTokenProviders = tokenProviders.stream().filter(tp -> tp.isApplicable(jp)).collect(Collectors.toList());
			if (!applicableTokenProviders.isEmpty()) {
				return cuCache.withCompilationUnit(jp, URI.create(params.getTextDocument().getUri()), cu -> computeTokens(applicableTokenProviders, jp, cu));
			}
		}
		return new SemanticTokens();
	}

	private SemanticTokens computeTokens(List<JdtSemanticTokensProvider> applicableTokenProviders, IJavaProject jp, CompilationUnit cu) {
		List<SemanticTokenData> tokensData = applicableTokenProviders.stream().map(tp -> tp.computeTokens(jp, cu)).flatMap(t -> t.stream()).collect(Collectors.toList());
		return new SemanticTokens(SemanticTokensUtils.mapTokensDataToLsp(tokensData, legend, offset -> cu.getLineNumber(offset) - 1, cu::getColumnNumber));
	}

}
