/*******************************************************************************
 * Copyright (c) 2015, 2026 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.yaml.completions;

import java.util.Collection;
import java.util.List;

import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.FeatureFlag;
import org.springframework.ide.vscode.commons.languageserver.completion.InternalCompletionList;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

import com.google.common.collect.ImmutableList;

public class SpringYamlCompletionEngine extends YamlCompletionEngine implements LanguageSpecific {

	private final BootJavaConfig config;

	public SpringYamlCompletionEngine(YamlStructureProvider structureProvider,
			YamlAssistContextProvider contextProvider, YamlCompletionEngineOptions options, BootJavaConfig config) {
		super(structureProvider, contextProvider, options);
		this.config = config;
	}

	@Override
	public InternalCompletionList getCompletions(TextDocument doc, int offset) throws Exception {
		if (!config.isFeatureEnabled(FeatureFlag.YAML_COMPLETION)) {
			return new InternalCompletionList(List.of(), true);
		}
		return super.getCompletions(doc, offset);
	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return ImmutableList.of(LanguageId.BOOT_PROPERTIES_YAML);
	}

}
