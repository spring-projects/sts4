package org.springframework.ide.vscode.util;

public class Assert {

	public static void isNull(String msg, Object obj) {
		if (obj!=null) {
			throw new IllegalStateException(msg);
		}
	}

	public static void isLegal(boolean b) {
		if (!b) {
			throw new IllegalStateException();
		}
	}
	
}
