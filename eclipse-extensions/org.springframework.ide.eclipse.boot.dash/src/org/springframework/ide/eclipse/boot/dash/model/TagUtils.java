/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * Utilities for generating tags array from text, generating string from tags array and others
 *
 * @author Alex Boyko
 *
 */
public class TagUtils {

	public static final char SEPARATOR_SYMBOL = ',';

	/**
	 * String separator between tags string representation
	 */
	public static final String SEPARATOR = SEPARATOR_SYMBOL + " ";

	/**
	 * Regular Expression pattern for separation string between tags in their textual representation
	 */
	public static final String SEPARATOR_REGEX = "\\s*" + SEPARATOR_SYMBOL + "\\s*";

	/**
	 * Parses text into tags
	 *
	 * @param text the string text
	 * @return array of string tags
	 */
	public static String[] parseTags(String text) {
		String s = text.trim();
		if (s.isEmpty()) {
			return new String[0];
		} else {
			String[] split = s.split(SEPARATOR_REGEX);
			if (split.length > 0) {
				ArrayList<String> sanitized = new ArrayList<>(split.length);
				for (String tag : split) {
					if (!tag.isEmpty()) {
						sanitized.add(tag);
					}
				}
				split = sanitized.toArray(new String[sanitized.size()]);
			}
			return split;
		}

	}

	/**
	 * Generates string representation for tags
	 *
	 * @param tags the tags
	 * @return the string representation of the tags
	 */
	public static String toString(Collection<String> tags) {
		return StringUtils.join(tags, SEPARATOR);
	}

	/**
	 * Generates string representation for tags
	 *
	 * @param tags the tags
	 * @return the string representation of the tags
	 */
	public static String toString(String[] tags) {
		return StringUtils.join(tags, SEPARATOR);
	}

	/**
	 * Creates styled string applying tagStyle at appropriate locations in a raw tags string.
	 */
	public static StyledString applyTagStyles(String text, Styler tagStyler) {
		StyledString styledString = new StyledString(text);
		Matcher matcher = Pattern.compile(SEPARATOR_REGEX).matcher(text);
		int position = 0;
		while (matcher.find()) {
			if (position < matcher.start()) {
				styledString.setStyle(position, matcher.start() - position, tagStyler);
			}
			position = matcher.end();
		}
		if (position < text.length()) {
			styledString.setStyle(position, text.length() - position, tagStyler);
		}
		return styledString;
	}

}
