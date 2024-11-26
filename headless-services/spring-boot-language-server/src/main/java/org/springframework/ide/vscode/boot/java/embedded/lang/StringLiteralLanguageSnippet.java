/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.embedded.lang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;

public class StringLiteralLanguageSnippet implements EmbeddedLanguageSnippet {
	
	final private int literalOffset;
	final private String escapedValue;
	final private String literalValue;
	
	private transient int startOffset;
	/*
	 *  Each region represents a tuple where:
	 *  - offset is the position in the snippet text.
	 *  - length is the number of chars symbol at that location takes in the escaped snippet text
	 */
	private transient List<Region> specialRegions;
	
	public StringLiteralLanguageSnippet(StringLiteral literal) {
		this(literal.getEscapedValue(), literal.getLiteralValue(), literal.getStartPosition());
	}
	
	public StringLiteralLanguageSnippet(String escapedValue, String literalValue, int literalOffset) {
		this.escapedValue = escapedValue;
		this.literalValue = literalValue;
		this.literalOffset = literalOffset;
	}

	@Override
	public int toJavaOffset(int offset) {
		if (specialRegions == null) {
			specialRegions = createMappings();
		}
		int mappedOffset = literalOffset + startOffset + offset;
		for (Region r : specialRegions) {
			if (offset > r.getStart()) {
				// length - 1 because 1 is added implicitly from '+ offset' above. Length of char escaped or not is >= 1
				mappedOffset += (r.getLength() - 1); 
			} else {
				break;
			}
		}
		return mappedOffset;
	}
	
	@Override
	public List<IRegion> toJavaRanges(IRegion range) {
		int start = toJavaOffset(range.getStart());
		int end = toJavaOffset(range.getEnd());
		return List.of(new Region(start, end - start));
	}

	@Override
	public String getText() {
		return literalValue;
	}
	
	private List<Region> createMappings() {
		List<Region> regions = new ArrayList<>();
		if (literalValue.length() > 0) {
			int current = 1; // skip over opening "
			startOffset = current;
			boolean escaping = false;
			// <= to include the next char after the end of the literal actual value
			for (int i = 0; i <= literalValue.length();) {
				if (escaping) {
					regions.add(new Region(i, 2));
					i++;
					current++;
					escaping = false;
				} else {
					if (escapedValue.charAt(current) == '\\') {
						escaping = escapedValue.charAt(current) == '\\';
						current++;
					} else {
						i++;
						current++;
					}
				}
			}
		}
		return regions;
	}

}
