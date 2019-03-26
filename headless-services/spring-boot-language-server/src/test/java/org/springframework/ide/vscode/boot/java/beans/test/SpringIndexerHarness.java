/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.Editor;

import com.google.common.collect.ImmutableList;

public class SpringIndexerHarness {

	public static class TestSymbolInfo {

		private String coveredText;
		private String label;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((coveredText == null) ? 0 : coveredText.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
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
			TestSymbolInfo other = (TestSymbolInfo) obj;
			if (coveredText == null) {
				if (other.coveredText != null)
					return false;
			} else if (!coveredText.equals(other.coveredText))
				return false;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}



		@Override
		public String toString() {
			return "["+coveredText+"] => " + label;
		}

		public TestSymbolInfo(String coveredText, String label) {
			this.coveredText = coveredText;
			this.label = label;
		}

	}

	private static final Comparator<Range> RANGE_COMPARATOR = Editor.RANGE_COMPARATOR;
	private static final Comparator<SymbolInformation> SYMBOL_COMPARATOR = new Comparator<SymbolInformation>() {

		@Override
		public int compare(SymbolInformation o1, SymbolInformation o2) {
			int r = o1.getLocation().getUri().compareTo(o2.getLocation().getUri());
			if (r!=0) return r;

			r = RANGE_COMPARATOR.compare(o1.getLocation().getRange(), o2.getLocation().getRange());
			if (r!=0) return r;

			return o1.getName().compareTo(o2.getName());
		}

	};

	public static TestSymbolInfo symbol(String coveredText, String label) {
		return new TestSymbolInfo(coveredText, label);
	}

	public static void assertDocumentSymbols(SpringSymbolIndex indexer, String documentUri, TestSymbolInfo... expectedSymbols) throws Exception {
		List<TestSymbolInfo> actualSymbols = getSymbolsInFile(indexer, documentUri);
		assertEquals(symbolsString(Arrays.asList(expectedSymbols)), symbolsString(actualSymbols));
	}

	private static String symbolsString(List<TestSymbolInfo> symbols) {
		StringBuilder buf = new StringBuilder();
		for (TestSymbolInfo s : symbols) {
			buf.append(s+"\n");
		}
		return buf.toString();
	}

	public static List<TestSymbolInfo> getSymbolsInFile(SpringSymbolIndex indexer, String docURI) throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docURI);
		if (symbols!=null) {
			symbols = new ArrayList<>(symbols);
			Collections.sort(symbols, SYMBOL_COMPARATOR);
			TextDocument doc = new TextDocument(docURI, LanguageId.JAVA, 1, IOUtils.toString(new URI(docURI)));
			ImmutableList.Builder<TestSymbolInfo> symbolInfos = ImmutableList.builder();
			for (SymbolInformation s : symbols) {
				int start = doc.toOffset(s.getLocation().getRange().getStart());
				int end = doc.toOffset(s.getLocation().getRange().getEnd());
				symbolInfos.add(new TestSymbolInfo(doc.textBetween(start, end), s.getName()));
			}
			return symbolInfos.build();
		}
		return ImmutableList.of();
	}

}
