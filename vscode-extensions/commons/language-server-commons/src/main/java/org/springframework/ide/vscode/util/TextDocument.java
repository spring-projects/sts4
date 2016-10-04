package org.springframework.ide.vscode.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.typefox.lsapi.Position;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.TextDocumentContentChangeEvent;
import io.typefox.lsapi.impl.PositionImpl;
import io.typefox.lsapi.impl.RangeImpl;
import io.typefox.lsapi.impl.TextDocumentItemImpl;

public class TextDocument implements IDocument {
	
	//TODO: This representiation of 'document content' is simplistic and inefficient
	// for large documents. 
	// Making any change, such as inserting a character a user typed, works by copying the String that represents the
	// contents. This could really become problematic for largish-documents when there are frequent changes.
	
	Pattern NEWLINE = Pattern.compile("\\r|\\n|\\r\\n|\\n\\r");
	private int[] _lineStarts;
	
	private final String uri;
	private String text = "";
	
	public TextDocument(String uri) {
		this.uri = uri;
	}
	
	private TextDocument(TextDocument other) {
		this.uri = other.uri;
		this.text = other.text;
		this._lineStarts = other._lineStarts; //no need to reparse lines.
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

	@Override
	public IRegion getLineInformationOfOffset(int offset) {
		if (offset<=getLength()) {
			int line = lineNumber(offset);
			return getLineInformation(line);
		}
		return null;
	}

	@Override
	public int getLength() {
		return text.length();
	}

	@Override
	public String get(int start, int len) throws BadLocationException {
		try {
			return text.substring(start, start+len);
		} catch (Exception e) {
			throw new BadLocationException(e);
		}
	}

	@Override
	public int getNumberOfLines() {
		return lineStarts().length;
	}

	@Override
	public String getDefaultLineDelimiter() {
		Matcher newlineFinder = NEWLINE.matcher(text);
		if (newlineFinder.find()) {
			return text.substring(newlineFinder.start(), newlineFinder.end());
		}
		return System.getProperty("line.separator");
	}

	@Override
	public char getChar(int offset) throws BadLocationException {
		if (offset>=0 && offset<text.length()) {
			return text.charAt(offset);
		}
		throw new BadLocationException();
	}

	@Override
	public int getLineOfOffset(int offset) {
		return lineNumber(offset);
	}

	@Override
	public IRegion getLineInformation(int line) {
		int[] starts = lineStarts();
		if (line<starts.length) {
			int start = starts[line];
			int nextLine = line+1;
			int end;
			if (nextLine>=starts.length) {
				//no next line. Last line in the document
				end = getLength();
			} else {
				end = starts[line+1];
				//To behave like Eclipse IDocument we must strip off line delimiter from the end.
				char c1 = getSafeChar(end-1);
				if (c1=='\r' || c1=='\n') {
					end--;
					char c2 = getSafeChar(end-1);
					if (c1!=c2 && (c2=='\r' || c2=='\n')) {
						end--;
					}
				}
			}
			
			int len = end - start;
			if (len<0) {
				len = 0;
			}
			return new Region(start, end-start);
		}
		return null;
	}

	private char getSafeChar(int ofs) {
		try {
			return getChar(ofs);
		} catch (BadLocationException e) {
			return 0;
		}
	}

	@Override
	public int getLineOffset(int line) {
		return lineStarts()[line];
	}
	
	public int toOffset(Position position) {
		int line = position.getLine();
		int lineStart = lineStarts()[line];
		return lineStart + position.getCharacter();
	}

	@Override
	public void replace(int start, int len, String ins) {
		text = text.substring(0, start) + ins + text.substring(start+len);
	}

	public synchronized TextDocument copy() {
		return new TextDocument(this);
	}

}
