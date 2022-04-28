/*******************************************************************************
 * Copyright (c) 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.ValueParser;

import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.*;

public class IdentifierParserTest {

	private ValueParser parser = ConcourseValueParsers.IDENTIFIER;
	
	@Test
	public void goodExamples() throws Exception {
		parser.parse("identifier123.ha-boo_lalala");
		parser.parse("simple_ident");
		parser.parse("anything-with-dashes-123");
	}

	@Test
	public void badExamples() {
		does_not_parse("spaces are bad");
		does_not_parse("upperCaseisBad");
		does_not_parse("strange@symbols");
		does_not_parse("strange!symbols");
		does_not_parse("strange:symbols");
	}

	private void does_not_parse(String string) {
		try {
			parser.parse(string);
			fail("Should have failed parsing!");
		} catch (Exception e) {
			assertContains("Identifier", ExceptionUtil.getMessage(e));
		}
	}

}
