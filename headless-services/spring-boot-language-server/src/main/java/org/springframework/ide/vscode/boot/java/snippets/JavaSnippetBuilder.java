/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.snippets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.IndentUtil;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;

import com.google.common.base.Supplier;

/**
 * Respobsible for converting eclipse-like template string into lsp snippet text.
 * @author Kris De Volder
 */
public class JavaSnippetBuilder{

	private Supplier<SnippetBuilder> snippetBuilderFactory;

	private static final Pattern PLACE_HOLDER = Pattern.compile("\\$\\{(.+?)\\}");

	public JavaSnippetBuilder(Supplier<SnippetBuilder> snippetBuilderFactory) {
		this.snippetBuilderFactory = snippetBuilderFactory;
	}

	public DocumentEdits createEdit(DocumentRegion query, String template) {
		IDocument doc = query.getDocument();
		IndentUtil indentUtil = new IndentUtil(doc);

		DocumentEdits edit = new DocumentEdits(doc);
		String snippet = createSnippet(template);

		String referenceIndent = indentUtil.getReferenceIndent(query.getStart(), doc);

		if (!referenceIndent.contains("\t")) {
			snippet = indentUtil.covertTabsToSpace(snippet);
		}
		String indentedSnippet = indentUtil.applyIndentation(snippet, referenceIndent);

		edit.replace(query.getStart(), query.getEnd(), indentedSnippet);
		return edit;
	}

	private String createSnippet(String template) {
		Matcher matcher = PLACE_HOLDER.matcher(template);
		int start = 0;
		SnippetBuilder snippet = snippetBuilderFactory.get();
		while (matcher.find(start)) {
			int matchStart = matcher.start();
			snippet.text(template.substring(start, matchStart));
			int matchEnd = matcher.end();
			String placeHolderImage = template.substring(matcher.start(1), matcher.end(1));
			int colon = placeHolderImage.indexOf(':');
			String id, value;
			if (colon>=0) {
				id = placeHolderImage.substring(0, colon);
				value = placeHolderImage.substring(colon+1);
			} else {
				id = placeHolderImage;
				value = id;
			}
			snippet.placeHolder(id, value);
			start = matchEnd;
		}
		snippet.text(template.substring(start));
		return snippet.build().toString();
	}

}
