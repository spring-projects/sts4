/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.util;

/**
 * @author Kris De Volder
 */
public class YamlUtil {

	/**
	 * If any one of these is found at the start of a string then the string requires
	 * to be escaped.
	 */
	private static final String[] SPECIAL_START = {
			//This list is probably not complete.
			"!",
			"#",
			"&",
			"*",
			">",
			"|",
			"?",
			"{",
			"}",
			"[",
			"]",
			",",
			"\"",
			"'",
			"`",
			"@",
			"- ",
			"\t",
			" ",
//			"\n", //also included in 'special content' so no need to check at start specifically.
//			"\r"
	};

	/**
	 * If any of these is found at the end of a string then the string requires to be
	 * escaped.
	 */
	private static final String[] SPECIAL_END = {
			"\t",
			" "
//			"\n", //also included in 'special content' so no need to check at start specifically.
//			"\r"
	};

	/**
	 * If any of these is found inside a string then the string requires to be escaped.
	 */
	private static final String[] SPECIAL_CONTENT = {
		": ", " #", "\n", "\r"
	};

	/**
	 * Given a string value convert it into a format that can be inserted into a yml file.
	 */
	public static String stringEscape(String value) {
		if (canInsertAsIs(value)) {
			return value;
		}
		//TODO: this does not properly handle values that contain line-breaks (linebreaks are no alowed in single-line stirngs, such as may
		// be used for 'keys' in yaml. And although they are allowed in mult-line strings, they will be subject to new end-of-line folding
		// this processing of newlines by yaml parser will mean that the value is not the same when parsed.
		//For string like that we probably need to resort to double-quoted strings using escape sequences using '\'.
		//These cases are rare and not yet implemented here.
		return "'"+value.replace("'", "''")+"'";
	}

	private static boolean canInsertAsIs(String value) {
		//See https://www.activestate.com/blog/2014/07/yaml-pro
		// section: "Don't Over-quote your Strings"
		for (String special : SPECIAL_START) {
			if (value.startsWith(special)) {
				return false;
			}
		}
		for (String special : SPECIAL_END) {
			if (value.endsWith(special)) {
				return false;
			}
		}
		for (String special : SPECIAL_CONTENT) {
			if (value.contains(special)) {
				return false;
			}
		}
		//Nothing special. Safe to include verbatim.
		return true;
	}

}
