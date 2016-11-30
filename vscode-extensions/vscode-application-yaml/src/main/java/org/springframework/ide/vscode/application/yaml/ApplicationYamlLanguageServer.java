package org.springframework.ide.vscode.application.yaml;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.completions.PropertyCompletionFactory;
import org.springframework.ide.vscode.application.properties.metadata.completions.RelaxedNameConfig;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.application.yaml.completions.ApplicationYamlAssistContext;
import org.springframework.ide.vscode.application.yaml.completions.ApplicationYamlStructureProvider;
import org.springframework.ide.vscode.application.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

public class ApplicationYamlLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	private YamlASTProvider parser = new YamlParser(yaml);
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private VscodeCompletionEngineAdapter completionEngine;
	private VscodeHoverEngineAdapter hoverEngine;

	
	public ApplicationYamlLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder javaProjectFinder) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		SimpleTextDocumentService documents = getTextDocumentService();
//		SimpleWorkspaceService workspace = getWorkspaceService();
		IReconcileEngine reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});
		
//		workspace.onDidChangeConfiguraton(settings -> {
//			System.out.println("Config changed: "+params);
//			Integer val = settings.getInt("languageServerExample", "maxNumberOfProblems");
//			if (val!=null) {
//				maxProblems = ((Number) val).intValue();
//				for (TextDocument doc : documents.getAll()) {
//					validateDocument(documents, doc);
//				}
//			}
//		});
		
		YamlStructureProvider structureProvider = ApplicationYamlStructureProvider.INSTANCE;
		RelaxedNameConfig relaxedNameConfig = RelaxedNameConfig.COMPLETION_DEFAULTS;

		final PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(javaProjectFinder);
		YamlAssistContextProvider contextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(index, completionFactory, typeUtilProvider.getTypeUtil(doc), relaxedNameConfig);
			}
		};
		YamlCompletionEngine yamlCompletionEngine =  new YamlCompletionEngine(structureProvider, contextProvider);
		
		completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
	
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(parser, structureProvider, contextProvider);
		hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		documents.onHover(hoverEngine::getHover);	
	}
	
	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletionsNumber(number);
	}

	protected IReconcileEngine getReconcileEngine() {
		return new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
	}

	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
