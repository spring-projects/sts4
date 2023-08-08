/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.net.URI;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public interface JdtAstReconciler {
	
	void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector, boolean isCompleteAst) throws RequiredCompleteAstException;
	
	boolean isApplicable(IJavaProject project);
	
	ProblemType getProblemType();

}
