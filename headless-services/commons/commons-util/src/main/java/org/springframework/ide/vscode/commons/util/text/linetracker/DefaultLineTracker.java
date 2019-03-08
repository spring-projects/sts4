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

package org.springframework.ide.vscode.commons.util.text.linetracker;

import java.util.Arrays;

/**
 * Standard implementation of {@link org.eclipse.jface.text.ILineTracker}.
 * <p>
 * The line tracker considers the three common line delimiters which are '\n',
 * '\r', '\r\n'.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultLineTracker extends AbstractLineTracker {

	/** The predefined delimiters of this tracker */
	public final static String[] DELIMITERS= { "\r", "\n", "\r\n" }; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
	/** A predefined delimiter information which is always reused as return value */
	private DelimiterInfo fDelimiterInfo= new DelimiterInfo();


	/**
	 * Creates a standard line tracker.
	 */
	public DefaultLineTracker() {
	}

	@Override
	public String[] getLegalLineDelimiters() {
		return Arrays.copyOf(DELIMITERS, DELIMITERS.length);
	}

	@Override
	protected DelimiterInfo nextDelimiterInfo(String text, int offset) {

		char ch;
		int length= text.length();
		for (int i= offset; i < length; i++) {

			ch= text.charAt(i);
			if (ch == '\r') {

				if (i + 1 < length) {
					if (text.charAt(i + 1) == '\n') {
						fDelimiterInfo.delimiter= DELIMITERS[2];
						fDelimiterInfo.delimiterIndex= i;
						fDelimiterInfo.delimiterLength= 2;
						return fDelimiterInfo;
					}
				}

				fDelimiterInfo.delimiter= DELIMITERS[0];
				fDelimiterInfo.delimiterIndex= i;
				fDelimiterInfo.delimiterLength= 1;
				return fDelimiterInfo;

			} else if (ch == '\n') {

				fDelimiterInfo.delimiter= DELIMITERS[1];
				fDelimiterInfo.delimiterIndex= i;
				fDelimiterInfo.delimiterLength= 1;
				return fDelimiterInfo;
			}
		}

		return null;
	}
}
