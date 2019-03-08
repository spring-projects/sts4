/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
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
import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.assertContains;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.ValueParser;

public class TimeOfDayParserTest {

	private ValueParser parser = ConcourseValueParsers.TIME_OF_DAY;

	@Test
	public void goodExamples() throws Exception {
		String[] examples = {
			"3:04 PM -0700",
			"3:04 PM +0700",
			"3:04 AM +0700",
			"00:00 AM +0700",
			"23:59 PM +0800",

			"3PM -0700",
			"0AM +0800",
			"24AM +0800",

			"3 PM -0700",
			"0 AM -0700",
			"24 PM -1234",

			"15:04 -0700",
			"0:00 -0700",
			"23:59 -0700",

			"304 -0700",
			"1504 -0700",
			"0004 -0700",
			"2359 -0700",

			"3:04 PM",
			"0:00 AM",
			"11:59 PM",

			"3PM",
			"1AM",
			"23PM",

			"3 PM",
			"1 AM",
			"23 PM",

			"15:04",
			"0:00",
			"00:00",
			"23:59",

			"1504",
			"0000",
			"2359"
		};
		for (String string : examples) {
			parser.parse(string);
		}
	}

	@Test
	public void badExamples() {
		does_not_parse("arbirary garbage");
		does_not_parse("3:04 PM -0700 extra");
		does_not_parse("extra 3:04 PM -0700 extra");
		does_not_parse("extra 3:04 PM -0700");
	}

	private void does_not_parse(String string) {
		try {
			parser.parse(string);
			fail("Should have failed parsing!");
		} catch (Exception e) {
			assertContains("Time", ExceptionUtil.getMessage(e));
		}
	}

}
