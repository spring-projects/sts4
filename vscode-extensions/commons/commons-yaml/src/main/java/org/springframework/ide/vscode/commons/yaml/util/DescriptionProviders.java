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
package org.springframework.ide.vscode.commons.yaml.util;

import java.io.InputStream;
import java.util.List;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfo;
import org.springframework.ide.vscode.commons.util.HtmlBuffer;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;

import com.google.common.collect.ImmutableList;

/**
 * Static methods and convenience constants for creating some 'description
 * providers'.
 *
 * @author Kris De Volder
 */
public class DescriptionProviders {

	private static final String NO_DESCRIPTION_TEXT = "no description";

	final static Logger logger = LoggerFactory.getLogger(DescriptionProviders.class);

	public static final HoverInfo NO_DESCRIPTION = italic(text(NO_DESCRIPTION_TEXT));

	public static Provider<HtmlSnippet> snippet(final HtmlSnippet snippet) {
		return new Provider<HtmlSnippet>() {
			@Override
			public String toString() {
				return snippet.toString();
			}

			@Override
			public HtmlSnippet get() {
				return snippet;
			}
		};
	}

	public static HoverInfo concat(HoverInfo... pieces) {
		return concat(ImmutableList.copyOf(pieces));
	}

	public static HoverInfo concat(List<HoverInfo> pieces) {
		if (pieces == null || pieces.size() == 0) {
			throw new IllegalArgumentException("At least one hover information is required for concat");
		} else if (pieces.size() == 1) {
			return pieces.get(0);
		} else {
			return new ConcatHoverInfo(pieces);
		}
	}

	public static HoverInfo italic(HoverInfo text) {
		return new HoverInfo() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append("*");
				text.renderAsMarkdown(buffer);
				buffer.append("*");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<i>");
				text.renderAsHtml(buffer);
				buffer.raw("</i>");
			}
		};
	}

	public static HoverInfo link(String text, String url) {
		return new HoverInfo() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append('[');
				buffer.append(text);
				buffer.append(']');
				if (url != null) {
					buffer.append('(');
					buffer.append(url);
					buffer.append(')');
				}
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<a href=\"");
				buffer.url("" + url);
				buffer.raw("\">");
				buffer.text(text);
				buffer.raw("</a>");
			}
		};
	}

	public static HoverInfo lineBreak() {
		return new HoverInfo() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append("\n\n");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<br>");
			}
		};
	}

	public static HoverInfo bold(HoverInfo text) {

		return new HoverInfo() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append("**");
				text.renderAsMarkdown(buffer);
				buffer.append("**");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<b>");
				text.renderAsHtml(buffer);
				buffer.raw("</b>");
			}
		};
	}

	public static HoverInfo text(String text) {
		return new HoverInfo() {
			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				// TODO: handle escaping
				buffer.append(text);
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.text(text);
			}
		};
	}

	public static HoverInfo fromClasspath(final Class<?> klass, final String resourcePath) {
		return new HoverInfo() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				String extension = ".md";
				String value = getText(klass, resourcePath, extension);
				if (value != null) {
					buffer.append(value);
				} else {
					NO_DESCRIPTION.renderAsMarkdown(buffer);
				}
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				String extension = ".html";
				String value = getText(klass, resourcePath, extension);
				if (value != null) {
					buffer.raw(value);
				} else {
					NO_DESCRIPTION.renderAsHtml(buffer);
				}
			}

			private String getText(final Class<?> klass, final String resourcePath, String extension) {
				try {
					InputStream stream = klass.getResourceAsStream(resourcePath + extension);
					if (stream != null) {
						return IOUtil.toString(stream);
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
				return null;
			}
		};
	}

	private static class ConcatHoverInfo implements HoverInfo {

		private HoverInfo[] pieces;

		ConcatHoverInfo(HoverInfo[] pieces) {
			this.pieces = pieces;
		}

		public ConcatHoverInfo(List<HoverInfo> pieces) {
			this(pieces.toArray(new HoverInfo[pieces.size()]));
		}

		@Override
		public void renderAsHtml(HtmlBuffer buffer) {
			for (HoverInfo hoverInfo : pieces) {
				hoverInfo.renderAsHtml(buffer);
			}
		}

		@Override
		public void renderAsMarkdown(StringBuilder buffer) {
			for (HoverInfo hoverInfo : pieces) {
				hoverInfo.renderAsMarkdown(buffer);
			}
		}

	}
}
