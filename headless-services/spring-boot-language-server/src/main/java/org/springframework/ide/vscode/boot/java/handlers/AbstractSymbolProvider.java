/*******************************************************************************
 * Copyright (c) 2019, 2022 Pivotal, Inc.
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
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava.SCAN_PASS;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class AbstractSymbolProvider implements SymbolProvider {

	@Override
	public void addSymbols(Annotation node, FullyQualified typeBinding, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		if (SCAN_PASS.ONE.equals(context.getPass())) {
			addSymbolsPass1(node, typeBinding, metaAnnotations, context, doc);
		}
		else if (SCAN_PASS.TWO.equals(context.getPass())) {
			addSymbolsPass2(node, typeBinding, metaAnnotations, context, doc);
		}
	}

	@Override
	public void addSymbols(ClassDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		if (SCAN_PASS.ONE.equals(context.getPass())) {
			addSymbolsPass1(typeDeclaration, context, doc);
		}
		else if (SCAN_PASS.TWO.equals(context.getPass())) {
			addSymbolsPass2(typeDeclaration, context, doc);
		}
	}

	@Override
	public void addSymbols(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		if (SCAN_PASS.ONE.equals(context.getPass())) {
			addSymbolsPass1(methodDeclaration, context, doc);
		}
		else if (SCAN_PASS.TWO.equals(context.getPass())) {
			addSymbolsPass2(methodDeclaration, context, doc);
		}
	}


	//
	// implementations can decide whether to implement just pass1 or if they need 2 phases, they would have to implement both methods (pass1 + pass2)
	//

	protected void addSymbolsPass1(Annotation node, FullyQualified typeBinding, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass1(ClassDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass1(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(Annotation node, FullyQualified typeBinding, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(ClassDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

}
