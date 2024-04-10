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
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlBaseListener;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlLexer;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser.Collection_valued_fieldContext;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser.Entity_nameContext;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser.Entity_type_literalContext;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser.Single_valued_object_fieldContext;
import org.springframework.ide.vscode.jpql.antlr.parser.JpqlParser.State_fieldContext;

public class JpqlSemanticTokens implements SemanticTokensDataProvider {

	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "class", "string", "number", "operator",
			"variable", "method", "modifier", "regexp");

	@Override
	public List<String> getTokenTypes() {
		return TOKEN_TYPES;
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		List<SemanticTokenData> tokens = new ArrayList<>();
		JpqlLexer lexer = new JpqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		JpqlParser parser = new JpqlParser(antlrTokens);
		HashSet<Token> coloredTokens = new HashSet<>();
		parser.addParseListener(new JpqlBaseListener() {

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
				case JpqlParser.STRINGLITERAL:
				case JpqlParser.CHARACTER:
					addToken(node.getSymbol(), "string");
					break;
				case JpqlParser.LONGLITERAL:
				case JpqlParser.INTLITERAL:
				case JpqlParser.FLOATLITERAL:
					addToken(node.getSymbol(), "number");
					break;
				case JpqlParser.IDENTIFICATION_VARIABLE:
					addToken(node.getSymbol(), "variable");
					break;
				case JpqlParser.JAVASTRINGLITERAL:
					addToken(node.getSymbol(), "class");
					break;
				case JpqlParser.EQUAL:
				case JpqlParser.NOT_EQUAL:
					addToken(node.getSymbol(), "operator");
					break;
				case JpqlParser.WS:
					break;
				case JpqlParser.SPEL:
					addToken(node.getSymbol(), "regexp");
					break;
				default:
					if (JpqlParser.WS < type && type <= JpqlParser.WHERE) {
						addToken(node.getSymbol(), "keyword");
					} else {
						addToken(node.getSymbol(), "modifier");
					}
				}
			}

			@Override
			public void enterState_field(State_fieldContext ctx) {
				if (!coloredTokens.contains(ctx.getStart())) {
					addToken(ctx.getStart(), "method");
				}
			}

			@Override
			public void enterSingle_valued_object_field(Single_valued_object_fieldContext ctx) {
				if (!coloredTokens.contains(ctx.getStart())) {
					addToken(ctx.getStart(), "method");
				}
			}

			@Override
			public void enterCollection_valued_field(Collection_valued_fieldContext ctx) {
				if (!coloredTokens.contains(ctx.getStart())) {
					addToken(ctx.getStart(), "method");
				}
			}

			@Override
			public void enterEntity_name(Entity_nameContext ctx) {
				if (!coloredTokens.contains(ctx.getStart())) {
					addToken(ctx.getStart(), "class");
				}
			}

			@Override
			public void enterEntity_type_literal(Entity_type_literalContext ctx) {
				if (!coloredTokens.contains(ctx.getStart())) {
					addToken(ctx.getStart(), "type");
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

		});
		parser.ql_statement();
		return tokens;
	}

}
