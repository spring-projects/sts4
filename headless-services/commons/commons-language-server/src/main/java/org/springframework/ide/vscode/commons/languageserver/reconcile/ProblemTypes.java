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
	 * This method is deprecated. Look at ApplicationYamlProblemType for an example of how to define
	 * problem types to facilitate integration with preferences ui.
	 *
	 * @param defaultSeverity
	 * @param typeName A unique name for this problem type. Note that it is the caller's responsibility that the typeName is unique.
	 * @return A newly create problem type.
	 */
	@Deprecated
	public static ProblemType create(String typeName, ProblemSeverity defaultSeverity, ProblemCategory category) {
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
			@Override
			public String getDescription() {
				return typeName;
			}
			@Override
			public String getLabel() {
				return typeName;
			}
			@Override
			public ProblemCategory getCategory() {
				return category;
			}
		};
	}

}
