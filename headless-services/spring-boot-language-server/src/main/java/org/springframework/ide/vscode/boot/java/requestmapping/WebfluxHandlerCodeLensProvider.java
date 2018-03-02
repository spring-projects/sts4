/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.handlers.CodeLensProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxHandlerCodeLensProvider implements CodeLensProvider {

	private final SpringIndexer springIndexer;

	public WebfluxHandlerCodeLensProvider(BootJavaLanguageServerComponents bootJavaLanguageServerComponents) {
		this.springIndexer = bootJavaLanguageServerComponents.getSpringIndexer();
	}

	@Override
	public void provideCodeLenses(TextDocument document, CompilationUnit cu, List<CodeLens> resultAccumulator) {
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				provideCodeLens(node, document, resultAccumulator);
				return super.visit(node);
			}
		});
	}

	protected void provideCodeLens(MethodDeclaration node, TextDocument document, List<CodeLens> resultAccumulator) {
		IMethodBinding methodBinding = node.resolveBinding();
		
		final String methodKey = methodBinding.getKey();
		List<Object> handlerInfos = this.springIndexer.getAllAdditionalInformation((addon) -> {
			if (addon instanceof WebfluxHandlerInformation) {
				WebfluxHandlerInformation handlerInfo = (WebfluxHandlerInformation) addon;
				return handlerInfo.getMethodKey() != null && handlerInfo.getMethodKey().equals(methodKey);
			}
			return false;
		});
		
		if (handlerInfos != null && handlerInfos.size() > 0) {
			for (Object object : handlerInfos) {
				try {
					WebfluxHandlerInformation handlerInfo = (WebfluxHandlerInformation) object;
				
					CodeLens codeLens = new CodeLens();
					codeLens.setRange(document.toRange(node.getName().getStartPosition(), node.getName().getLength()));
					codeLens.setCommand(new Command(handlerInfo.getSymbol(), null));

					resultAccumulator.add(codeLens);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
