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
package org.springframework.ide.vscode.commons.protocol.java;

/**
 * @author Martin Lippert
 */
public class JavaCodeCompleteData {
	
	public static final String PACKAGE_PROPOSAL = "package";
	public static final String CLASS_PROPOSAL = "class";
	public static final String INTERFACE_PROPOSAL = "interface";
	public static final String ENUM_PROPOSAL = "enum";
	
	private String kind;
	private String fullyQualifiedName;
	private int relevance;

	public String getFullyQualifiedName() {
		return this.fullyQualifiedName;
	}
	
	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}
	
	public String getKind() {
		return this.kind;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public int getRelevance() {
		return this.relevance;
	}
	
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}

	public boolean isInterfaceProposal() {
		return INTERFACE_PROPOSAL.equals(kind);
	}

	public boolean isEnumProposal() {
		return ENUM_PROPOSAL.equals(kind);
	}

	public boolean isPackageProposal() {
		return PACKAGE_PROPOSAL.equals(kind);
	}

	public boolean isClassProposal() {
		return CLASS_PROPOSAL.equals(kind);
	}

}
