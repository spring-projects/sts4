/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

public class DocumentDescriptor {
	
	private final String docURI;
	private final long lastModified;
	
	public DocumentDescriptor(String docURI, long lastModified) {
		super();
		this.docURI = docURI;
		this.lastModified = lastModified;
	}
	
	public String getDocURI() {
		return docURI;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
}
