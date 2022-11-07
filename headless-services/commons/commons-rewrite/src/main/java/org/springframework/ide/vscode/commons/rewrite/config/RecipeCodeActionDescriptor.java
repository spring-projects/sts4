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
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public interface RecipeCodeActionDescriptor {
	
	default String getId() {
		return getClass().getName();
	}
	
	JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext);
	
	boolean isApplicable(IJavaProject project);
	
	default ProblemType getProblemType() {
		return null;
	}

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
