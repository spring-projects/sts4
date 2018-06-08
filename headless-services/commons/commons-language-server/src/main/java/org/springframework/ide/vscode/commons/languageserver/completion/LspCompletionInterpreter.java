/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.Direction;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.OffsetTransformer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class LspCompletionInterpreter implements IDocumentState {

	private DocumentState docState;

	private DocumentRegion mainLine;
	private DocumentRegion mainEditRegion;
	private DocumentRegion beforeMainEditRegion;
	private DocumentRegion afterMainEditRegion;

	private boolean needsCursorMove = false;

	private TextDocument originalDoc;

	private SimpleLanguageServer server;


	private static final Logger logger = LoggerFactory.getLogger(LspCompletionInterpreter.class);

	public LspCompletionInterpreter(TextDocument originalDoc, int cursor, SimpleLanguageServer server) {
		this.originalDoc = originalDoc;
		this.server = server;
		this.docState = new DocumentState(originalDoc.copy());
		this.mainLine = new DocumentRegion(originalDoc, originalDoc.getLineInformationOfOffset(cursor));
	}

	@Override
	public void insert(boolean grabCursor, int start, String text) throws BadLocationException {
		trackEditRegions(start, start);
		docState.insert(grabCursor, start, text);
	}

	@Override
	public void delete(boolean grabCursor, int start, int end) throws BadLocationException {
		trackEditRegions(start, end);
		docState.delete(grabCursor, start, end);
	}

	protected void trackEditRegions(int start, int end) {
		DocumentRegion editRegion = new DocumentRegion(originalDoc, start, end);
		this.needsCursorMove = true;
		if (isMainEdit(start, end)) {
			mainEditRegion = editRegion.merge(mainEditRegion);
			this.needsCursorMove = false;
		} else if (allBefore(start, end)) {
			beforeMainEditRegion = editRegion.merge(beforeMainEditRegion);
		} else if (allAfter(start, end)){
			afterMainEditRegion = editRegion.merge(afterMainEditRegion);
		} else {
			if (start < mainLine.getStart()) {
				DocumentRegion beforePiece = new DocumentRegion(originalDoc, start, mainLine.getStart());
				beforeMainEditRegion = beforePiece.merge(beforeMainEditRegion);
				editRegion = new DocumentRegion(originalDoc, mainLine.getStart(), editRegion.getEnd());
			}
			if (end > mainLine.getEnd()) {
				DocumentRegion afterPiece = new DocumentRegion(originalDoc, mainLine.getEnd(), end);
				afterMainEditRegion = afterPiece.merge(afterMainEditRegion);
				editRegion = new DocumentRegion(originalDoc, start, mainLine.getEnd());
			}
			mainEditRegion = editRegion.merge(mainEditRegion);
		}
	}

	public void resolveEdits(CompletionItem item) throws BadLocationException {
		TextEdit mainEdit = null;
		if (mainEditRegion != null) {
			mainEdit = new TextEdit();
			mainEdit.setRange(originalDoc.toRange(mainEditRegion));
			OffsetTransformer org2New = docState.getOrg2New();
			String newText = docState.getDocument().textBetween(
					org2New.transform(mainEditRegion.getStart(), Direction.BEFORE),
					org2New.transform(mainEditRegion.getEnd(), Direction.AFTER)
			);
			item.setTextEdit(mainEdit);
			item.setInsertTextFormat(InsertTextFormat.Snippet);

			if (Boolean.getBoolean("lsp.completions.indentation.enable")) {
				mainEdit.setNewText(newText);
			} else {
				mainEdit.setNewText(vscodeIndentFix(originalDoc, originalDoc.toPosition(mainEditRegion.getStart()), newText));
			}
		} else {
			item.setInsertText("");
		}

		ImmutableList.Builder<TextEdit> additionalEdits = ImmutableList.builder();

		TextEdit beforeEdit = resolveAdditionalEdit(beforeMainEditRegion, Direction.BEFORE);
		TextEdit afterEdit = resolveAdditionalEdit(afterMainEditRegion, Direction.AFTER);

		if (isAdjoining(beforeMainEditRegion, mainEditRegion) && beforeEdit.getNewText().equals("")) {
			//Special case because vscode seems to have some bug when additional edits deletes newline preceding
			// the insertion point (and we are adding it back in the mainEdit). This is actually a bit of
			// a weird thing for us to do anyways...
			//So we detect if the beforeEdit is a deletion that is canceled out by the main edits's insertion
			//and simplify the result.
			String deletedText = beforeMainEditRegion.toString();
			String insertedText = mainEdit.getNewText();
			if (insertedText.startsWith(deletedText)) {
				//cancels eachother!
				beforeEdit = null;
				mainEdit.setNewText(insertedText.substring(deletedText.length()));
			}
		}

		if (beforeEdit!=null) additionalEdits.add(beforeEdit);
		if (afterEdit!=null) additionalEdits.add(afterEdit);
		item.setAdditionalTextEdits(additionalEdits.build());

		if (mainEdit!=null && mainEdit.getNewText().equals("")) {
			//Vscode handles this poorly and adds 'junk' instead.
			mainEdit.setNewText(" "); // Adding a space. It is still junk but it is at least not immediately visible.
		}

		if (needsCursorMove) {
			Position position  = docState.getDocument().toPosition(docState.getCursor());
			item.setCommand(new Command("Move Cursor", server.MOVE_CURSOR_COMMAND_ID, ImmutableList.of(originalDoc.getUri(), position)));
		}
	}

	private boolean isAdjoining(DocumentRegion left, DocumentRegion right) {
		return left!=null && right!=null && left.getEnd()==right.getStart();
	}

	protected TextEdit resolveAdditionalEdit(DocumentRegion editRegion, Direction direction)
			throws BadLocationException {
		if (editRegion != null) {
			TextEdit edit = new TextEdit();
			edit.setRange(originalDoc.toRange(editRegion));
			OffsetTransformer org2New = docState.getOrg2New();
			String newText = docState.getDocument().textBetween(
					org2New.transform(editRegion.getStart(), direction),
					org2New.transform(editRegion.getEnd(), direction));
			edit.setNewText(newText);
			return edit;
		}
		return null;
	}

	private boolean isMainEdit(int start, int end) {
		return mainLine.containsOffset(start) && mainLine.containsOffset(end);
	}

	private boolean allBefore(int start, int end) {
		return end <= mainLine.getStart();
	}

	private boolean allAfter(int start, int end) {
		return start >= mainLine.getEnd();
	}

	private static String vscodeIndentFix(TextDocument doc, Position start, String newText) {
		//Vscode applies some magic indent to a multi-line edit text. We do everything ourself so we have adjust for the magic
		// and do some kind of 'inverse magic' here.
		//See here: https://github.com/Microsoft/language-server-protocol/issues/83
		IndentUtil indenter = new IndentUtil(doc);
		try {
			String refIndent = indenter.getReferenceIndent(doc.toOffset(start), doc);
			if (!refIndent.isEmpty()) {
				return  StringUtil.stripIndentation(refIndent, newText);
			}
		} catch (BadLocationException e) {
			logger.error("", e);
		}
		return newText;
	}
}
