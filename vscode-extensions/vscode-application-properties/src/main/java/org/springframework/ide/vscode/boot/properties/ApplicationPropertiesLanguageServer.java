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
package org.springframework.ide.vscode.boot.properties;

import java.util.stream.Collectors;

import org.springframework.ide.vscode.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.properties.parser.ParseResults;
import org.springframework.ide.vscode.properties.parser.Parser;
import org.springframework.ide.vscode.util.SimpleLanguageServer;
import org.springframework.ide.vscode.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.util.TextDocument;

import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticSeverity;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentSyncKind;
import io.typefox.lsapi.impl.DiagnosticImpl;
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

	public ApplicationPropertiesLanguageServer() {
		this.parser = new AntlrParser();
		SimpleTextDocumentService documents = getTextDocumentService();
		
		documents.onDidChangeContent(params -> {
			System.out.println("Document changed: "+params);
			TextDocument doc = params.getDocument();
			parseResults = parser.parse(doc.getText());
			validateDocument(documents, doc);
		});
		
	}

	private void validateDocument(SimpleTextDocumentService documents, TextDocument doc) {
		documents.publishDiagnostics(doc, parseResults.syntaxErrors.stream().map(problem -> {
			DiagnosticImpl diagnostic = new DiagnosticImpl();
			diagnostic.setMessage(createSyntaxErrorMessage(problem.getMessage()));
			diagnostic.setCode(problem.getCode());
			diagnostic.setSeverity(DiagnosticSeverity.Error);
			diagnostic.setSource("java-properties");
			diagnostic.setRange(doc.toRange(problem.getOffset(), problem.getLength()));
			return diagnostic;
		}).collect(Collectors.toList()));
	}
	
	private static String createSyntaxErrorMessage(String parserMessage) {
		String message = parserMessage;
		if (parserMessage.contains("extraneous input '\\n' expecting")) {
			message = SYNTAX_ERROR_MSG__UNEXPECTED_END_OF_LINE;
		} else if (parserMessage.contains("mismatched input '<EOF>' expecting")) {
			message = YNTAX_ERROR_MSG__UNEXPECTED_END_OF_INPUT;
		}
		return SYNTAX_ERROR_HEADER_MSG + message;
	}
	
	@Override
	protected ServerCapabilitiesImpl getServerCapabilities() {
		ServerCapabilitiesImpl c = new ServerCapabilitiesImpl();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Full);
		
		return c;
	}
}
