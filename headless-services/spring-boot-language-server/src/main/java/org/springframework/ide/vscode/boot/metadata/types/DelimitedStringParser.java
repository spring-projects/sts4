/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata.types;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.ValueParseException;
import org.springframework.ide.vscode.commons.util.ValueParser;

import com.google.common.collect.ImmutableList;

/**
 * Parser that is capable of parsing comma-separated lists. The parser
 * is constructed from another parser which is used to parse the elements.
 */
public class DelimitedStringParser implements ValueParser {

	private static final Pattern COMMA = Pattern.compile("(\\s)*\\,(\\s)*");;
	private Pattern SEPARATOR = COMMA;

	private ValueParser elementParser;

	public DelimitedStringParser(ValueParser elementParser) {
		super();
		this.elementParser = elementParser;
	}

	@Override
	public Object parse(String str) throws Exception {
		if (!StringUtil.hasText(str)) {
			return ImmutableList.of();
		}
		int offset = 0;
		Matcher matcher = SEPARATOR.matcher(str);
		ArrayList<Object> parsed = new ArrayList<>();
		while (matcher.find()) {
			parseOne(str, offset, matcher.start(), parsed);
			offset = matcher.end();
		}
		//parse last piece too!
		parseOne(str, offset, str.length(), parsed);
		return parsed;
	}

	private void parseOne(String input, int start, int end, ArrayList<Object> parsed) throws Exception {
		String piece = input.substring(start, end);
		try {
			Object parsedPiece = elementParser.parse(piece);
			parsed.add(parsedPiece);
		} catch (Exception e) {
			if (e instanceof ValueParseException) {
				((ValueParseException) e).adjustHighlight(start, end, piece);
			} else {
				e = new ValueParseException(ExceptionUtil.getMessage(e), start, end, piece);
			}
			throw e;
		}
	}

}
