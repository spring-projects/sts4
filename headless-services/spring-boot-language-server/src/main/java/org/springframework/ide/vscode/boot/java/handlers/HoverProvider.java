/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public interface HoverProvider {

	default Hover provideHover(J node, Annotation annotation, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Hover provideHover(J node, ClassDeclaration typeDeclaration, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Hover provideHover(MethodDeclaration methodDeclaration, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Hover provideMethodParameterHover(VariableDeclarations parameter, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, ClassDeclaration typeDeclaration, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, MethodDeclaration methodDeclaration, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		return null;
	}

 }
