/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava.SCAN_PASS;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;

/**
 * @author Martin Lippert
 */
public class AbstractSymbolProvider implements SymbolProvider {

	@Override
	public void addSymbols(Annotation node, ITypeBinding typeBinding, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		if (SCAN_PASS.ONE.equals(context.getPass())) {
			addSymbolsPass1(node, typeBinding, metaAnnotations, context, doc);
		}
		else if (SCAN_PASS.TWO.equals(context.getPass())) {
			addSymbolsPass2(node, typeBinding, metaAnnotations, context, doc);
		}
	}

	@Override
	public void addSymbols(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
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

	protected void addSymbolsPass1(Annotation node, ITypeBinding typeBinding, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass1(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass1(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(Annotation node, ITypeBinding typeBinding, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

	protected void addSymbolsPass2(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
	}

}
