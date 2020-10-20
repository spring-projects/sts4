/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.util;

/**
 * A snippet that can be rendered into html.
 *
 * @author Kris De Volder
 */
@FunctionalInterface
public interface HtmlSnippet {

	void render(HtmlBuffer html);

	default public String toHtml() {
		HtmlBuffer buf = new HtmlBuffer();
		render(buf);
		return buf.toString();
	}

	// Create snippets:

	public static HtmlSnippet text(final String text) {
		return new HtmlSnippet() {
			@Override
			public void render(HtmlBuffer html) {
				html.text(text);
			}
		};
	}

	public static HtmlSnippet raw(final String rawHtml) {
		return new HtmlSnippet() {
			@Override
			public void render(HtmlBuffer html) {
				html.raw(rawHtml);
			}
		};
	}

	public static HtmlSnippet italic(String text) {
		return italic(text(text));
	}

	public static HtmlSnippet italic(final HtmlSnippet wrappee) {
		return new HtmlSnippet() {
			@Override
			public void render(HtmlBuffer html) {
				html.raw("<i>");
				wrappee.render(html);
				html.raw("</i>");
			}
		};
	}

	// add more as needed ...
}
