package org.springframework.ide.vscode.languageserver.testharness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.text.BadLocationException;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.Assert;

import com.google.common.base.Strings;

public class Editor {

	static class EditorState {
		String documentContents;
		int selectionStart;
		int selectionEnd;

		public EditorState(String text) {
			selectionStart = text.indexOf(CURSOR);
			if (selectionStart>=0) {
				text = text.substring(0,selectionStart) + text.substring(selectionStart+CURSOR.length());
				selectionEnd = text.indexOf(CURSOR, selectionStart);
				if (selectionEnd>=0) {
					text = text.substring(0, selectionEnd) + text.substring(selectionEnd+CURSOR.length());
				} else {
					selectionEnd = selectionStart;
				}
			} else {
				//No CURSOR markers found
				selectionStart = text.length();
				selectionEnd = text.length();
			}
			this.documentContents = text;
		}
	}

	private static final String CURSOR = "<*>"; // used by our test harness
	private static final String VS_CODE_CURSOR_MARKER = "{{}}"; //vscode uses this in edits to mark cursor position

	private static final Comparator<Diagnostic> PROBLEM_COMPARATOR = new Comparator<Diagnostic>() {
		@Override
		public int compare(Diagnostic o1, Diagnostic o2) {
			int diff = compare(o1.getRange().getStart(), o2.getRange().getStart());
			if (diff!=0) return diff;
			return compare(o1.getRange().getEnd(), o2.getRange().getEnd());
		}

		private int compare(Position p1, Position p2) {
			int d = p1.getLine() - p2.getLine();
			if (d!=0) return d;
			return p1.getCharacter() - p2.getCharacter();
		}
	};

	private LanguageServerHarness harness;
	private TextDocumentInfo document;

	private int selectionEnd;

	private int selectionStart;
	private Set<String> ignoredTypes;

	public Editor(LanguageServerHarness harness, String contents) throws Exception {
		this.harness = harness;
		EditorState state = new EditorState(contents);
		this.document = harness.openDocument(harness.createWorkingCopy(state.documentContents));
		this.selectionStart = state.selectionStart;
		this.selectionEnd = state.selectionEnd;
		this.ignoredTypes = new HashSet<>();
	}

	/**
	 * Check that a 'expectedProblems' are found by the reconciler. Expected problems are
	 * specified by string of the form "${badSnippet}|${messageSnippet}". The badSnippet
	 * is the text expected to be covered by the marker's region and the message snippet must
	 * be found in the error marker's message.
	 * <p>
	 * The expected problems are matched one-to-one in the order given (so markers in the
	 * editor must appear in the expected order for the assert to pass).
	 *
	 * @param editor
	 * @param expectedProblems
	 * @throws BadLocationException
	 */
	public void assertProblems(String... expectedProblems) throws Exception {
		Editor editor = this;
		List<Diagnostic> actualProblems = new ArrayList<>(editor.reconcile().stream().filter(d -> {
			return !ignoredTypes.contains(d.getCode());
		}).collect(Collectors.toList()));
		Collections.sort(actualProblems, PROBLEM_COMPARATOR);
		String bad = null;
		if (actualProblems.size()!=expectedProblems.length) {
			bad = "Wrong number of problems (expecting "+expectedProblems.length+" but found "+actualProblems.size()+")";
		} else {
			for (int i = 0; i < expectedProblems.length; i++) {
				if (!matchProblem(actualProblems.get(i), expectedProblems[i])) {
					bad = "First mismatch at index "+i+": "+expectedProblems[i]+"\n";
					break;
				}
			}
		}
		if (bad!=null) {
			fail(bad+problemSumary(editor, actualProblems));
		}
	}

	private String problemSumary(Editor editor, List<Diagnostic> actualProblems) throws Exception {
		StringBuilder buf = new StringBuilder();
		for (Diagnostic p : actualProblems) {
			buf.append("\n----------------------\n");

			String snippet = editor.getText(p.getRange());
			buf.append("("+p.getRange().getStart().getLine()+", "+p.getRange().getStart().getCharacter()+")["+snippet+"]:\n");
			buf.append("   "+p.getMessage());
		}
		return buf.toString();
	}

