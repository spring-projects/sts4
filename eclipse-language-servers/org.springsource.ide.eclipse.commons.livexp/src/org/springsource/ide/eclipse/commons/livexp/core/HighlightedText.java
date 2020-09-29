/*******************************************************************************
 * Copyright (c) 2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * 
 * Builds a text with different segments that are highlighted
 * 
 */
public class HighlightedText {

	
	private List<TextSegment> segments = new ArrayList<>();

	public HighlightedText appendHighlight(String text) {
		append(text, Style.HIGHLIGHT);
		return this;
	}

	public HighlightedText appendPlain(String text) {
		append(text, Style.PLAIN);
		return this;
	}

	
	private synchronized void append(String text, Style style) {
		if (text != null) {
			if (style == null) {
				style = Style.PLAIN;
			}
			segments.add(new TextSegment(text, style));
		}
	}

	public synchronized List<TextSegment> build() {
		return ImmutableList.copyOf(segments);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((segments == null) ? 0 : segments.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HighlightedText other = (HighlightedText) obj;
		if (segments == null) {
			if (other.segments != null)
				return false;
		} else if (!segments.equals(other.segments))
			return false;
		return true;
	}

	public static class Style {
		
		public static final Style HIGHLIGHT = new Style("highlight");
		public static final Style PLAIN = new Style("plain");


		private final String name;

		public Style(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

	}

	public static class TextSegment {
		private final Style style;
		private final String text;

		public TextSegment(String text, Style style) {
			this.text = text;
			this.style = style;
		}

		public String getText() {
			return this.text;
		}

		public Style getStyle() {
			return this.style;
		}
	}

	public static HighlightedText plain(String text) {
		return new HighlightedText().appendPlain(text);
	}

	public static HighlightedText create() {
		return new HighlightedText();
	}

}
