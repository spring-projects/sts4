package org.springframework.ide.vscode.application.properties.hover;

import java.util.Collection;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.HtmlBuffer;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.StringUtil;

public abstract class AbstractPropertyRenderableProvider {
		
	/**
	 * Fake host name that is used in 'action link' urls so that we can
	 * recognize them as such.
	 */
	private static final String ACTION_HOST = "action";
	
	Renderable getRenderable() {
		return new Renderable() {

			@Override
			public void renderAsHtml(HtmlBuffer buffer) {
//				JavaTypeLinks jtLinks = new JavaTypeLinks(this);

				renderId(buffer);

				String type = getType();
				if (type==null) {
					type = Object.class.getName();
				}
//				jtLinks.javaTypeLink(html, getJavaProject(), type);
				actionLink(buffer, type);

				String deflt = formatDefaultValue(getDefaultValue());
				if (deflt!=null) {
					buffer.raw("<br><br>");
					buffer.text("Default: ");
					buffer.raw("<i>");
					buffer.text(deflt);
					buffer.raw("</i>");
				}

				if (isDeprecated()) {
					buffer.raw("<br><br>");
					String reason = getDeprecationReason();
					if (StringUtil.hasText(reason)) {
						buffer.bold("Deprecated: ");
						buffer.text(reason);
					} else {
						buffer.bold("Deprecated!");
					}
				}

				Renderable description = getDescription();
				if (description!=null) {
					buffer.raw("<br><br>");
					buffer.p(description.toHtml());
				}

			}

			@Override
			public void renderAsMarkdown(StringBuilder buffer) {
				buffer.append(Renderables.getHtmlToMarkdownConverter().convert(toHtml()));
			}
			
		};
	}
	
	final protected void renderId(HtmlBuffer html) {
		boolean deprecated = isDeprecated();
		String tag = deprecated ? "s" : "b";
		String replacement = getDeprecationReplacement();

		html.raw("<"+tag+">");
			html.text(getId());
		html.raw("</"+tag+">");
		if (StringUtil.hasText(replacement)) {
			html.text(" -> "+ replacement);
		}
		html.raw("<br>");
	}

	protected abstract Object getDefaultValue();
	protected abstract IJavaProject getJavaProject();
	protected abstract Renderable getDescription();
	protected abstract String getType();
	protected abstract String getDeprecationReason();
	protected abstract String getId();
	protected abstract String getDeprecationReplacement();
	protected abstract boolean isDeprecated();

	public static String formatDefaultValue(Object defaultValue) {
		if (defaultValue!=null) {
			if (defaultValue instanceof String) {
				return (String) defaultValue;
			} else if (defaultValue instanceof Number) {
				return ((Number)defaultValue).toString();
			} else if (defaultValue instanceof Boolean) {
				return Boolean.toString((Boolean) defaultValue);
			} else if (defaultValue instanceof Object[]) {
				return StringUtil.arrayToCommaDelimitedString((Object[]) defaultValue);
			} else if (defaultValue instanceof Collection<?>) {
				return StringUtil.collectionToCommaDelimitedString((Collection<?>) defaultValue);
			} else {
				//no idea what it is but try 'toString' and hope for the best
				return defaultValue.toString();
			}
		}
		return null;
	}

	/**
	 * Creates an 'action' link and adds it to the html buffer. When the user clicks the given
	 * link then the provided runnable is to be executed.
	 */
	public void actionLink(HtmlBuffer html, String displayString/*, Runnable runnable*/) {
//		String actionId = registerAction(runnable);
		html.raw("<a href=\"http://"+ACTION_HOST+"/");
		html.url("action-id");
		html.raw("\">");
		html.text(displayString);
		html.raw("</a>");
	}

}
