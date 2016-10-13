package org.springframework.ide.vscode.yaml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.vscode.util.Futures;
import org.springframework.ide.vscode.util.SimpleLanguageServer;
import org.springframework.ide.vscode.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.util.TextDocument;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemKind;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticSeverity;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.CompletionItemImpl;
import io.typefox.lsapi.impl.CompletionListImpl;
import io.typefox.lsapi.impl.CompletionOptionsImpl;
import io.typefox.lsapi.impl.DiagnosticImpl;
import io.typefox.lsapi.impl.PositionImpl;
import io.typefox.lsapi.impl.RangeImpl;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;

public class YamlLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	
	public YamlLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			System.out.println("Document changed: "+params);
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

	private void validateDocument(SimpleTextDocumentService documents, TextDocument doc) {
		List<DiagnosticImpl> diagnostics = reconcile(documents, doc);
		documents.publishDiagnostics(doc, diagnostics);
	}
	
	protected List<DiagnosticImpl> reconcile(SimpleTextDocumentService documents, TextDocument doc) {
		try {
			Iterator<Node> asts = yaml.composeAll(new StringReader(doc.getText())).iterator();
			while (asts.hasNext()) {
				asts.next();
			}
			return ImmutableList.of();
		} catch (YAMLException e) {
			return ImmutableList.of(parseError(e));
		}
	}

	private DiagnosticImpl parseError(YAMLException e) {
		DiagnosticImpl d = new DiagnosticImpl();
		d.setMessage(getMessage(e));
		d.setRange(getRange(e));
		d.setSeverity(DiagnosticSeverity.Error);
		d.setCode(ErrorCodes.YAML_SYNTAX_ERROR);
		d.setSource("yaml");
		return d;
	}

	private String getMessage(YAMLException e) {
		if (e instanceof MarkedYAMLException) {
			return ((MarkedYAMLException) e).getProblem();
		}
		return e.getMessage();
	}

	private RangeImpl getRange(YAMLException _e) {
		if (_e instanceof MarkedYAMLException) {
			MarkedYAMLException e = (MarkedYAMLException) _e;

			PositionImpl start = new PositionImpl();
			start.setLine(e.getProblemMark().getLine());
			start.setCharacter(e.getProblemMark().getColumn());
			
			RangeImpl rng = new RangeImpl();
			rng.setStart(start);
			rng.setEnd(start);
			return rng;
		}
		return null;
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
