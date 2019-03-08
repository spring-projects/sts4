/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.quickfix;

import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Missing Property Quickfix data
 *
 * @author Alex Boyko
 *
 */
public class MissingPropertyData {

	private TextDocumentIdentifier doc;
	private String property;

	public MissingPropertyData(TextDocumentIdentifier doc, String property) {
		super();
		this.setDoc(doc);
		this.setProperty(property);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public TextDocumentIdentifier getDoc() {
		return doc;
	}

	public void setDoc(TextDocumentIdentifier doc) {
		this.doc = doc;
	}

}
