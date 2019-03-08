/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

public class MemberData extends JavaElementData {
	
	private String declaringType;	
	private int flags;

	public String getDeclaringType() {
		return declaringType;
	}

	public void setDeclaringType(String declaringType) {
		this.declaringType = declaringType;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((declaringType == null) ? 0 : declaringType.hashCode());
		result = prime * result + flags;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberData other = (MemberData) obj;
		if (declaringType == null) {
			if (other.declaringType != null)
				return false;
		} else if (!declaringType.equals(other.declaringType))
			return false;
		if (flags != other.flags)
			return false;
		return true;
	}

}
