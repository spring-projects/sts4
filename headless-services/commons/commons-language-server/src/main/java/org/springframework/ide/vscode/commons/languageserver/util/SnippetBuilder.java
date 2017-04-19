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
	 * match the undocumented format vscode currently supports.
	 * <p>
	 * Note: this format is explicitly different from what the LSP
	 * specifies. So it is very likely we should change this implementation
	 * in the near future.
	 */
	protected String createPlaceHolder(int id) {
		//Default implementation now only handes the undocumented snippet format that vscode supports.
		return "{{"+id+":"+"}}";
	}

	@Override
	public String toString() {
		return buf.toString();
	}

}
