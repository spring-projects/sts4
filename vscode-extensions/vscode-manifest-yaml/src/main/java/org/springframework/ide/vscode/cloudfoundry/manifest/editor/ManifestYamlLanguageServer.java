package org.springframework.ide.vscode.cloudfoundry.manifest.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.util.Futures;
import org.springframework.ide.vscode.util.SimpleLanguageServer;
import org.springframework.ide.vscode.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.util.TextDocument;
import org.springframework.ide.vscode.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.yaml.schema.YValueHint;
import org.springframework.ide.vscode.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemKind;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.CompletionItemImpl;
import io.typefox.lsapi.impl.CompletionListImpl;
import io.typefox.lsapi.impl.CompletionOptionsImpl;
import io.typefox.lsapi.impl.DiagnosticImpl;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;

public class ManifestYamlLanguageServer extends SimpleLanguageServer {

	private static final Provider<Collection<YValueHint>> NO_BUILDPACKS = () -> ImmutableList.of();
	
	private Yaml yaml = new Yaml();
	private YamlSchema schema = new ManifestYmlSchema(NO_BUILDPACKS);
	
	public ManifestYamlLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
		
		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateDocument(documents, doc);
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
		
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
	}

	private void validateDocument(SimpleTextDocumentService documents, TextDocument doc) {
		IProblemCollector problems = new IProblemCollector() {
			
			private List<DiagnosticImpl> diagnostics = new ArrayList<>();
			
			@Override
			public void endCollecting() {
				documents.publishDiagnostics(doc, diagnostics);
			}
			
			@Override
			public void beginCollecting() {
				diagnostics.clear();
			}
			
			@Override
			public void accept(ReconcileProblem problem) {
				DiagnosticImpl d = new DiagnosticImpl();
				d.setCode(problem.getCode());
				d.setMessage(problem.getMessage());
				d.setRange(doc.toRange(problem.getOffset(), problem.getLength()));
				diagnostics.add(d);
			}
		};
		
		YamlASTProvider parser = new YamlParser(yaml);
		YamlSchemaBasedReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema);
		engine.reconcile(doc, problems);
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
