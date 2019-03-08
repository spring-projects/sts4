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
package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.eclipse.lsp4j.TextEdit;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

public class ReplaceStringData {

	private String uri;
	private TextEdit edit;

	public ReplaceStringData() {}

	public ReplaceStringData(DocumentRegion target, String newText) throws BadLocationException {
		this.uri = target.getDocument().getUri();
		this.edit = new TextEdit(target.asRange(), newText);
	}

	public TextEdit getEdit() {
		return edit;
	}
	public void setEdit(TextEdit edit) {
		this.edit = edit;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Override
	public String toString() {
		return "ReplaceStringData [uri=" + uri + ", edit=" + edit + "]";
	}
}
