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
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.HashMap;

import org.springframework.ide.vscode.commons.languageserver.util.PlaceHolderString.PlaceHolder;
import org.springframework.ide.vscode.commons.util.text.Region;

public class SnippetBuilder {

	/**
	 * Create a 'gimped' snippetbuilder which generates snippets without
	 * the '$' placeholders. This is useful to provide some snippet-like
	 * support in contexts that don't provide snippet support.
	 */
	public static SnippetBuilder gimped() {
		return new SnippetBuilder() {
			@Override
			protected String createPlaceHolder(int id) {
				return "";
			}
		};
	}

	private static final int FIRST_PLACE_HOLDER_ID = 1;
	private int nextPlaceHolderId = FIRST_PLACE_HOLDER_ID;
	private StringBuilder buf = new StringBuilder();
	private HashMap<Object, PlaceHolder> placeHolders = new HashMap<>();

	public SnippetBuilder text(String text) {
		buf.append(text);
		return this;
	}

	/**
	 * Create a new `placeholder` and appends it to the snippet.
	 */
	public SnippetBuilder placeHolder() {
		int offset = buf.length();
		int id = nextPlaceHolderId++;
		buf.append(createPlaceHolder(id));
		int end = buf.length();
		placeHolders.put(id, new PlaceHolderString.PlaceHolder(id, new Region(offset, end-offset)));
		return this;
	}

	/**
	 * Create a placeholder string (a 'tab stop' inside the snippet).
	 * <p>
	 * As there are different formats for placeholders, this method can
	 * be overridden by subclasses to support other formats.
	 * <p>
	 * The default implementation creates place holder strings that
	 * match format specified by LSP 3.0.
	 */
	protected String createPlaceHolder(int id) {
		return "$"+id;
	}

	public PlaceHolderString build() {
		return new PlaceHolderString(placeHolders, buf.toString());
	}

	@Override
	public String toString() {
		String str = buf.toString();
		if (getPlaceholderCount()==1 ) {
			String placeHolder = createPlaceHolder(FIRST_PLACE_HOLDER_ID);
			if (str.endsWith(placeHolder)) {
				str = str.substring(0, str.length()-placeHolder.length());
			}
		}
		return str;
	}

	public void newline(int indent) {
		buf.append("\n");
		for (int i = 0; i < indent; i++) {
			buf.append(' ');
		}
	}

	public void ensureSpace() {
		if (buf.length()>0 && !Character.isWhitespace(buf.charAt(buf.length()-1))) {
			buf.append(' ');
		}
	}

	/**
	 * @return The number of placeholder that where inserted in the snippet.
	 */
	public int getPlaceholderCount() {
		return nextPlaceHolderId-1;
	}

}
