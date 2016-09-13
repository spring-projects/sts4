package org.springframework.ide.vscode.testharness;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.typefox.lsapi.Position;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.TextDocumentIdentifierImpl;
import io.typefox.lsapi.TextDocumentItemImpl;

public class TextDocumentInfo {

	Pattern NEWLINE = Pattern.compile("\\r|\\n|\\r\\n|\\n\\r");
	
	private final TextDocumentItemImpl document;
	
	private int[] _lineStarts;

	public TextDocumentInfo(TextDocumentItemImpl document) {
		this.document = document;
	}

	public String getLanguageId() {
		return getDocument().getLanguageId();
	}

	public int getVersion() {
		return getDocument().getVersion();
	}

	public String getText() {
		return getDocument().getText();
	}

	public String getUri() {
		return getDocument().getUri();
	}

	public TextDocumentItemImpl getDocument() {
		return document;
	}

	public String getText(Range rng) {
		int start = toOffset(rng.getStart());
		int end = toOffset(rng.getEnd());
		return getText().substring(start, end);
	}

	public int toOffset(Position p) {
		int startOfLine = startOfLine(p.getLine());
		return startOfLine+p.getCharacter();
	}

	private int startOfLine(int line) {
		return lineStarts()[line];
	}

	private int[] lineStarts() {
		if (_lineStarts==null) {
			_lineStarts = parseLines();
		}
		return _lineStarts;
	}

	private int[] parseLines() {
		List<Integer> lineStarts = new ArrayList<>();
		lineStarts.add(0);
		Matcher matcher = NEWLINE.matcher(getText());
		int pos = 0;
		while (matcher.find(pos)) {
			lineStarts.add(pos = matcher.end());
		}
		int[] array = new int[lineStarts.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = lineStarts.get(i);
		}
		return array;
	}

	/**
	 * Find and return the (first) position of a given text snippet in the
	 * document.
	 * 
	 * @return The position, or null if the snippet can't be found.
	 */
	public Position positionOf(String snippet) {
		int offset = getText().indexOf(snippet);
		if (offset>=0) {
			return toPosition(offset);
		}
		return null;
	}

	public Position toPosition(int offset) {
		int line = lineNumber(offset);
		int startOfLine = startOfLine(line);
		int column = offset - startOfLine;
		PositionImpl pos = new PositionImpl();
		pos.setCharacter(column);
		pos.setLine(line);
		return pos;
	}

	/**
	 * Determine the line-number a given offset (i.e. what line is the offset inside of?)
	 */
	private int lineNumber(int offset) {
		int[] lineStarts = lineStarts();
		// TODO Could use binary search which is faster
		int lineNumber = 0;
		for (int i = 0; i < lineStarts.length; i++) {
			if (lineStarts[i]<=offset) {
				lineNumber = i;
			} else {
				return lineNumber;
			}
		}
		return lineNumber;
	}

	public TextDocumentIdentifierImpl getId() {
		TextDocumentIdentifierImpl id = new TextDocumentIdentifierImpl();
		id.setUri(getUri());
		return id;
	}
	
}
