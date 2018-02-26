/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.editor.harness;

//import org.eclipse.jface.viewers.StyledString;
//import org.eclipse.swt.custom.StyleRange;

public abstract class StyledStringMatcher {

//	public abstract void match(StyledString styledString) throws Exception;

	/**
	 * Creates a matcher that matches a given {@link StyledString} if a given String
	 * equals the concatenation of all the text in the {@link StyledString} formatted
	 * in 'plain' font (currently 'plain' just means that it is not using 'strikeout'
	 * as that is the only other style we currently care about).
	 */
	public static StyledStringMatcher plainFont(final String string) {
		throw new UnsupportedOperationException("Not yet implemented");
//		return new StyledStringMatcher() {
//			@Override
//			public String toString() {
//				return "plain("+string+")";
//			}
//
//			@Override
//			public void match(StyledString styledString) throws Exception {
//				StringBuilder extractedText = new StringBuilder();
//				String string = styledString.getString();
//				for (StyleRange styleRange : styledString.getStyleRanges()) {
//					if (!styleRange.strikeout) {
//						extractedText.append(string.substring(styleRange.start, styleRange.start + styleRange.length));
//					}
//				}
//				assertEquals(string, extractedText.toString());
//			}
//		};
	}

	/**
	 * Creates a matcher that matches a given {@link StyledString} if a given String
	 * equals the concatenation of all the text in the {@link StyledString} formatted
	 * in 'strikeout' font.
	 */
	public static StyledStringMatcher strikeout(final String expectedString) {
		throw new UnsupportedOperationException("Not yet implemented");
//		return new StyledStringMatcher() {
//			@Override
//			public String toString() {
//				return "strikeout("+expectedString+")";
//			}
//			@Override
//			public void match(StyledString styledString) throws Exception {
//				StringBuilder extractedText = new StringBuilder();
//				String unstyledString = styledString.getString();
//				for (StyleRange styleRange : styledString.getStyleRanges()) {
//					if (styleRange.strikeout) {
//						extractedText.append(unstyledString.substring(styleRange.start, styleRange.start + styleRange.length));
//					}
//				}
//				assertEquals(expectedString, extractedText.toString());
//			}
//		};
	}

}
