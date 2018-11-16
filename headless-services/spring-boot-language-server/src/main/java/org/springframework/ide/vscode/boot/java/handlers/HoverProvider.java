/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public interface HoverProvider {

	default Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	default Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	default Hover provideHover(MethodDeclaration methodDeclaration, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	default Hover provideMethodParameterHover(SingleVariableDeclaration parameter, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project,TypeDeclaration typeDeclaration, TextDocument doc, SpringBootApp[] runningApps) {
		return null;
	}

	default Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, MethodDeclaration methodDeclaration, TextDocument doc, SpringBootApp[] runningApps) {
		return null;
	}

 }
