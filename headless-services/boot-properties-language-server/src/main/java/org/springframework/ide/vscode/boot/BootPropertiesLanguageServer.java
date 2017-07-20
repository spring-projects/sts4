/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot;

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
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter.HoverType;
import org.springframework.ide.vscode.commons.languageserver.java.DefaultJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.maven.JavaProjectWithClasspathFileFinderStrategy;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenProjectFinderStrategy;
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
 *
 */
public class BootPropertiesLanguageServer extends SimpleLanguageServer {

	public static final JavaProjectFinder DEFAULT_PROJECT_FINDER = new DefaultJavaProjectFinder(new IJavaProjectFinderStrategy[] {
			new MavenProjectFinderStrategy(MavenCore.getDefault()),
			new GradleProjectFinderStrategy(GradleCore.getDefault()),
			new JavaProjectWithClasspathFileFinderStrategy()
	});

	private static final String YML = ".yml";
	private static final String PROPERTIES = ".properties";

	private static final YamlCompletionEngineOptions COMPLETION_OPTIONS = new YamlCompletionEngineOptions() {
		public boolean includeDeindentedProposals() { return false; };
	};
	// Shared:
	private final JavaProjectFinder javaProjectFinder;
	private final SpringPropertyIndexProvider indexProvider;
	private final TypeUtilProvider typeUtilProvider;
	private final VscodeCompletionEngineAdapter completionEngine;
	private final VscodeHoverEngineAdapter hoverEngine;
	private final RelaxedNameConfig relaxedNameConfig = RelaxedNameConfig.COMPLETION_DEFAULTS;

	private final PropertyCompletionFactory completionFactory;

	// For yaml
	private final Yaml yaml = new Yaml();
	private final YamlASTProvider parser = new YamlParser(yaml);
	private final YamlStructureProvider yamlStructureProvider= YamlStructureProvider.DEFAULT;
	private YamlAssistContextProvider yamlAssistContextProvider;

	public BootPropertiesLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder javaProjectFinder) {
		super("vscode-boot-properties");
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.javaProjectFinder = javaProjectFinder;
		this.completionFactory = new PropertyCompletionFactory(javaProjectFinder);
		this.yamlAssistContextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(ydoc, index, completionFactory, typeUtilProvider.getTypeUtil(doc), relaxedNameConfig);
			}
		};

		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc.getId(), reconcileEngine);
		});

		ICompletionEngine propertiesCompletionEngine = getCompletionEngine();
		completionEngine = createCompletionEngineAdapter(this, propertiesCompletionEngine);
		completionEngine.setMaxCompletions(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);

		HoverInfoProvider hoverInfoProvider = getHoverProvider();
		hoverEngine = new VscodeHoverEngineAdapter(this, hoverInfoProvider);
		documents.onHover(hoverEngine::getHover);
	}

	private ICompletionEngine getCompletionEngine() {
		ICompletionEngine propertiesCompletions = new SpringPropertiesCompletionEngine(indexProvider, typeUtilProvider, javaProjectFinder);
		ICompletionEngine yamlCompletions = new YamlCompletionEngine(yamlStructureProvider, yamlAssistContextProvider, COMPLETION_OPTIONS);
		return (IDocument document, int offset) -> {
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

	protected HoverInfoProvider getHoverProvider() {
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

	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletions(number);
	}

	public void setHoverType(HoverType type) {
		hoverEngine.setHoverType(type);
	}

	protected IReconcileEngine getReconcileEngine() {
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


}
