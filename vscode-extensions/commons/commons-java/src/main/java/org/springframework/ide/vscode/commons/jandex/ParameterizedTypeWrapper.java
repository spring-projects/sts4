/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

import static org.springframework.ide.vscode.commons.jandex.Wrappers.wrap;
import java.util.stream.Stream;

import org.jboss.jandex.ParameterizedType;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IParameterizedType;

final class ParameterizedTypeWrapper extends TypeWrapper<ParameterizedType> implements IParameterizedType {
	
	ParameterizedTypeWrapper(ParameterizedType type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}

	@Override
	public IJavaType owner() {
		return wrap(getType().owner());
	}

	@Override
	public Stream<IJavaType> arguments() {
		return getType().arguments().stream().map(Wrappers::wrap);
	}
	
}
