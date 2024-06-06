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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.java.reconcilers.CompositeASTVisitor;
import org.springframework.ide.vscode.boot.java.semantictokens.JavaSemanticTokensProvider;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensHandler;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtSemanticTokensHandler implements SemanticTokensHandler {
	
	private final CompilationUnitCache cuCache;
	private final JavaProjectFinder projectFinder;
	private final Collection<JdtSemanticTokensProvider> tokenProviders;
	private final SemanticTokensLegend legend;
	
	private JavaSemanticTokensProvider jdtLsProvider;
	
	public JdtSemanticTokensHandler(CompilationUnitCache cuCache, JavaProjectFinder projectFinder, Collection<JdtSemanticTokensProvider> tokenProviders) {
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
		ArrayList<JdtSemanticTokensProvider> tokenProvidersList = new ArrayList<>(tokenProviders.size());
		for (JdtSemanticTokensProvider tp : tokenProviders) {
			if (tp instanceof JavaSemanticTokensProvider jdtLsProvider) {
				this.jdtLsProvider = jdtLsProvider;
			} else {
				tokenProvidersList.add(tp);
			}
		}
		this.tokenProviders = tokenProvidersList;
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
		capabilities.setFull(false);
		capabilities.setRange(true);
		capabilities.setLegend(legend);
		return capabilities;
	}
	
	
	
	@Override
	public List<SemanticTokenData> semanticTokensFull(TextDocument doc, CancelChecker cancelChecker) {
		return semanticTokens(doc, cancelChecker, null);
	}

	@Override
	public List<SemanticTokenData> semanticTokensRange(TextDocument doc, Range range, CancelChecker cancelChecker) {
		return semanticTokens(doc, cancelChecker, range);
	}
	
	private List<SemanticTokenData> semanticTokens(TextDocument doc, CancelChecker cancelChecker, Range r) {
		Optional<IJavaProject> optProject = projectFinder.find(doc.getId());
		if (optProject.isPresent()) {
			IJavaProject jp = optProject.get();
			List<JdtSemanticTokensProvider> applicableTokenProviders = tokenProviders.stream().filter(tp -> tp.isApplicable(jp)).collect(Collectors.toList());
			if (!applicableTokenProviders.isEmpty()) {
				return cuCache.withCompilationUnit(jp, URI.create(doc.getUri()), cu -> computeTokens(applicableTokenProviders, jp, cu, r));
			}
		}
		return null;
	}

	private List<SemanticTokenData> computeTokens(List<JdtSemanticTokensProvider> applicableTokenProviders, IJavaProject jp, CompilationUnit cu, Range r) {
		if (cu == null) {
			return null;
		}
		Collector<SemanticTokenData> collector = new Collector<>();
		CompositeASTVisitor visitor = new CompositeASTVisitor();
		applicableTokenProviders.forEach(tp -> visitor.add(tp.getTokensComputer(jp, cu, collector)));
		if (r != null) {
			if (r.getStart() != null) {
				visitor.setStartOffset(cu.getPosition(r.getStart().getLine(), r.getStart().getCharacter()));
			}
			if (r.getEnd() != null) {
				visitor.setEndOffset(cu.getPosition(r.getEnd().getLine(), r.getEnd().getCharacter()));
			}
		}
		cu.accept(visitor);
		if (!collector.isEmpty() && jdtLsProvider != null) {
			// If there are tokens computed then also run JDT LS tokens provider not to lose JDT LS semantic highlights
			cu.accept(jdtLsProvider.getTokensComputer(jp, cu, collector));
		}
		return collector.isEmpty() ? null : collector.get();
	}

}
