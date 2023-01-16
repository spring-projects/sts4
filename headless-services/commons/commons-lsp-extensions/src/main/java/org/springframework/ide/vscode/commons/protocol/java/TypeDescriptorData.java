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
import java.util.Objects;

public class TypeDescriptorData extends MemberData {
	
	private String fqName;
	private boolean clazz;
	private boolean annotation;
	private boolean interfaze;
	private boolean enam;
	private boolean record;
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

	public boolean isRecord() {
		return record;
	}

	public void setRecord(boolean record) {
		this.record = record;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(superInterfaceNames);
		result = prime * result + Objects.hash(annotation, clazz, enam, fqName, interfaze, record, superClassName);
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
		return annotation == other.annotation && clazz == other.clazz && enam == other.enam
				&& Objects.equals(fqName, other.fqName) && interfaze == other.interfaze && record == other.record
				&& Objects.equals(superClassName, other.superClassName)
				&& Arrays.equals(superInterfaceNames, other.superInterfaceNames);
	}

}
