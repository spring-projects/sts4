package org.springframework.ide.vscode.util;

public interface IDocument {

	String get();
	IRegion getLineInformationOfOffset(int offset);
	int getLength();
	String get(int start, int len) throws BadLocationException;
	int getNumberOfLines();
	String getDefaultLineDelimiter();
	char getChar(int offset) throws BadLocationException;
	int getLineOfOffset(int offset);
	IRegion getLineInformation(int line);
	int getLineOffset(int line);
	void replace(int start, int len, String text);
	String textBetween(int start, int end) throws BadLocationException;

}
