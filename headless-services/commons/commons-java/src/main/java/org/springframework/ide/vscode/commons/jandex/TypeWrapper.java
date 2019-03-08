/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

class TypeWrapper<T> {
	
	private T type;
	
	TypeWrapper(T type) {
		this.type = type;
	}
	
	T getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeWrapper) {
			return type.equals(((TypeWrapper<T>)obj).type);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return type.toString();
	}

}
