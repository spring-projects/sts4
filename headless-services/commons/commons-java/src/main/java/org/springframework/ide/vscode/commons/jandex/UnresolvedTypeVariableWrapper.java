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

import org.jboss.jandex.UnresolvedTypeVariable;
import org.springframework.ide.vscode.commons.java.IUnresolvedTypeVariable;

final class UnresolvedTypeVariableWrapper extends TypeWrapper<UnresolvedTypeVariable> implements IUnresolvedTypeVariable {
	
	UnresolvedTypeVariableWrapper(UnresolvedTypeVariable type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}
	
}
