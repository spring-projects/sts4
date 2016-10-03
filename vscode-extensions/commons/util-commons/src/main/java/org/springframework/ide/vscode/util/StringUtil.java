package org.springframework.ide.vscode.util;

public class StringUtil {
	public static boolean hasText(String name) {
		return name!=null && !name.trim().equals("");
	}

	public static String collectionToDelimitedString(Iterable<String> strings, String delim) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String s : strings) {
			if (!first) {
				b.append(delim);
			}
			b.append(s);
			first = false;
		}
		return b.toString();
	}

	public static String trimEnd(String s) {
		if (s!=null) {
			return s.replaceAll("\\s+\\z", "");
		}
		return null;
	}

}
