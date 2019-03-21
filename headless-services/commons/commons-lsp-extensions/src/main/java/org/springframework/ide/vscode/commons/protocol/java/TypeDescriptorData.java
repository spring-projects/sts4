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

import java.util.Arrays;

public class TypeDescriptorData extends MemberData {
	
	private String fqName;
	private boolean clazz;
	private boolean annotation;
	private boolean interfaze;
	private boolean enam;
	private String superClassName;
	private String[] superInterfaceNames;

	public String getFqName() {
		return fqName;
	}

	public void setFqName(String fqName) {
		this.fqName = fqName;
	}

	public boolean isClass() {
		return clazz;
	}

	public void setClass(boolean clazz) {
		this.clazz = clazz;
	}

	public boolean isAnnotation() {
		return annotation;
	}

	public void setAnnotation(boolean annotation) {
		this.annotation = annotation;
	}

	public boolean isInterface() {
		return interfaze;
	}

	public void setInterface(boolean interfaze) {
		this.interfaze = interfaze;
	}

	public boolean isEnum() {
		return enam;
	}

	public void setEnum(boolean enam) {
		this.enam = enam;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public String[] getSuperInterfaceNames() {
		return superInterfaceNames;
	}

	public void setSuperInterfaceNames(String[] superInterfaceNames) {
		this.superInterfaceNames = superInterfaceNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (annotation ? 1231 : 1237);
		result = prime * result + (clazz ? 1231 : 1237);
		result = prime * result + (enam ? 1231 : 1237);
		result = prime * result + ((fqName == null) ? 0 : fqName.hashCode());
		result = prime * result + (interfaze ? 1231 : 1237);
		result = prime * result + ((superClassName == null) ? 0 : superClassName.hashCode());
		result = prime * result + Arrays.hashCode(superInterfaceNames);
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
		TypeDescriptorData other = (TypeDescriptorData) obj;
		if (annotation != other.annotation)
			return false;
		if (clazz != other.clazz)
			return false;
		if (enam != other.enam)
			return false;
		if (fqName == null) {
			if (other.fqName != null)
				return false;
		} else if (!fqName.equals(other.fqName))
			return false;
		if (interfaze != other.interfaze)
			return false;
		if (superClassName == null) {
			if (other.superClassName != null)
				return false;
		} else if (!superClassName.equals(other.superClassName))
			return false;
		if (!Arrays.equals(superInterfaceNames, other.superInterfaceNames))
			return false;
		return true;
	}

}
