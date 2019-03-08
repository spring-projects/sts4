/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

public class HighlightParams {

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
