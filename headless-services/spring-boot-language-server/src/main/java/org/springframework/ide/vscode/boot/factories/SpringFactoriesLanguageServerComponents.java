/*******************************************************************************
 * Copyright (c) 2022, 2025 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.factories;

import java.util.Optional;
import java.util.Set;

import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.FeatureFlag;
import org.springframework.ide.vscode.boot.app.FeatureFlagGuards;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

public class SpringFactoriesLanguageServerComponents implements LanguageServerComponents {
	
	private SpringFactoriesReconcileEngine reconciler;
	private SpringSymbolIndex springIndex;
	private final BootJavaConfig config;

	public SpringFactoriesLanguageServerComponents(SpringFactoriesReconcileEngine reconciler, JavaProjectFinder projectFinder, SpringSymbolIndex springIndex, BootJavaConfig config) {
		this.springIndex = springIndex;
		this.config = config;
		this.reconciler = reconciler;
	}

	@Override
	public Set<LanguageId> getInterestingLanguages() {
		return Set.of(LanguageId.SPRING_FACTORIES);
	}

	@Override
	public HoverHandler getHoverProvider() {
		return null;
	}

	@Override
	public Optional<IReconcileEngine> getReconcileEngine() {
		return Optional.of(reconciler);
	}

	@Override
	public Optional<DocumentSymbolHandler> getDocumentSymbolProvider() {
		DocumentSymbolHandler documentSymbols = params -> springIndex.getDocumentSymbols(params.getTextDocument().getUri());
		return Optional.of(FeatureFlagGuards.onlyWhen(config, FeatureFlag.DOCUMENT_SYMBOLS, documentSymbols));
	}

}
