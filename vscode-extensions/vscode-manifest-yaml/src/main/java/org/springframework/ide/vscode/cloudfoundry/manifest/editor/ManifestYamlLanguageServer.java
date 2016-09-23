package org.springframework.ide.vscode.cloudfoundry.manifest.editor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.reconcile.IDocument;
import org.springframework.ide.vscode.commons.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.util.Futures;
import org.springframework.ide.vscode.util.SimpleLanguageServer;
import org.springframework.ide.vscode.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.util.TextDocument;
import org.springframework.ide.vscode.yaml.ErrorCodes;
import org.springframework.ide.vscode.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.yaml.reconcile.SchemaBasedYamlASTReconciler;
import org.springframework.ide.vscode.yaml.reconcile.YamlASTReconciler;
import org.springframework.ide.vscode.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.yaml.schema.YValueHint;
import org.springframework.ide.vscode.yaml.schema.YamlSchema;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemImpl;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.CompletionListImpl;
import io.typefox.lsapi.CompletionOptionsImpl;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticImpl;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.RangeImpl;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.ServerCapabilitiesImpl;

public class ManifestYamlLanguageServer extends SimpleLanguageServer {

	private static final Provider<Collection<YValueHint>> NO_BUILDPACKS = () -> ImmutableList.of();
	
	private Yaml yaml = new Yaml();
	
	public ManifestYamlLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();

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
		
		documents.onCompletion(params -> {
			CompletableFuture<CompletionList> promise = new CompletableFuture<>();
			CompletionListImpl completions = new CompletionListImpl();
			completions.setIncomplete(false);
			List<CompletionItemImpl> items = new ArrayList<>();
			{
//		        {
//		            label: 'TypeScript',
//		            kind: CompletionItemKind.Text,
//		            data: 1
//		        },
				CompletionItemImpl item = new CompletionItemImpl();
				item.setLabel("TypeScript");
				item.setKind(CompletionItem.KIND_TEXT);
				item.setData(1);
				items.add(item);
			}

			{
//				{
//		            label: 'JavaScript',
//		            kind: CompletionItemKind.Text,
//		            data: 2
//		        }				
				CompletionItemImpl item = new CompletionItemImpl();
				item.setLabel("JavaScript");
				item.setKind(CompletionItem.KIND_TEXT);
				item.setData(2);
				items.add(item);
			}
			completions.setItems(items);

			promise.complete(completions);
			return promise;
		});
		
		documents.onCompletionResolve((_item) -> {
			CompletionItemImpl item = (CompletionItemImpl) _item;
			Object data = item.getData();
			if (Integer.valueOf(1).equals(data)) {
				item.setDetail("TypeScript details");
				item.setDocumentation("TypeScript docs");
			} else {
				item.setDetail("JavaScript details");
				item.setDocumentation("JavaScript docs");
			}
			return Futures.of((CompletionItem)item);
		});
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
		
		YamlSchema schema = new ManifestYmlSchema(NO_BUILDPACKS);
		YamlASTProvider parser = new YamlParser(yaml);
		YamlSchemaBasedReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema);
		engine.reconcile(doc, problems);
	}
	
	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		
		c.setTextDocumentSync(ServerCapabilities.SYNC_FULL);
		
		CompletionOptionsImpl completionProvider = new CompletionOptionsImpl();
		completionProvider.setResolveProvider(true);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
