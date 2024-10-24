/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.annotations;

/**
 * @author Martin Lippert
 */
public class AnnotationAttributeProposal {
	
	private static final String EMPTY_DETAIL = "";

	private final String filterText;
	private final String label;
	private final String detail;
	
	public AnnotationAttributeProposal(String label) {
		this(label, label, label);
	}
	
	public AnnotationAttributeProposal(String label, String detail) {
		this(label, detail, label);
	}
	
	public AnnotationAttributeProposal(String label, String detail, String filterText) {
		this.label = label;
		this.detail = detail == null ? EMPTY_DETAIL : detail;
		this.filterText = filterText;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDetail() {
		return detail;
	}
	
	public String getFilterText() {
		return filterText;
	}

}
