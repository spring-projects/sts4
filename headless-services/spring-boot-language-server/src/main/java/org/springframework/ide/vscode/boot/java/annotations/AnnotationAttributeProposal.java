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

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(detail, filterText, label);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationAttributeProposal other = (AnnotationAttributeProposal) obj;
		return Objects.equals(detail, other.detail) && Objects.equals(filterText, other.filterText)
				&& Objects.equals(label, other.label);
	}

}
