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

import static org.springframework.ide.vscode.commons.jandex.Wrappers.wrap;

import org.jboss.jandex.ArrayType;
import org.springframework.ide.vscode.commons.java.IArrayType;
import org.springframework.ide.vscode.commons.java.IJavaType;

final class ArrayTypeWrapper extends TypeWrapper<ArrayType> implements IArrayType {
	
	ArrayTypeWrapper(ArrayType type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}

	@Override
	public int dimensions() {
		return getType().dimensions();
	}

	@Override
	public IJavaType component() {
		return wrap(getType().component());
	}
	
}
