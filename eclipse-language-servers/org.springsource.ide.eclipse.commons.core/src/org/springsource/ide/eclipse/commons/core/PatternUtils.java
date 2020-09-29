/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class PatternUtils {

	private PatternUtils() {
		// Not for instantiation
	}

	/**
	 * Creates a pattern element from the pattern string which is either a
	 * reg-ex expression or of wildcard format ('*' matches any character and
	 * '?' matches one character).
	 * @param pattern  the search pattern
	 * @param isCaseSensitive set to <code>true</code> to create a case
	 * 				insensitve pattern
	 * @param isRegexSearch <code>true</code> if the passed string is a reg-ex
	 * 				pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String pattern,
					boolean isCaseSensitive, boolean isRegexSearch)
					throws PatternSyntaxException {
		if (!isRegexSearch) {
			pattern = toRegExFormat(pattern);
		}
		if (!isCaseSensitive) {
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE |
									 Pattern.UNICODE_CASE | Pattern.MULTILINE);
		}
		return Pattern.compile(pattern, Pattern.MULTILINE);
	}

	/**
	 * Converts wildcard format ('*' and '?') to reg-ex format.
	 */
	private static String toRegExFormat(String pattern) {
		StringBuffer regex = new StringBuffer(pattern.length());
		boolean escaped = false;
		boolean quoting = false;
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c == '*' && !escaped) {
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append(".*");
				escaped = false;
				continue;
			} else if (c == '?' && !escaped) {
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append(".");
				escaped = false;
				continue;
			} else if (c == '\\' && !escaped) {
				escaped = true;
				continue;								
			} else if (c == '\\' && escaped) {
				escaped = false;
				if (quoting) {
					regex.append("\\E");
					quoting = false;
				}
				regex.append("\\\\");
				continue;								
			}
			if (!quoting) {
				regex.append("\\Q");
				quoting = true;
			}
			if (escaped && c != '*' && c != '?' && c != '\\') {
				regex.append('\\');
			}
			regex.append(c);
			escaped = c == '\\';
		}
		if (quoting) {
			regex.append("\\E");
		}
		return regex.toString();
	}
}
