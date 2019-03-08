/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.overzealous.remark.Remark;

/**
 * Static methods and convenience constants for creating some 'description
 * providers'.
 *
 * @author Kris De Volder
 */
public class Renderables {

	private static final String NO_DESCRIPTION_TEXT = "no description";

	final static Logger logger = LoggerFactory.getLogger(Renderables.class);

	public static final Renderable NO_DESCRIPTION = italic(text(NO_DESCRIPTION_TEXT));
	
	public static Remark getHtmlToMarkdownConverter() {
		return new Remark();
	}
	
	@FunctionalInterface
	public interface HtmlContentFiller {
		void fill(HtmlBuffer buffer);
	}
	
	public static Renderable htmlBlob(HtmlContentFiller contentFiller) {
		return new Renderable() {

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				contentFiller.fill(buffer);
			}

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append(getHtmlToMarkdownConverter().convert(toHtml()));
			}
			
		};
	}

	public static Renderable htmlBlob(String html) {
		return htmlBlob(buffer -> buffer.raw(html));
	}
	
	public static Renderable mdBlob(String md) {
		return new Renderable() {
			
			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append(md);
			}
			
			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				throw new UnsupportedOperationException("Not implemented");
			}
		};
	}
	
	public static Renderable concat(Renderable... pieces) {
		return concat(ImmutableList.copyOf(pieces));
	}

	public static Renderable concat(List<Renderable> pieces) {
		if (pieces == null || pieces.size() == 0) {
			throw new IllegalArgumentException("At least one hover information is required for concat");
		} else if (pieces.size() == 1) {
			return pieces.get(0);
		} else {
			return new ConcatRenderables(pieces);
		}
	}

	public static Renderable italic(Renderable text) {
		return new Renderable() {

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
	
	public static Renderable inlineSnippet(Renderable text) {
		return new Renderable() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append("`");
				text.renderAsMarkdown(buffer);
				buffer.append("`");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<pre>");
				text.renderAsHtml(buffer);
				buffer.raw("</pre>");
			}
		};
	}
	
	public static Renderable paragraph(Renderable text) {
		return new Renderable() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				//TODO: This looks wrong. A paragraphs in markdown are created
				// by separating the text between them with TWO newlines. So this isn't
				// quite rigth as it provides no guarantees that there will be 
				// two newlines before or after the paragraph's text.
				// The correct implementation should probably check wether text in buffer already
				// ends with newline(s) and add more only if needed. Then it should 
				// also append double newline at its end.
				
				//TODO: verify that the if below solves the above "todo"
				if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '\n') {
					// 2 spaces and then new line would create a line break in text
					buffer.append("  ");
				}
				buffer.append("\n");
				text.renderAsMarkdown(buffer);
				buffer.append("\n");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<p>");
				text.renderAsHtml(buffer);
				buffer.raw("</p>");
			}
		};
	}
	
	public static Renderable strikeThrough(Renderable text) {
		return new Renderable() {
			
			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append("~~");
				text.renderAsMarkdown(buffer);
				buffer.append("~~");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<del>");
				text.renderAsHtml(buffer);
				buffer.raw("</del>");
			}
		};
	}

	public static Renderable link(String text, String url) {
		return new Renderable() {

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

	public static Renderable lineBreak() {
		return new Renderable() {

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '\n') {
					// 2 spaces and then new line would create a line break in text
					buffer.append("  ");
				}
				buffer.append("\n");
			}

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				buffer.raw("<br>");
			}
		};
	}

	public static Renderable bold(String text) {
		return bold(text(text));
	}

	public static Renderable bold(Renderable text) {

		return new Renderable() {

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

	public static Renderable text(String text) {
		return new Renderable() {
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
	
	public static Renderable lazy(Supplier<Renderable> supplier) {
		return new Renderable() {
			
			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				supplier.get().renderAsMarkdown(buffer);
			}
			
			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
				supplier.get().renderAsHtml(buffer);
			}
		};
	}

	public static Renderable fromClasspath(final Class<?> klass, final String resourcePath) {
		return Renderables.lazy(() -> {
			String html = getText(klass, resourcePath, ".html");
			String markdown = getText(klass, resourcePath, ".md");
			if (html==null && markdown==null) {
				return NO_DESCRIPTION;
			} else {
				return new Renderable() {
					
					@Override
					public void renderAsMarkdown(StringBuilder buffer) {
						if (markdown!=null) {
							buffer.append(markdown);
						} else {
							buffer.append(getHtmlToMarkdownConverter().convert(html));
						}
					}
		
					@Override
					public void renderAsHtml(HtmlBuffer buffer) {
						if (html!=null) {
							buffer.raw(html);
						} else {
							//TODO: proper conversion to html
							buffer.raw("<pre>");
							buffer.raw(markdown);
							buffer.raw("</pre>");
						}
					}
		
				};

			}
		});
	}

	private static String getText(final Class<?> klass, String resourcePath, String extension) {
		if (extension!=null) {
			resourcePath = resourcePath + extension;
		}
		try {
			InputStream stream = klass.getResourceAsStream(resourcePath);
			if (stream != null) {
				return IOUtil.toString(stream);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return null;
	}

	private static class ConcatRenderables implements Renderable {

		private Renderable[] pieces;

		ConcatRenderables(Renderable... pieces) {
			this.pieces = pieces;
		}

		public ConcatRenderables(List<Renderable> pieces) {
			this(pieces.toArray(new Renderable[pieces.size()]));
		}

		@Override
		public void renderAsHtml(HtmlBuffer buffer) {
			for (Renderable hoverInfo : pieces) {
				hoverInfo.renderAsHtml(buffer);
			}
		}

		@Override
		public void renderAsMarkdown(StringBuilder buffer) {
			for (Renderable hoverInfo : pieces) {
				hoverInfo.renderAsMarkdown(buffer);
			}
		}

	}
}
