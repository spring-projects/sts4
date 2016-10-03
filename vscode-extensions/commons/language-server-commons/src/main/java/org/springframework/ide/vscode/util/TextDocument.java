package org.springframework.ide.vscode.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.commons.reconcile.IDocument;

import io.typefox.lsapi.Range;
import io.typefox.lsapi.TextDocumentContentChangeEvent;
import io.typefox.lsapi.impl.PositionImpl;
import io.typefox.lsapi.impl.RangeImpl;

public class TextDocument implements IDocument {
	
	Pattern NEWLINE = Pattern.compile("\\r|\\n|\\r\\n|\\n\\r");
	private int[] _lineStarts;
	
	private final String uri;
	private String text = "";
	
	public TextDocument(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String get() {
		return getText();
	}
	
	public synchronized String getText() {
		return text;
	}
	
	public synchronized void setText(String text) {
		this.text = text;
		this._lineStarts = null;
	}
	public void apply(TextDocumentContentChangeEvent change) {
		Range rng = change.getRange();
		if (rng==null) {
			//full sync mode
			setText(change.getText());
		} else {
			//incremental sync mode
			throw new IllegalStateException("Incremental sync not yet implemented");
		}
	}

	/**
	 * Convert a simple offset+length pair into a vscode range. This is a method on 
	 * TextDocument because it requires splitting document into lines to determine
	 * line numbers from offsets.
	 */
	public RangeImpl toRange(int offset, int length) {
		int end = offset + length;
		RangeImpl range = new RangeImpl();
		range.setStart(toPosition(offset));
		range.setEnd(toPosition(end));
		return range;
	}
	
	/**
	 * Determine the line-number a given offset (i.e. what line is the offset inside of?)
	 */
	private int lineNumber(int offset) {
		int[] lineStarts = lineStarts();
		// TODO Should really use binary search here for speed
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


	public PositionImpl toPosition(int offset) {
		int line = lineNumber(offset);
		int startOfLine = startOfLine(line);
		int column = offset - startOfLine;
		PositionImpl pos = new PositionImpl();
		pos.setCharacter(column);
		pos.setLine(line);
		return pos;
	}

	private int startOfLine(int line) {
		return lineStarts()[line];
	}

	private synchronized int[] lineStarts() {
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



}
