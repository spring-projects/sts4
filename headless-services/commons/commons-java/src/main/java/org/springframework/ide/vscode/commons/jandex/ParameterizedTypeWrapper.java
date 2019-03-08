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

import static org.springframework.ide.vscode.commons.jandex.Wrappers.wrap;
import java.util.stream.Stream;

import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.springframework.ide.vscode.commons.java.IClassType;
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
		Type owner = getType().owner();
		if (owner == null) {
			return new IClassType() {

				@Override
				public String name() {
					return getFQName();
				}

				@Override
				public String getFQName() {
					return ParameterizedTypeWrapper.this.name();
				}
			};
		} else {
			return wrap(owner);
		}
	}

	@Override
	public Stream<IJavaType> arguments() {
		return getType().arguments().stream().map(Wrappers::wrap);
	}

}
