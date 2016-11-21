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
package org.springframework.ide.vscode.commons.util;

/**
 * Placeholder. Still need to figure out what exactly we should do with this in
 * vscode. TODO: rename to Renderable
 */
public interface Renderable {

	void renderAsHtml(HtmlBuffer buffer);

	void renderAsMarkdown(StringBuilder buffer);

	default String toMarkdown() {
		StringBuilder buffer = new StringBuilder();
		renderAsMarkdown(buffer);
		return buffer.toString();
	}

	default String toHtml() {
		HtmlBuffer buffer = new HtmlBuffer();
		renderAsHtml(buffer);
		return buffer.toString();
	}
}
