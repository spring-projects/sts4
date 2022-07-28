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

import org.springframework.ide.vscode.boot.java.rewrite.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public interface RecipeSpringJavaProblemDescriptor extends RecipeCodeActionDescriptor {
	
	ProblemType getProblemType();
	
}
