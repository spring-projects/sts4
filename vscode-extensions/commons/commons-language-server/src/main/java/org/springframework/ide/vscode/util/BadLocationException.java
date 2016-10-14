package org.springframework.ide.vscode.util;

/**
 * Replacement for Eclipse's BadLocationException (so as ot make porting code easier)
 */
public class BadLocationException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadLocationException(Throwable e) {
		super(e);
	}

	public BadLocationException() {
	}

}
