/*******************************************************************************
 * Copyright (c) 2018, 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.handlers.CodeLensProvider;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxHandlerCodeLensProvider implements CodeLensProvider {

	private final SpringSymbolIndex springIndexer;

	public WebfluxHandlerCodeLensProvider(SpringSymbolIndex springIndexer) {
		this.springIndexer = springIndexer;
	}

	@Override
	public void provideCodeLenses(CancelChecker cancelToken, TextDocument document, CompilationUnit cu, List<CodeLens> resultAccumulator) {
		new JavaIsoVisitor<List<CodeLens>>() {
			public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, List<CodeLens> cl) {
				provideCodeLens(cancelToken, method, document, cl);
				return method;
			};
		}.visitNonNull(cu, resultAccumulator);		
	}

	protected void provideCodeLens(CancelChecker cancelToken, MethodDeclaration method, TextDocument document, List<CodeLens> resultAccumulator) {
		cancelToken.checkCanceled();
		
		ClassDeclaration declaringType = ORAstUtils.findDeclaringType(method);
		
		if (method != null && ORAstUtils.findDeclaringType(method) != null) {

			final String handlerClass = declaringType.getType().getFullyQualifiedName();
			final String handlerMethod = method.getMethodType().toString(); // TODO: OR AST likely a problem
			
			cancelToken.checkCanceled();

			List<SymbolAddOnInformation> handlerInfos = this.springIndexer.getAllAdditionalInformation((addon) -> {
				if (addon instanceof WebfluxHandlerInformation) {
					WebfluxHandlerInformation handlerInfo = (WebfluxHandlerInformation) addon;
					return handlerInfo.getHandlerClass() != null && handlerInfo.getHandlerClass().equals(handlerClass)
							&& handlerInfo.getHandlerMethod() != null && handlerInfo.getHandlerMethod().equals(handlerMethod);
				}
				return false;
			});

			if (handlerInfos != null && handlerInfos.size() > 0) {
				for (Object object : handlerInfos) {
					try {
						WebfluxHandlerInformation handlerInfo = (WebfluxHandlerInformation) object;

						CodeLens codeLens = new CodeLens();
						Range r = ORAstUtils.getRange(method.getName());
						codeLens.setRange(document.toRange(r.getStart().getOffset(), r.length()));

						String httpMethod = WebfluxUtils.getStringRep(handlerInfo.getHttpMethods(), string -> string);
						String codeLensCommand = httpMethod != null ? httpMethod + " " : "";

						codeLensCommand += handlerInfo.getPath();

						String acceptType = WebfluxUtils.getStringRep(handlerInfo.getAcceptTypes(), WebfluxUtils::getMediaType);
						codeLensCommand += acceptType != null ? " - Accept: " + acceptType : "";

						String contentType = WebfluxUtils.getStringRep(handlerInfo.getContentTypes(), WebfluxUtils::getMediaType);
						codeLensCommand += contentType != null ? " - Content-Type: " + contentType : "";
						
						Command cmd = new Command();
						cmd.setTitle(codeLensCommand);
						codeLens.setCommand(cmd);

						resultAccumulator.add(codeLens);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
