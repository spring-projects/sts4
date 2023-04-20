/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

/**
 * @author Martin Lippert
 */
public class DataRepositoryDefinition {

	private final SimpleType type;
	private final DomainType domainType;

	public DataRepositoryDefinition(SimpleType type, DomainType domainType) {
		this.type = type;
		this.domainType = domainType;
	}

	public DomainType getDomainType() {
		return domainType;
	}
	
	public SimpleType getType() {
		return type;
	}

}
