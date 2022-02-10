/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
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

import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public interface SymbolProvider {

	void addSymbols(Annotation node, FullyQualified typeBinding, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc);
	void addSymbols(ClassDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc);
	void addSymbols(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc);

}
