/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import org.springframework.ide.vscode.commons.util.ValueParseException;
import org.springframework.ide.vscode.commons.util.ValueParser;

public class BoshValueParsers {

	public static final ValueParser INTEGER_OR_RANGE = new ValueParser() {

		private int findDash(String s) throws ValueParseException {
			int firstDash = s.indexOf('-');
			if (firstDash<0) {
				return firstDash; //no dash... and that's okay!
			}
			int secondDash = s.indexOf('-', firstDash+1);
			if (secondDash>=0) {
				//Only one dash is expected!
				throw new ValueParseException("Should be either a Integer, or a range (of the form '<integer>-<integer>')");
			}
			return firstDash;
		}

		@Override
		public Object parse(String s) throws Exception {
			int dash = findDash(s);
			if (dash>=0) {
				int low, high;
				//range
				try {
					low = Integer.parseInt(s.substring(0, dash));
				} catch (Exception e) {
					throw new ValueParseException("Should be a Integer", 0, dash);
				}
				try {
					high = Integer.parseInt(s.substring(dash+1));
				} catch (Exception e) {
					throw new ValueParseException("Should be a Integer", dash+1, s.length());
				}
				if (low>high) {
					throw new ValueParseException(low + " should be smaller than "+high);
				}
			} else {
				//integer
				try {
					return Integer.parseInt(s);
				} catch (Exception e) {
					throw new ValueParseException("Should be either a Integer, or a range (of the form '<integer>-<integer>')");
				}
			}
			return s;
		}
	};


}
