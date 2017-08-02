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

public class SnippetBuilder {

	private int nextPlaceHolderId = 1;
	private StringBuilder buf = new StringBuilder();

	public SnippetBuilder text(String text) {
		buf.append(text);
		return this;
	}

	/**
	 * Create a new `placeholder` and appends it to the snippet.
	 */
	public SnippetBuilder placeHolder() {
		buf.append(createPlaceHolder(nextPlaceHolderId++));
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

	@Override
	public String toString() {
		return buf.toString();
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
