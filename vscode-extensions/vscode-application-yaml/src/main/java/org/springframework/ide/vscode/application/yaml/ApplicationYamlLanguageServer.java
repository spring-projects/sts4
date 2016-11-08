package org.springframework.ide.vscode.application.yaml;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.completions.RelaxedNameConfig;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.yaml.completions.ApplicationYamlCompletionEngine;
import org.springframework.ide.vscode.application.yaml.completions.ApplicationYamlStructureProvider;
import org.springframework.ide.vscode.application.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.yaml.snakeyaml.Yaml;

import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.CompletionOptionsImpl;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;

public class ApplicationYamlLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	private YamlASTProvider parser = new YamlParser(yaml);
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	
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
		
		YamlCompletionEngine yamlCompletionEngine = ApplicationYamlCompletionEngine.create(
				indexProvider, 
				javaProjectFinder, 
				ApplicationYamlStructureProvider.INSTANCE, 
				typeUtilProvider, 
				RelaxedNameConfig.COMPLETION_DEFAULTS
		);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
	}

	protected IReconcileEngine getReconcileEngine() {
		return new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
	}

	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		CompletionOptionsImpl completionProvider = new CompletionOptionsImpl();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
