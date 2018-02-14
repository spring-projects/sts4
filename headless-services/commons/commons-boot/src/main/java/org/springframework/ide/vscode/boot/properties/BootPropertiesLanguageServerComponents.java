/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties;

import org.springframework.ide.vscode.boot.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.common.PropertyCompletionFactory;
import org.springframework.ide.vscode.boot.common.RelaxedNameConfig;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.boot.properties.completions.SpringPropertiesCompletionEngine;
import org.springframework.ide.vscode.boot.properties.hover.PropertiesHoverInfoProvider;
import org.springframework.ide.vscode.boot.properties.reconcile.SpringPropertiesReconcileEngine;
import org.springframework.ide.vscode.boot.yaml.completions.ApplicationYamlAssistContext;
import org.springframework.ide.vscode.boot.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

/**
 * Language Server for Spring Boot Application Properties files
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class BootPropertiesLanguageServerComponents implements LanguageServerComponents {

	private static final String YML = ".yml";
	private static final String PROPERTIES = ".properties";

	private static final YamlCompletionEngineOptions COMPLETION_OPTIONS = new YamlCompletionEngineOptions() {
		@Override
		public boolean includeDeindentedProposals() { return false; };
	};
	// Shared:
	private final ProjectObserver projectObserver;
	private final JavaProjectFinder javaProjectFinder;
	private final SpringPropertyIndexProvider indexProvider;
	private final TypeUtilProvider typeUtilProvider;
	private final RelaxedNameConfig relaxedNameConfig = RelaxedNameConfig.COMPLETION_DEFAULTS;

	private final PropertyCompletionFactory completionFactory;

	// For yaml
	private final Yaml yaml = new Yaml();
	private final YamlASTProvider parser = new YamlParser(yaml);
	private final YamlStructureProvider yamlStructureProvider= YamlStructureProvider.DEFAULT;
	private YamlAssistContextProvider yamlAssistContextProvider;
	private final SimpleLanguageServer server;

	public BootPropertiesLanguageServerComponents(SimpleLanguageServer server, LSFactory<BootLanguageServerParams> _params) {
		this.server = server;
		BootLanguageServerParams serverParams = _params.create(server);

		this.indexProvider = serverParams.indexProvider;
		this.typeUtilProvider = serverParams.typeUtilProvider;
		this.javaProjectFinder = serverParams.projectFinder;
		this.projectObserver = serverParams.projectObserver;

		this.completionFactory = new PropertyCompletionFactory(javaProjectFinder);
		this.yamlAssistContextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(ydoc, index, completionFactory, typeUtilProvider.getTypeUtil(doc), relaxedNameConfig);
			}
		};

	}

	@Override
	public ICompletionEngine getCompletionEngine() {
		ICompletionEngine propertiesCompletions = new SpringPropertiesCompletionEngine(indexProvider, typeUtilProvider, javaProjectFinder);
		ICompletionEngine yamlCompletions = new YamlCompletionEngine(yamlStructureProvider, yamlAssistContextProvider, COMPLETION_OPTIONS);
		return (TextDocument document, int offset) -> {
			String uri = document.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					return propertiesCompletions.getCompletions(document, offset);
				} else if (uri.endsWith(YML)) {
					return yamlCompletions.getCompletions(document, offset);
				}
			}
			return ImmutableList.of();
		};
	}

	@Override
	public HoverInfoProvider getHoverProvider() {
		HoverInfoProvider propertiesHovers = new PropertiesHoverInfoProvider(indexProvider, typeUtilProvider, javaProjectFinder);
		HoverInfoProvider ymlHovers = new YamlHoverInfoProvider(parser, yamlStructureProvider, yamlAssistContextProvider);

		return (IDocument document, int offset) -> {
			String uri = document.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					return propertiesHovers.getHoverInfo(document, offset);
				} else if (uri.endsWith(YML)) {
					return ymlHovers.getHoverInfo(document, offset);
				}
			}
			return null;
		};
	}

	@Override
	public IReconcileEngine getReconcileEngine() {
		IReconcileEngine propertiesReconciler = new SpringPropertiesReconcileEngine(indexProvider, typeUtilProvider);
		IReconcileEngine ymlReconciler = new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);

		return (doc, problemCollector) -> {
			String uri = doc.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					propertiesReconciler.reconcile(doc, problemCollector);
					return;
				} else if (uri.endsWith(YML)) {
					ymlReconciler.reconcile(doc, problemCollector);
					return;
				}
			}
			//No real reconciler is applicable. So tell the problemCollector there are no problems.
			problemCollector.beginCollecting();
			problemCollector.endCollecting();
		};
	}

	public SpringPropertyIndexProvider getPropertiesIndexProvider() {
		return indexProvider;
	}
}
