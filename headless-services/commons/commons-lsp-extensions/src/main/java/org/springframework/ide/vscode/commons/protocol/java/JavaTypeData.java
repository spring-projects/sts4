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

import java.util.LinkedHashMap;

public class JavaTypeData {
	
	public enum JavaTypeKind {
		CLASS,
		ARRAY,
		PARAMETERIZED,
		TYPE_VARIABLE,
		WILDCARD,
		VOID,
		INT,
		CHAR,
		BOOLEAN,
		FLOAT,
		BYTE,
		DOUBLE,
		LONG,
		SHORT,
		UNRESOLVED
	}
	
	private JavaTypeKind kind;
	private String name;
	private LinkedHashMap<String, Object> extras;
	
	public JavaTypeKind getKind() {
		return kind;
	}
	public void setKind(JavaTypeKind kind) {
		this.kind = kind;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LinkedHashMap<String, Object> getExtras() {
		return extras;
	}
	public void setExtras(LinkedHashMap<String, Object> extras) {
		this.extras = extras;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extras == null) ? 0 : extras.hashCode());
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaTypeData other = (JavaTypeData) obj;
		if (extras == null) {
			if (other.extras != null)
				return false;
		} else if (!extras.equals(other.extras))
			return false;
		if (kind != other.kind)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
