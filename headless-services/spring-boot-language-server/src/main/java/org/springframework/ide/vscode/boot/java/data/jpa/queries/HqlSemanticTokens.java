/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.parser.hql.HqlBaseListener;
import org.springframework.ide.vscode.parser.hql.HqlLexer;
import org.springframework.ide.vscode.parser.hql.HqlParser;
import org.springframework.ide.vscode.parser.hql.HqlParser.EntityNameContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.IdentifierContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.ParameterContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.SimplePathElementContext;

public class HqlSemanticTokens implements SemanticTokensDataProvider {

	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "class", "string", "number", "operator",
			"variable", "method", "parameter", "regexp");

	@Override
	public List<String> getTokenTypes() {
		return TOKEN_TYPES;
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		HqlLexer lexer = new HqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		HqlParser parser = new HqlParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		parser.addParseListener(new HqlBaseListener() {
			
			private void processTerminalNode(TerminalNode node) {
				int type = node.getSymbol().getType();
				switch (type) {
				case HqlParser.STRINGLITERAL:
				case HqlParser.CHARACTER:
					semantics.put(node.getSymbol(), "string");
					break;
				case HqlParser.HEXLITERAL:
				case HqlParser.INTEGER_LITERAL:
				case HqlParser.FLOAT_LITERAL:
				case HqlParser.BINARY_LITERAL:
					semantics.put(node.getSymbol(), "number");
					break;
				case HqlParser.IDENTIFICATION_VARIABLE:
					semantics.put(node.getSymbol(), "variable");
					break;
				case HqlParser.JAVASTRINGLITERAL:
					semantics.put(node.getSymbol(), "class");
					break;
				case HqlParser.WS:
					break;
				case HqlParser.SPEL:
					semantics.put(node.getSymbol(), "regexp");
					break;
				default:
					if (HqlParser.WS < type && type <= HqlParser.YEAR) {
						semantics.put(node.getSymbol(), "keyword");
					} else {
						semantics.put(node.getSymbol(), "operator");
					}
				}
			}

			@Override
			public void visitTerminal(TerminalNode node) {
				processTerminalNode(node);
			}

			@Override
			public void visitErrorNode(ErrorNode node) {
				processTerminalNode(node);
			}
						
			@Override
			public void exitSimplePathElement(SimplePathElementContext ctx) {
				semantics.put(ctx.identifier().getStart(), "method");
			}

			@Override
			public void exitEntityName(EntityNameContext ctx) {
				for (IdentifierContext i : ctx.identifier()) {
					semantics.put(i.getStart(), "class");
				}
			}

			@Override
			public void exitParameter(ParameterContext ctx) {
				if (ctx.identifier() != null && ctx.identifier().getStart() != null) {
					semantics.put(ctx.identifier().getStart(), "parameter");
				}
			}

		});
		
		parser.ql_statement();
		
		List<SemanticTokenData> tokens = semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList());
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
