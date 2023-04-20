/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class SimpleType {

	private final String packageName;
	private final String fullName;
	private final String simpleName;

	public SimpleType(ITypeBinding typeBinding) {
		if (typeBinding.getPackage() == null) {
			this.packageName = "";
		} else {
			this.packageName = typeBinding.getPackage().getName();
		}
		this.fullName = typeBinding.getQualifiedName();
		this.simpleName = typeBinding.getName();
	}
	
	public String getPackageName() {
		return packageName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getSimpleName() {
		return simpleName;
	}
	
	public boolean shouldImportType(String fqName) {
		if (fqName.startsWith("java.lang.")) {
			return false;
		}
		if (fqName.startsWith(packageName) && fqName.length() >= packageName.length() + 2 // +2: one for '.', second for capital letter
				&& fqName.charAt(packageName.length()) == '.' && Character.isUpperCase(fqName.charAt(packageName.length() + 1))) { 
			return false;
		}
		return true;

	}

}
