/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

import org.jboss.jandex.ClassType;
import org.springframework.ide.vscode.commons.java.IClassType;

final class ClassTypeWrapper extends TypeWrapper<ClassType> implements IClassType {

	ClassTypeWrapper(ClassType type) {
		super(type);
	}

	@Override
	public String name() {
		return "L" + getType().name().toString().replace('.', '/') + ";";
	}

	@Override
	public String getFQName() {
		return getType().name().toString();
	}

}
