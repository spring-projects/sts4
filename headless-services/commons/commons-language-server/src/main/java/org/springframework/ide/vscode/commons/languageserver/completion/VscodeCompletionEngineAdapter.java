/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.TextReplace;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SortKeys;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adapts a {@link ICompletionEngine}, wrapping it, to implement {@link VscodeCompletionEngine}
 */
public class VscodeCompletionEngineAdapter implements VscodeCompletionEngine {

	private final static int MAX_COMPLETIONS = 50;
	private int maxCompletions = MAX_COMPLETIONS;
	final static Logger logger = LoggerFactory.getLogger(VscodeCompletionEngineAdapter.class);
	public static final String VS_CODE_CURSOR_MARKER = "{{}}";

	private SimpleLanguageServer server;
	private ICompletionEngine engine;

	public VscodeCompletionEngineAdapter(SimpleLanguageServer server, ICompletionEngine engine) {
		this.server = server;
		this.engine = engine;
	}

	public void setMaxCompletionsNumber(int maxCompletions) {
		this.maxCompletions = maxCompletions;
	}

	@Override
	public CompletableFuture<CompletionList> getCompletions(TextDocumentPositionParams params) {
		return getCompletionsMono(params).toFuture();
	}

	private Mono<CompletionList> getCompletionsMono(TextDocumentPositionParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.get(params).copy();
		if (doc!=null) {
			return Mono.fromCallable(() -> {
				//TODO: This callable is a 'big lump of work' so can't be canceled in pieces.
				// Should we push using of reactive streams down further and compose this all
				// using reactive style? If not then this is overkill could just as well use
				// only standard Java API such as Executor and CompletableFuture directly.
				int offset = doc.toOffset(params.getPosition());
				List<ICompletionProposal> completions = new ArrayList<>(engine.getCompletions(doc, offset));
				Collections.sort(completions, ScoreableProposal.COMPARATOR);
				CompletionList list = new CompletionList();
				list.setIsIncomplete(false);
				List<CompletionItem> items = new ArrayList<>(completions.size());
				SortKeys sortkeys = new SortKeys();
				int count = 0;
				for (ICompletionProposal c : completions) {
					count++;
					if (maxCompletions > 0 && count>maxCompletions) {
						list.setIsIncomplete(true);
						break;
					}
					try {
						items.add(adaptItem(doc, c, sortkeys));
					} catch (Exception e) {
						logger.error("error computing completion", e);
					}
				}
				list.setItems(items);
				return list;
			})
			.subscribeOn(Schedulers.elastic()); //!!! without this the mono will just be computed on the same thread that calls it.
		}
		return Mono.just(SimpleTextDocumentService.NO_COMPLETIONS);
	}

	private CompletionItem adaptItem(TextDocument doc, ICompletionProposal completion, SortKeys sortkeys) throws Exception {
		CompletionItem item = new CompletionItem();
		item.setLabel(completion.getLabel());
		item.setKind(completion.getKind());
		item.setSortText(sortkeys.next());
		item.setFilterText(completion.getFilterText());
		item.setDetail(completion.getDetail());
		item.setDocumentation(toMarkdown(completion.getDocumentation()));
		adaptEdits(item, doc, completion.getTextEdit());
		return item;
	}

	private String toMarkdown(Renderable r) {
		if (r!=null) {
			return r.toMarkdown();
		}
		return null;
	}

	private void adaptEdits(CompletionItem item, TextDocument doc, DocumentEdits edits) throws Exception {
		TextReplace replaceEdit = edits.asReplacement(doc);
		if (replaceEdit==null) {
			//The original edit does nothing.
			item.setInsertText("");
		} else {
			TextDocument newDoc = doc.copy();
			edits.apply(newDoc);
			TextEdit vscodeEdit = new TextEdit();
			vscodeEdit.setRange(doc.toRange(replaceEdit.start, replaceEdit.end-replaceEdit.start));
			vscodeEdit.setNewText(vscodeIndentFix(doc, vscodeEdit.getRange().getStart(), replaceEdit.newText));
			//TODO: cursor offset within newText? for now we assume its always at the end.
			item.setTextEdit(vscodeEdit);
			item.setInsertTextFormat(InsertTextFormat.Snippet);
		}
	}

	private String vscodeIndentFix(TextDocument doc, Position start, String newText) {
		//Vscode applies some magic indent to a multi-line edit text. We do everything ourself so we have adjust for the magic
		// and do some kind of 'inverse magic' here.
		//See here: https://github.com/Microsoft/language-server-protocol/issues/83
		int referenceLine = start.getLine();
		int referenceLineIndent = doc.getLineIndentation(referenceLine);
		int vscodeMagicIndent = Math.min(start.getCharacter(), referenceLineIndent);
		return vscodeMagicIndent>0
				? StringUtil.stripIndentation(vscodeMagicIndent, newText)
				: newText;
	}


	@Override
	public CompletableFuture<CompletionItem> resolveCompletion(CompletionItem unresolved) {
		//TODO: item is pre-resoved so we don't do anything, but we really should somehow defer some work, such as
		// for example computing docs and edits to resolve time.
		//The tricky part is that we have to probably remember infos about the unresolved elements somehow so we can resolve later.
		return Futures.of(unresolved);
	}
}
