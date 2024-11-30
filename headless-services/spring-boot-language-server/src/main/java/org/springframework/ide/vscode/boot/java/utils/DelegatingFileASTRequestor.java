/*******************************************************************************
 * Copyright (c) 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.function.BiConsumer;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;

public class DelegatingFileASTRequestor extends FileASTRequestor {

	private final FileASTRequestor delegate;
	private final BiConsumer<String, CompilationUnit> handler;
	
	public DelegatingFileASTRequestor(FileASTRequestor delegate, BiConsumer<String, CompilationUnit> handler) {
		this.delegate = delegate;
		this.handler = handler;
	}

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		super.acceptAST(sourceFilePath, ast);
		this.handler.accept(sourceFilePath, ast);
		delegate.acceptAST(sourceFilePath, ast);
	}

	@Override
	public void acceptBinding(String bindingKey, IBinding binding) {
		super.acceptBinding(bindingKey, binding);
		delegate.acceptBinding(bindingKey, binding);
	}
	
}
