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

import org.jboss.jandex.WildcardType;
import org.springframework.ide.vscode.commons.java.IWildcardType;

final class WildcardTypeWrapper extends TypeWrapper<WildcardType> implements IWildcardType {
	
	WildcardTypeWrapper(WildcardType type) {
		super(type);
	}

	@Override
	public String name() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
