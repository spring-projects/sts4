package org.springframework.ide.vscode.cloudfoundry.manifest.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.TextReplace;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.yaml.completion.DefaultCompletionFactory;
import org.springframework.ide.vscode.yaml.structure.YamlStructureParser;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.TextDocumentPositionParams;
import io.typefox.lsapi.impl.CompletionItemImpl;
import io.typefox.lsapi.impl.CompletionListImpl;
import io.typefox.lsapi.impl.PositionImpl;
import io.typefox.lsapi.impl.TextEditImpl;

/**
 * Adapts a {@link ICompletionEngine}, wrapping it, to implement {@link VscodeCompletionEngine}
 */
public class VscodeCompletionEngineAdapter implements VscodeCompletionEngine {

	final static Logger logger = LoggerFactory.getLogger(VscodeCompletionEngineAdapter.class);

	public static final String VS_CODE_CURSOR_MARKER = "{{}}";

	private SimpleLanguageServer server;
	private ICompletionEngine engine;

	public VscodeCompletionEngineAdapter(SimpleLanguageServer server, ICompletionEngine engine) {
		this.server = server;
		this.engine = engine;
	}

	@Override
	public CompletableFuture<CompletionList> getCompletions(TextDocumentPositionParams params) {
		//TODO: This returns a CompletableFuture which suggests we should try to do expensive work asyncly.
		// We are currently just doing all this in a blocking way and wrapping the already computed list into
		// a trivial pre-resolved future.
		try {
			SimpleTextDocumentService documents = server.getTextDocumentService();
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				int offset = doc.toOffset(params.getPosition());
				List<ICompletionProposal> completions = new ArrayList<>(engine.getCompletions(doc, offset));
				Collections.sort(completions, DefaultCompletionFactory.COMPARATOR);
				CompletionListImpl list = new CompletionListImpl();
				list.setIncomplete(false);
				List<CompletionItemImpl> items = new ArrayList<>(completions.size());
				SortKeys sortkeys = new SortKeys();
				for (ICompletionProposal c : completions) {
					try {
						items.add(adaptItem(doc, c, sortkeys));
					} catch (Exception e) {
						logger.error("error computing completion", e);
					}
				}
				list.setItems(items);
				return Futures.of(list);
			}
		} catch (Exception e) {
			logger.error("error computing completions", e);
		}
		return SimpleTextDocumentService.NO_COMPLETIONS;
	}

	private CompletionItemImpl adaptItem(TextDocument doc, ICompletionProposal completion, SortKeys sortkeys) throws Exception {
		CompletionItemImpl item = new CompletionItemImpl();
		item.setLabel(completion.getLabel());
		item.setKind(completion.getKind());
		item.setSortText(sortkeys.next());
		item.setFilterText(completion.getLabel());
		adaptEdits(item, doc, completion.getTextEdit());
		return item;
	}

	private void adaptEdits(CompletionItemImpl item, TextDocument doc, DocumentEdits edits) throws Exception {
		TextReplace replaceEdit = edits.asReplacement(doc);
		if (replaceEdit==null) {
			//The original edit does nothing.
			item.setInsertText("");
		} else {
			TextDocument newDoc = doc.copy();
			edits.apply(newDoc);
			TextEditImpl vscodeEdit = new TextEditImpl();
			vscodeEdit.setRange(newDoc.toRange(replaceEdit.start, replaceEdit.end-replaceEdit.start));
			vscodeEdit.setNewText(vscodeIndentFix(vscodeEdit.getRange().getStart(), replaceEdit.newText));
			//TODO: cursor offset within newText? for now we assume its always at the end.
			item.setTextEdit(vscodeEdit);
		}
	}

	private String vscodeIndentFix(PositionImpl start, String newText) {
		//Vscode applies some magic indent to a multi-line edit text. We do everything ourself so we have adjust for the magic
		// and do some kind of 'inverse magic' here.
		int vscodeMagicIndent = start.getCharacter();
		return YamlStructureParser.stripIndentation(vscodeMagicIndent, newText);
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletion(CompletionItem unresolved) {
		//TODO: item is pre-resoved so we don't do anything, but we really should somehow defer some work, such as
		// for example computing docs and edits to resolve time.
		//The tricky part is that we have to probably remember infos about the unresolved elements somehow so we can resolve later.
		return Futures.of(unresolved);
	}

}
