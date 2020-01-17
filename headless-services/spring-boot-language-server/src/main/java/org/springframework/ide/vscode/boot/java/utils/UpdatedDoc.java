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

import java.util.function.Supplier;

public class UpdatedDoc {
	
	private final String docURI;
	private final long lastModified;
	private final Supplier<String> content;
	
	public UpdatedDoc(String docURI, long lastModified, Supplier<String> content) {
		super();
		this.docURI = docURI;
		this.lastModified = lastModified;
		this.content = content;
	}
	
	public String getDocURI() {
		return docURI;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public Supplier<String> getContent() {
		return content;
	}

}
