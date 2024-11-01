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
package org.springframework.ide.vscode.commons.languageserver.semantic.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.lsp4j.SemanticTokensLegend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class SemanticTokensUtils {
	
	private static Logger log = LoggerFactory.getLogger(SemanticTokensUtils.class);
	
	private static int getSemanticTokenTypeIndex(SemanticTokensLegend legend, String tokenType) {
		return legend.getTokenTypes().indexOf(tokenType);
	}

	private static int getSemanticTokenModifiersFlags(SemanticTokensLegend legend, String[] modifiers) {
		int flags = 0;
		for (String modifier : modifiers) {
			int bit = legend.getTokenModifiers().indexOf(modifier);
			if (bit < 0) {
				throw new IllegalArgumentException("Cannot find modifier '%s' in the legend: %s".formatted(modifier, legend));
			} else {
				flags |= (1 << bit);
			}
		}
		return flags;
	}
	
	public static List<Integer> mapTokensDataToLsp(List<SemanticTokenData> tokensData, SemanticTokensLegend legend, Function<Integer, Integer> getLineNumber, Function<Integer, Integer> getColumnNumber) {
		// Sort tokens by start offset
		Collections.sort(tokensData);
		
		List<Integer> data = new ArrayList<>(tokensData.size() * 5);
		
		// Encode relative positions for tokens
		int previousLine = 0;
		int previousColumn = 0;
		for (SemanticTokenData tokenData : tokensData) {
			int currentLine = getLineNumber.apply(tokenData.getStart());
			int currentColumn = getColumnNumber.apply(tokenData.getStart());
			data.add(currentLine - previousLine);
			data.add(currentLine == previousLine ? currentColumn - previousColumn : currentColumn);
			data.add(tokenData.getEnd() -  tokenData.getStart());
			data.add(SemanticTokensUtils.getSemanticTokenTypeIndex(legend, tokenData.type()));
			data.add(SemanticTokensUtils.getSemanticTokenModifiersFlags(legend, tokenData.modifiers()));
			previousLine = currentLine;
			previousColumn = currentColumn;
		}
		
		return data;
	}
	
	public static List<Integer> mapTokensDataToLsp(TextDocument doc, SemanticTokensLegend legend,
			List<SemanticTokenData> tokensData) {
		
		// Sort tokens by start offset
		Collections.sort(tokensData);
		
		List<Integer> data = new ArrayList<>(tokensData.size() * 5);
		
		// Encode relative positions for tokens
		int previousLine = 0;
		int previousColumn = 0;
		for (SemanticTokenData tokenData : tokensData) {
			try {
				int currentLine = doc.getLineOfOffset(tokenData.getStart());
				int currentColumn = tokenData.getStart() - doc.getLineOffset(currentLine);
				data.add(currentLine - previousLine);
				data.add(currentLine == previousLine ? currentColumn - previousColumn : currentColumn);
				data.add(tokenData.getEnd() -  tokenData.getStart());
				data.add(SemanticTokensUtils.getSemanticTokenTypeIndex(legend, tokenData.type()));
				data.add(SemanticTokensUtils.getSemanticTokenModifiersFlags(legend, tokenData.modifiers()));
				previousLine = currentLine;
				previousColumn = currentColumn;
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		
		return data;
	}

}
