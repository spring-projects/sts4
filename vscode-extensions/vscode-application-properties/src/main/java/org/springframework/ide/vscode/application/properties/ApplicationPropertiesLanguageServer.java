/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.reconcile.SpringPropertiesReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.BadWordReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.Parser;

import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;

/**
 * Language Server for Spring Boot Application Properties files
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesLanguageServer extends SimpleLanguageServer {
	
	private static final String SYNTAX_ERROR_HEADER_MSG = "Syntax Error: ";
	private static final String YNTAX_ERROR_MSG__UNEXPECTED_END_OF_INPUT = "Unexpected end of input, value identifier is expected";
	private static final String SYNTAX_ERROR_MSG__UNEXPECTED_END_OF_LINE = "Unexpected end of line, value identifier is expected";
	
	private ParseResults parseResults;
	private Parser parser;
	
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;


	public ApplicationPropertiesLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.parser = new AntlrParser();
		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});

//		documents.onDidChangeContent(params -> {
//			System.out.println("Document changed: "+params);
//			TextDocument doc = params.getDocument();
//			parseResults = parser.parse(doc.getText());
//			validateDocument(documents, doc);
//		});
		
	}

//	private void validateDocument(SimpleTextDocumentService documents, TextDocument doc) {
//		documents.publishDiagnostics(doc, parseResults.syntaxErrors.stream().map(problem -> {
//			DiagnosticImpl diagnostic = new DiagnosticImpl();
//			diagnostic.setMessage(createSyntaxErrorMessage(problem.getMessage()));
//			diagnostic.setCode(problem.getCode());
//			diagnostic.setSeverity(DiagnosticSeverity.Error);
//			diagnostic.setSource("java-properties");
//			diagnostic.setRange(doc.toRange(problem.getOffset(), problem.getLength()));
//			return diagnostic;
//		}).collect(Collectors.toList()));
//	}
//	
//	private static String createSyntaxErrorMessage(String parserMessage) {
//		String message = parserMessage;
//		if (parserMessage.contains("extraneous input '\\n' expecting")) {
//			message = SYNTAX_ERROR_MSG__UNEXPECTED_END_OF_LINE;
//		} else if (parserMessage.contains("mismatched input '<EOF>' expecting")) {
//			message = YNTAX_ERROR_MSG__UNEXPECTED_END_OF_INPUT;
//		}
//		return SYNTAX_ERROR_HEADER_MSG + message;
//	}
	
	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		return c;
	}
	
	protected IReconcileEngine getReconcileEngine() {
		return new SpringPropertiesReconcileEngine(indexProvider, typeUtilProvider);
	}


}
