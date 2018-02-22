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

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public interface SymbolProvider {

	Collection<SymbolInformation> getSymbols(Annotation node, ITypeBinding typeBinding, Collection<ITypeBinding> metaAnnotations, TextDocument doc);
	Collection<SymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc);
	Collection<SymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc);

}
