package org.springframework.ide.vscode.util;

public class StringUtil {
	public static boolean hasText(String name) {
		return name!=null && !name.trim().equals("");
	}
}
