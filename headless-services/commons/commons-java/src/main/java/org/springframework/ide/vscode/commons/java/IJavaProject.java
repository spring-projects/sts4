/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public interface IJavaProject extends IJavaElement {
	
	IClasspath getClasspath();

	@Override
	default String getElementName() {
		return getClasspath().getName();
	}

	@Override
	default IJavadoc getJavaDoc() {
		return null;
	}

	@Override
	default boolean exists() {
		return getClasspath().exists();
	}
	
}
