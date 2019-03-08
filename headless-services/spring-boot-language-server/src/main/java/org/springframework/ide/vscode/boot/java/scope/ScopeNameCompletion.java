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
package org.springframework.ide.vscode.boot.java.scope;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class ScopeNameCompletion {
	
	private final String label;
	private final String detail;
	private final Renderable documentation;
	private final CompletionItemKind kind;
	private final String value;
	
	public ScopeNameCompletion(String value, String label, String detail, Renderable documentation, CompletionItemKind kind) {
		super();
		this.value = value;
		this.label = label;
		this.detail = detail;
		this.documentation = documentation;
		this.kind = kind;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public CompletionItemKind getKind() {
		return kind;
	}
	
	public Renderable getDocumentation() {
		return documentation;
	}
	
	public String getValue() {
		return this.value;
	}

}
