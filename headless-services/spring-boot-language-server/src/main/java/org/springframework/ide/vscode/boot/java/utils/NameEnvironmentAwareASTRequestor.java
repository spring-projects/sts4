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
package org.springframework.ide.vscode.boot.java.utils;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Lippert
 */
public class NameEnvironmentAwareASTRequestor extends FileASTRequestor {

	private static final Logger log = LoggerFactory.getLogger(NameEnvironmentAwareASTRequestor.class);

	private FileASTRequestor realOne;
	private INameEnvironment nameEnvironment;

	public NameEnvironmentAwareASTRequestor(FileASTRequestor realOne) {
		this.realOne = realOne;
	}

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit cu) {
		if (nameEnvironment == null) {
			extractNameEnvironment();
		}
		
		realOne.acceptAST(sourceFilePath, cu);
	}
	
	public INameEnvironment getNameEnvironment() {
		return nameEnvironment;
	}

	private void extractNameEnvironment() {
		try {
			Field declaredField = FileASTRequestor.class.getDeclaredField("compilationUnitResolver");
			declaredField.setAccessible(true);
			
			Object compilationUnitResolver = declaredField.get(this);
			Object lookupEnvironment = compilationUnitResolver.getClass().getField("lookupEnvironment")
					.get(compilationUnitResolver);
			nameEnvironment = (INameEnvironment) lookupEnvironment.getClass()
					.getField("nameEnvironment").get(lookupEnvironment);
		} catch (Exception e) {
			log.error(" could not identify name environment when scanning for symbols in Java code - " + e.getMessage());
		}

	}
}
