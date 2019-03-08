/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

public class ProblemTypes {

	/**
	 * Creates a new problem type. The newly created problem type is not 'equals' to any other
	 * problem type.
	 *
	 * @param defaultSeverity
	 * @param typeName A unique name for this problem type. Note that it is the caller's responsibility that the typeName is unique.
	 *                 If this method is called more than once with identical typeName's it makes no attempts to veify that
	 *                 the name is uniquer, or to return the same object for the same typeName.
	 * @return A newly create problem type.
	 */
	public static ProblemType create(String typeName, ProblemSeverity defaultSeverity) {
		return new ProblemType() {
			@Override
			public String toString() {
				return typeName;
			}
			@Override
			public ProblemSeverity getDefaultSeverity() {
				return defaultSeverity;
			}
			@Override
			public String getCode() {
				return typeName;
			}
		};
	}

}
