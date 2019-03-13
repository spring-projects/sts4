/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.spring.tools.boot.java.ls;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class HighlightParams {
	
	//TODO: Identical copy of this exists in org.springframework.ide.vscode.commons.languageserver.HighlightParams
	// But because that is only built in plain maven jar (not osgi) we can't use it. We should find a solution
	// for this somehow (i.e. build a version of some of our 'plain maven' jars as osgi bundles and publish on
	// a update site.

	private TextDocumentIdentifier doc;
	private List<CodeLens> codeLenses;

	public HighlightParams() {
	}

	public HighlightParams(TextDocumentIdentifier doc, List<CodeLens> codeLenses) {
		super();
		this.doc = doc;
		this.codeLenses = codeLenses;
	}
	public TextDocumentIdentifier getDoc() {
		return doc;
	}
	public void setDoc(TextDocumentIdentifier doc) {
		this.doc = doc;
	}
	public List<CodeLens> getCodeLenses() {
		return codeLenses;
	}
	public void setCodeLenses(List<CodeLens> codeLens) {
		this.codeLenses = codeLens;
	}

	@Override
	public String toString() {
		return "HighlightParams [doc=" + doc + ", ranges=" + codeLenses + "]";
	}
}
