/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

public class HighlightParams {

	//TODO: Identical copy of this exists in org.springframework.ide.vscode.commons.languageserver.HighlightParams
	// But because that is only built in plain maven jar (not osgi) we can't use it. We should find a solution
	// for this somehow (i.e. build a version of some of our 'plain maven' jars as osgi bundles and publish on
	// a update site.

	private VersionedTextDocumentIdentifier doc;
    private List<CodeLens> codeLenses;

	public HighlightParams() {
	}

	public HighlightParams(VersionedTextDocumentIdentifier doc, List<CodeLens> codeLenses) {
		super();
		this.doc = doc;
		this.codeLenses = codeLenses;
	}
	public VersionedTextDocumentIdentifier getDoc() {
		return doc;
	}
	public void setDoc(VersionedTextDocumentIdentifier doc) {
		this.doc = doc;
	}

	public List<CodeLens> getCodeLenses() {
		return codeLenses;
	}

	public void setCodeLenses(List<CodeLens> codeLenses) {
		this.codeLenses = codeLenses;
	}

}
