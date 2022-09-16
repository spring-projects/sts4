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
package org.springframework.ide.vscode.commons.rewrite.config;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaVisitor;
import org.springframework.ide.vscode.commons.java.IJavaProject;

public interface RecipeCodeActionDescriptor {
	
	String getRecipeId();
	
	String getLabel(RecipeScope s);
	
	RecipeScope[] getScopes();
	
	JavaVisitor<ExecutionContext> getMarkerVisitor();
	
	boolean isApplicable(IJavaProject project);

	static String buildLabel(String label, RecipeScope s) {
		switch (s) {
		case FILE:
			return label + " in file";
		case PROJECT:
			return label + " in project";
		default:
			return label;
		}
	}

}
