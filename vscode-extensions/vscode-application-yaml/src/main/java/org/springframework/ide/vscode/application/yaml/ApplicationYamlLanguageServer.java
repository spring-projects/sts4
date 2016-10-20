package org.springframework.ide.vscode.application.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.yaml.snakeyaml.Yaml;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemKind;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.CompletionItemImpl;
import io.typefox.lsapi.impl.CompletionListImpl;
import io.typefox.lsapi.impl.CompletionOptionsImpl;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;

public class ApplicationYamlLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	private YamlASTProvider parser = new YamlParser(yaml);
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	
	public ApplicationYamlLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider) {
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
				item.setKind(CompletionItemKind.Text);
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
				item.setKind(CompletionItemKind.Text);
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

	protected IReconcileEngine getReconcileEngine() {
		return new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
	}

	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		CompletionOptionsImpl completionProvider = new CompletionOptionsImpl();
		completionProvider.setResolveProvider(true);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
