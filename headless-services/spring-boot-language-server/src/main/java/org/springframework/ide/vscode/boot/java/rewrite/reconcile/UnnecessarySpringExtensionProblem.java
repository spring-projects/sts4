/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.rewrite.codeaction.UnnecessarySpringExtensionCodeAction;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;

public class UnnecessarySpringExtensionProblem extends UnnecessarySpringExtensionCodeAction
		implements RecipeSpringJavaProblemDescriptor {

	@Override
	public RecipeScope[] getScopes() {
		return new RecipeScope[] { RecipeScope.FILE };
	}

	@Override
	public Boot2JavaProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_TEST_SPRING_EXTENSION;
	}

}
