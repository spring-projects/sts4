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
package org.springframework.ide.eclipse.boot.launch.properties;

import org.eclipse.core.expressions.PropertyTester;

import com.google.common.base.Objects;

public class EmbeddedEditorPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		switch (property) {
		case "context":
			if (receiver instanceof EmbeddedEditor) {
				return Objects.equal(expectedValue, ((EmbeddedEditor)receiver).getContext());
			}
			return false;
		}
		return false;
	}

}