	/**
	 * Get the editor text, with cursor markers inserted (for easy textual comparison
	 * after applying a proposal)
	 */
	public String getText() {
		String text = document.getText();
		text = text.substring(0, selectionEnd) + CURSOR + text.substring(selectionEnd);
		if (selectionStart<selectionEnd) {
			text = text.substring(0,selectionStart) + CURSOR + text.substring(selectionStart);
		}
		return deWindowsify(text);
	}

	public void setText(String content) throws Exception {
		EditorState state = new EditorState(content);
		document = harness.changeDocument(document.getUri(), state.documentContents);
		this.selectionStart = state.selectionStart;
		this.selectionEnd = state.selectionEnd;
	}

	/**
	 * @return The 'raw' text in the editor, i.e. without the cursor markers.
	 */
	public String getRawText() throws Exception {
		return document.getText();
	}

	public void setRawText(String newContent) throws Exception {
		document = harness.changeDocument(document.getUri(), newContent);
	}

	public String getText(Range range) {
		return document.getText(range);
	}

	private String deWindowsify(String text) {
		return text.replaceAll("\\r\\n", "\n");
	}


	private boolean matchProblem(Diagnostic problem, String expect) {
		String[] parts = expect.split("\\|");
		assertEquals(2, parts.length);
		String badSnippet = parts[0];
		String messageSnippet = parts[1];
		boolean spaceSensitive = badSnippet.trim().length()<badSnippet.length();
		boolean emptyRange = problem.getRange().getStart().equals(problem.getRange().getEnd());
		String actualBadSnippet = emptyRange
				? getCharAt(problem.getRange().getStart())
				: getText(problem.getRange());
		if (!spaceSensitive) {
			actualBadSnippet = actualBadSnippet.trim();
		}
		return actualBadSnippet.equals(badSnippet)
				&& problem.getMessage().contains(messageSnippet);
	}

	private String getCharAt(Position start) {
		String text = document.getText();
		int offset = document.toOffset(start);
		return offset<text.length()
			? text.substring(offset, offset+1)
			: "";
	}

	@SuppressWarnings("unchecked")
	private List<Diagnostic> reconcile() {
		// We assume the language server works synchronously for now and it does an immediate reconcile
		// when the document changes. In the future this is probably not going to be the case though and then this
		// method will need to somehow ensure the linter is done working before retrieving the problems from the
		// test harness.
		PublishDiagnosticsParams diagnostics = harness.getDiagnostics(document);
		if (diagnostics!=null) {
			return diagnostics.getDiagnostics();
		}
		return Collections.emptyList();
	}

	public void assertCompletions(String... expectTextAfter) throws Exception {
		StringBuilder expect = new StringBuilder();
		StringBuilder actual = new StringBuilder();
		for (String after : expectTextAfter) {
			expect.append(after);
			expect.append("\n-------------------\n");
		}

		for (CompletionItem completion : getCompletions()) {
			Editor editor = this.clone();
			editor.apply(completion);
			actual.append(editor.getText());
			actual.append("\n-------------------\n");
		}
		assertEquals(expect.toString(), actual.toString());
	}

	public void apply(CompletionItem completion) throws Exception {
		TextEdit edit = completion.getTextEdit();
		String docText = document.getText();
		if (edit!=null) {
			String replaceWith = edit.getNewText();
			//Apply indentfix, this is magic vscode seems to apply to edits returned by language server. So our harness has to
			// mimick that behavior. I'm not sure this fix is really emulating it faithfully as its undocumented :-(
			int indentFix = edit.getRange().getStart().getCharacter();
			replaceWith = replaceWith.replaceAll("\\n", "\n"+Strings.repeat(" ", indentFix));

			int cursorReplaceOffset = replaceWith.indexOf(VS_CODE_CURSOR_MARKER);
			if (cursorReplaceOffset>=0) {
				replaceWith = replaceWith.substring(0, cursorReplaceOffset) + replaceWith.substring(cursorReplaceOffset+VS_CODE_CURSOR_MARKER.length());
			} else {
				cursorReplaceOffset = replaceWith.length();
			}

			Range rng = edit.getRange();
			int start = document.toOffset(rng.getStart());
			int end = document.toOffset(rng.getEnd());
			String newText = docText.substring(0, start) + replaceWith + docText.substring(end);
			setRawText(newText);
			selectionStart = selectionEnd = start+cursorReplaceOffset;
		} else {
			String insertText = getInsertText(completion);
			String newText = docText.substring(0, selectionStart) + insertText + docText.substring(selectionStart);

			selectionStart+= insertText.length();
			selectionEnd += insertText.length();
			setRawText(newText);
		}
	}

