package org.springframework.ide.vscode.testharness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.text.BadLocationException;

import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.Position;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.Range;

public class Editor {
	
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

	public Editor(LanguageServerHarness harness, String contents) throws Exception {
		this.harness = harness;
		this.document = harness.openDocument(harness.createWorkingCopy(contents));
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
		List<Diagnostic> actualProblems = new ArrayList<>(editor.reconcile());
		Collections.sort(actualProblems, PROBLEM_COMPARATOR);
		String bad = null;
		if (actualProblems.size()!=expectedProblems.length) {
			bad = "Wrong number of problems (expecting "+expectedProblems.length+" but found "+actualProblems.size()+")";
		} else {
			for (int i = 0; i < expectedProblems.length; i++) {
				if (!matchProblem(editor, actualProblems.get(i), expectedProblems[i])) {
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

	public String getText(Range range) {
		return document.getText(range);
	}

	public void setText(String newContent) throws Exception {
		document = harness.changeDocument(document.getUri(), newContent);
	}

	private boolean matchProblem(Editor editor, Diagnostic problem, String expect) {
		String[] parts = expect.split("\\|");
		assertEquals(2, parts.length);
		String badSnippet = parts[0];
		String messageSnippet = parts[1];
		boolean spaceSensitive = badSnippet.trim().length()<badSnippet.length();
		boolean emptyRange = problem.getRange().getStart().equals(problem.getRange().getEnd());
		String actualBadSnippet = emptyRange 
				? editor.getCharAt(problem.getRange().getStart())
				: editor.getText(problem.getRange());
		if (!spaceSensitive) {
			actualBadSnippet = actualBadSnippet.trim();
		}
		return actualBadSnippet.equals(badSnippet)
				&& problem.getMessage().contains(messageSnippet);
	}

	private String getCharAt(Position start) {
		int offset = document.toOffset(start);
		return document.getText().substring(offset, offset+1);
	}

	@SuppressWarnings("unchecked")
	private List<Diagnostic> reconcile() {
		// We assume the language server works synchronously for now and it does an immediate reconcile
		// when the document changes. In the future this is probably not going to be the case though and then this
		// method will need to somehow ensure the linter is done working before retrieving the problems from the
		// test harness.
		PublishDiagnosticsParams diagnostics = harness.getDiagnostics(document);
		if (diagnostics!=null) {
			return (List<Diagnostic>) diagnostics.getDiagnostics();
		}
		return Collections.emptyList();
	}

	public void assertCompletions(String... specs) {
		
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public void assertIsHoverRegion(String string) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	public void assertHoverContains(String string, String string2) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

}
