package org.springframework.ide.vscode.commons.util.text;

import org.springframework.ide.vscode.commons.util.BadLocationException;

public interface IDocument {

	String getUri();
	String get();
	IRegion getLineInformationOfOffset(int offset);
	int getLength();
	String get(int start, int len) throws BadLocationException;
	int getNumberOfLines();
	String getDefaultLineDelimiter();
	char getChar(int offset) throws BadLocationException;
	int getLineOfOffset(int offset) throws BadLocationException;
	IRegion getLineInformation(int line);
	int getLineOffset(int line) throws BadLocationException;
	void replace(int start, int len, String text) throws BadLocationException;
	String textBetween(int start, int end) throws BadLocationException;

}
