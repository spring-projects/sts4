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

import org.springframework.ide.vscode.boot.java.SpringJavaProblemType;
import org.springframework.ide.vscode.boot.java.rewrite.RecipeScope;
import org.springframework.ide.vscode.boot.java.rewrite.codeaction.BeanMethodsNotPublicCodeAction;

public class BeanMethodNotPublicProblem extends BeanMethodsNotPublicCodeAction implements RecipeSpringJavaProblemDescriptor {

	@Override
	public RecipeScope[] getScopes() {
		return new RecipeScope[] { RecipeScope.NODE };
	}

	@Override
	public SpringJavaProblemType getProblemType() {
		return SpringJavaProblemType.JAVA_PUBLIC_BEAN_METHOD;
	}

}
