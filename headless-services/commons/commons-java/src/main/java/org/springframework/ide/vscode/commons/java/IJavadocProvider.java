/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public interface IJavadocProvider {

	IJavadoc getJavadoc(IType type);

	IJavadoc getJavadoc(IField field);

	IJavadoc getJavadoc(IMethod method);

	IJavadoc getJavadoc(IAnnotation annotation);

	final static IJavadocProvider NULL = new IJavadocProvider() {

		@Override
		public IJavadoc getJavadoc(IType type) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IField field) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IMethod method) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IAnnotation method) {
			return null;
		}

	};

}
