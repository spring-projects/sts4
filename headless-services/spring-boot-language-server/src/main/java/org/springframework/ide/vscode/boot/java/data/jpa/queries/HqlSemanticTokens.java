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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;

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
import org.springframework.ide.vscode.parser.hql.HqlParser.SimplePathElementContext;

public class HqlSemanticTokens implements SemanticTokensDataProvider {

	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "class", "string", "number", "operator",
			"variable", "method", "modifier", "regexp");

	@Override
	public List<String> getTokenTypes() {
		return TOKEN_TYPES;
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		List<SemanticTokenData> tokens = new ArrayList<>();
		HqlLexer lexer = new HqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		HqlParser parser = new HqlParser(antlrTokens);
		LinkedHashSet<Token> coloredTokens = new LinkedHashSet<>();
		
		Stack<SimplePathElementContext> pathContexts = new Stack<>();
		
		parser.addParseListener(new HqlBaseListener() {
			
			private void addToken(Token token, String tokenType) {
				tokens.add(new SemanticTokenData(token.getStartIndex() + initialOffset,
						token.getStartIndex() + token.getText().length() + initialOffset, tokenType, new String[0]));
				coloredTokens.add(token);
			}

			private void processTerminalNode(TerminalNode node) {
				if (coloredTokens.contains(node.getSymbol())) {
					return;
				}
				int type = node.getSymbol().getType();
				switch (type) {
				case HqlParser.STRINGLITERAL:
				case HqlParser.CHARACTER:
					addToken(node.getSymbol(), "string");
					break;
				case HqlParser.HEXLITERAL:
				case HqlParser.INTEGER_LITERAL:
				case HqlParser.FLOAT_LITERAL:
				case HqlParser.BINARY_LITERAL:
					addToken(node.getSymbol(), "number");
					break;
				case HqlParser.IDENTIFICATION_VARIABLE:
					if (pathContexts.isEmpty()) {
						addToken(node.getSymbol(), "variable");
					} else {
						addToken(node.getSymbol(), "method");
					}
					break;
				case HqlParser.JAVASTRINGLITERAL:
					addToken(node.getSymbol(), "class");
					break;
				case HqlParser.WS:
					break;
//				case JpqlParser.SPEL:
//					addToken(node.getSymbol(), "regexp");
//					break;
				default:
					if (HqlParser.WS < type && type <= HqlParser.YEAR) {
						addToken(node.getSymbol(), "keyword");
					} else {
						addToken(node.getSymbol(), "modifier");
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
			public void enterEntityName(EntityNameContext ctx) {
				addToken(ctx.getStart(), "class");
			}
			
			

			@Override
			public void enterSimplePathElement(SimplePathElementContext ctx) {
				pathContexts.push(ctx);
			}

			@Override
			public void exitSimplePathElement(SimplePathElementContext ctx) {
				pathContexts.pop();
			}

		});
		
		parser.ql_statement();
		return tokens;
	}

}
