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

import java.util.List;

import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;

public class EmbeddedLanguageSnippetWithPrefixAndSuffix implements EmbeddedLanguageSnippet {
	
	private final EmbeddedLanguageSnippet snippet;
	private final int start;
	private final int end;

	public EmbeddedLanguageSnippetWithPrefixAndSuffix(EmbeddedLanguageSnippet snippet, int start, int end) {
		this.snippet = snippet;
		this.start = start;
		this.end = end; 
	}
	
	@Override
	public String getText() {
		return snippet.getText().substring(start, end);
	}

	@Override
	public List<IRegion> toJavaRanges(IRegion range) {
		return snippet.toJavaRanges(new Region(range.getOffset() + start, range.getLength()));
	}

	@Override
	public int toJavaOffset(int offset) {
		return snippet.toJavaOffset(offset + start);
	}

}
