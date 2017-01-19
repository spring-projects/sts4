/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.java.completions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class BootJavaCompletionEngine implements ICompletionEngine {

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception {
		List<ICompletionProposal> completions = new ArrayList<>();

		System.out.println("create parser");

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);

		System.out.println("set source");

		parser.setSource(document.get(0, document.getLength()).toCharArray());

		System.out.println("start parse");

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		System.out.println("end parse");

		ASTNode node = NodeFinder.perform(cu, offset, 0);

		System.out.println("AST node found: " + node.getClass().getName());

		return completions;
	}

}
