/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpParser implements ValueParser {

	final private Pattern pat;
	final private String typeName; // used only for error message
	final private String patternDescription; //Human readable description of the regexp pay

	/**
	 * Create a RegexpParser which succeeds if the input string matches the given regexp
	 * and fail otherwise.
	 *
	 * @param regexp
	 * @param typeName Name of the type (used in error message for failing parses)
	 * @param patternDescription Human readable description of the regexp pattern (included in the error message for failing parses)
	 */
	public RegexpParser(String regexp, String typeName, String patternDescription) {
		super();
		this.pat = Pattern.compile(regexp);
		this.typeName = typeName;
		this.patternDescription = patternDescription;
	}

	@Override
	public Object parse(String str) throws Exception {
		Matcher matcher = pat.matcher(str);
		if (matcher.matches()) {
			return matcher;
		}
		throw new ValueParseException("'"+str+"' is not a valid '"+typeName+"'. "+patternDescription);
	}

}