	private String getInsertText(CompletionItem completion) {
		String s = completion.getInsertText();
		if (s==null) {
			//If no insertText is provided the label is used
			s = completion.getLabel();
		}
		return s;
	}

	@Override
	public Editor clone() {
		try {
			return new Editor(harness, getText());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<CompletionItem> getCompletions() throws Exception {
		CompletionList cl = harness.getCompletions(this.document, this.getCursor());
		ArrayList<CompletionItem> items = new ArrayList<>(cl.getItems());
		Collections.sort(items, new Comparator<CompletionItem>() {

			@Override
			public int compare(CompletionItem o1, CompletionItem o2) {
				return sortKey(o1).compareTo(sortKey(o2));
			}

			private String sortKey(CompletionItem item) {
				String k = item.getSortText();
				if (k==null) {
					k = item.getLabel();
				}
				return k;
			}
		});
		return items;
	}

	public CompletionItem getFirstCompletion() throws Exception {
		return getCompletions().get(0);
	}

	private Position getCursor() {
		return document.toPosition(selectionStart);
	}

	public void assertIsHoverRegion(String string) throws Exception {
		int hoverPosition = getRawText().indexOf(string) + string.length() / 2;
		Hover hover = harness.getHover(document, document.toPosition(hoverPosition));
		assertEquals(string, getText(hover.getRange()));
	}

	public void assertHoverContains(String hoverOver, String snippet) throws Exception {
		int hoverPosition = getRawText().indexOf(hoverOver) + hoverOver.length() / 2;
		Hover hover = harness.getHover(document, document.toPosition(hoverPosition));
		assertContains(snippet, hover.getContents().toString());	}

	public void assertNoHover(String string) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	/**
	 * Verifies an expected textSnippet is contained in the hover text that is
	 * computed when hovering mouse at position at the end of first occurrence of
	 * a given string in the editor.
	 */
	public void assertHoverText(String afterString, String expectSnippet) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public void setSelection(int start, int end) {
		Assert.assertTrue(start>=0);
		Assert.assertTrue(end>=start);
		Assert.assertTrue(end<=document.getText().length());
		this.selectionStart = start;
		this.selectionEnd = end;
	}

	@Override
	public String toString() {
		return "Editor(\n"+getText()+"\n)";
	}

	public void assertLinkTargets(String hoverOver, String... expecteds) {
		throw new UnsupportedOperationException("Not implemented yet!");
//		Editor editor = this;
//		int pos = editor.middleOf(hoverOver);
//		assertTrue("Not found in editor: '"+hoverOver+"'", pos>=0);
//
//		List<IJavaElement> targets = getLinkTargets(editor, pos);
//		assertEquals(expecteds.length, targets.size());
//		for (int i = 0; i < expecteds.length; i++) {
//			assertEquals(expecteds[i], JavaElementLabels.getElementLabel(targets.get(i), JavaElementLabels.DEFAULT_QUALIFIED | JavaElementLabels.M_PARAMETER_TYPES));
//		}
	}

	/**
	 * Get a problem that covers the given text in the editor. Throws exception
	 * if no matching problem is found.
	 */
	public Diagnostic assertProblem(String coveredText) throws Exception {
		Editor editor = this;
		List<Diagnostic> problems = editor.reconcile();
		for (Diagnostic p : problems) {
			String c = editor.getText(p.getRange());
			if (c.equals(coveredText)) {
				return p;
			}
		}
		fail("No problem found covering the text '"+coveredText+"' in: \n"
				+ problemSumary(editor, problems)
		);
		return null; //unreachable but compiler doesn't know
	}

	public CompletionItem assertFirstQuickfix(Diagnostic problem, String expectLabel) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public void assertText(String expected) {
		assertEquals(expected, getText());
	}

	public void ignoreProblem(Object type) {
		ignoredTypes.add(type.toString());
	}

}
