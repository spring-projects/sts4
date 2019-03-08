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
package org.springframework.ide.vscode.concourse;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.ValueParser;

import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.*;

public class DurationParserTest {

	private ValueParser parser = ConcourseValueParsers.DURATION;
	
	@Test
	public void goodExamples() throws Exception {
		parser.parse("1h40m");
		parser.parse("1.5h");
		parser.parse("23h59m59s99ms200µs100ns");
	}

	@Test
	public void badExamples() {
		does_not_parse("1h:40m");
		does_not_parse("15h 30m");
		does_not_parse("23hours");
	}

	private void does_not_parse(String string) {
		try {
			parser.parse(string);
			fail("Should have failed parsing!");
		} catch (Exception e) {
			assertContains("Duration", ExceptionUtil.getMessage(e));
		}
	}

}
