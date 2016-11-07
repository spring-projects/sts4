/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.yaml.completions;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.completions.PropertyCompletionFactory;
import org.springframework.ide.vscode.application.properties.metadata.completions.RelaxedNameConfig;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class ApplicationYamlCompletionEngine {
	public static YamlCompletionEngine create(
			final SpringPropertyIndexProvider indexProvider,
			final JavaProjectFinder documentContextFinder,
			final YamlStructureProvider structureProvider,
			final TypeUtilProvider typeUtilProvider,
			final RelaxedNameConfig conf
	) {
		final PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(documentContextFinder);
		YamlAssistContextProvider contextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(index, completionFactory, typeUtilProvider.getTypeUtil(doc), conf);
			}
		};
		return new YamlCompletionEngine(structureProvider, contextProvider);
	}
}
