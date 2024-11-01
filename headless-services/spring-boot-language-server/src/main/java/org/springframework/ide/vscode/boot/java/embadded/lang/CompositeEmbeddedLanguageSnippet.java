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
package org.springframework.ide.vscode.boot.java.embadded.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;

public class CompositeEmbeddedLanguageSnippet implements EmbeddedLanguageSnippet {
	
	private TreeMap<Region, EmbeddedLanguageSnippet> snippetParts;
	private String text;
	
	public CompositeEmbeddedLanguageSnippet(Collection<EmbeddedLanguageSnippet> snippets) {
		snippetParts = new TreeMap<>(new Comparator<IRegion>() {

			@Override
			public int compare(IRegion o1, IRegion o2) {
				return o1.getOffset() - o2.getOffset();
			}
			
		});
		StringBuilder sb = new StringBuilder();
		int offset = 0;
		for (EmbeddedLanguageSnippet s : snippets) {
			String t = s.getText();
			sb.append(t);
			snippetParts.putIfAbsent(new Region(offset, t.length()), s);
			offset += t.length();
		}
		this.text = sb.toString();
	}

	@Override
	public List<IRegion> toJavaRanges(IRegion range) {
		List<IRegion> javaRegions = new ArrayList<>(); 
		Entry<Region, EmbeddedLanguageSnippet> startEntry = snippetParts.floorEntry(new Region(range.getStart(), 0));
		Entry<Region, EmbeddedLanguageSnippet> endEntry = snippetParts.floorEntry(new Region(range.getEnd(), 0));

		
		if (startEntry.getKey().equals(endEntry.getKey())) {
			// The Range is within the same snippet piece
			javaRegions.addAll(startEntry.getValue().toJavaRanges(new Region(range.getOffset() - startEntry.getKey().getOffset(), range.getLength())));
		} else {
			// The range spans a number of snippets
			// starting snippet - part of it should be included
			int offset = range.getOffset() - startEntry.getKey().getOffset();
			int length = startEntry.getKey().getLength() - offset;
			if (length > 0) {
				javaRegions.addAll(startEntry.getValue().toJavaRanges(new Region(offset, length)));
			}
			// snippet parts entirely in the request range - entire snippet range is included
			for (Entry<Region, EmbeddedLanguageSnippet> e  : snippetParts.subMap(startEntry.getKey(), false, endEntry.getKey(), false).entrySet()) {
				javaRegions.addAll(e.getValue().toJavaRanges(new Region(0, e.getKey().getLength())));
			}
			// ending snippet - part of it should be included
			offset = 0;
			length = range.getEnd() - endEntry.getKey().getOffset();
			if (length > 0) {
				javaRegions.addAll(endEntry.getValue().toJavaRanges(new Region(offset, length)));
			}
			
		}
		return javaRegions;
	}

	@Override
	public int toJavaOffset(int offset) {
		Entry<Region, EmbeddedLanguageSnippet> entry = snippetParts.floorEntry(new Region(offset, 0));
		return entry.getValue().toJavaOffset(offset - entry.getKey().getOffset());
	}

	@Override
	public String getText() {
		return text;
	}

}
