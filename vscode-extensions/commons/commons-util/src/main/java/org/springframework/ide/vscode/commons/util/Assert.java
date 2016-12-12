package org.springframework.ide.vscode.commons.util;

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

	public static void isLegal(boolean b, String msg) {
		if (!b) {
			throw new IllegalStateException(msg);
		}
	}

	public static void isNotNull(Object it) {
		if (it==null) {
			throw new NullPointerException();
		}
	}

	public static void isTrue(boolean b) {
		isLegal(b);
	}

}
