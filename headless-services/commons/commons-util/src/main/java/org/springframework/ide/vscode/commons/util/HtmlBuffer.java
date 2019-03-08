/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Helper class to make it a little easier to create simple html page.
 *
 * @author Kris De Volder
 */
public class HtmlBuffer {

	private StringBuilder buffer = new StringBuilder();


	/**
	 * Append text, applies escaping to the text as needed.
	 */
	public void text(String text) {
		raw(convertToHTMLContent(text));
	}

	/**
	 * Append 'raw' text. Doesn't apply any escaping.
	 */
	public void raw(String rawText) {
		buffer.append(rawText);
	}

	/**
	 * Append text, applies urlencoding to the text.
	 */
	public void url(String string) {
		try {
			raw(URLEncoder.encode(string, "utf8"));
		} catch (UnsupportedEncodingException e) {
			Log.log(e);
		}
	}


	public String toString() {
		return buffer.toString();
	}

	protected void addPrologAndEpilog() {
//		HTMLPrinter.insertPageProlog(buffer, 0, getCSSStyles());
//		HTMLPrinter.addPageEpilog(buffer);
	}

//	/**
//	 * Note: copied from org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal.getCSSStyles()
//	 * Returns the style information for displaying HTML (Javadoc) content.
//	 *
//	 * @return the CSS styles
//	 * @since 3.3
//	 */
//	public static String getCSSStyles() {
//		if (fgCSSStyles == null) {
//			Bundle bundle= Platform.getBundle(JavaPlugin.getPluginId());
//			URL url= bundle.getEntry("/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
//			if (url != null) {
//				BufferedReader reader= null;
//				try {
//					url= FileLocator.toFileURL(url);
//					reader= new BufferedReader(new InputStreamReader(url.openStream()));
//					StringBuffer buffer= new StringBuffer(200);
//					String line= reader.readLine();
//					while (line != null) {
//						buffer.append(line);
//						buffer.append('\n');
//						line= reader.readLine();
//					}
//					fgCSSStyles= buffer.toString();
//				} catch (IOException ex) {
//					JavaPlugin.log(ex);
//				} finally {
//					try {
//						if (reader != null)
//							reader.close();
//					} catch (IOException e) {
//					}
//				}
//
//			}
//		}
//		String css= fgCSSStyles;
//		if (css != null) {
//			FontData fontData= JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
//			css= HTMLPrinter.convertTopLevelFont(css, fontData);
//		}
//		return css;
//	}

	public void hline() {
		raw("<hr>");
	}

	public void p(String string) {
		raw("<p>");
		text(string);
		raw("</p>");
	}

	public void bold(String string) {
		raw("<b>");
		text(string);
		raw("</b>");
	}

	/**
	 * Escapes reserved HTML characters in the given string.
	 * <p>
	 * <b>Warning:</b> Does not preserve whitespace.
	 * 
	 * @param content the input string
	 * @return the string with escaped characters
	 */
	public static String convertToHTMLContent(String content) {
		content= replace(content, '&', "&amp;"); //$NON-NLS-1$
		content= replace(content, '"', "&quot;"); //$NON-NLS-1$
		content= replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	private static String replace(String text, char c, String s) {

		int previous= 0;
		int current= text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuffer buffer= new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous= current + 1;
			current= text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

}
