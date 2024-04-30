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
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.parser.jpql.JpqlBaseListener;
import org.springframework.ide.vscode.parser.jpql.JpqlLexer;
import org.springframework.ide.vscode.parser.jpql.JpqlParser;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Collection_valued_fieldContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Entity_nameContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Entity_type_literalContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Identification_variableContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Input_parameterContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.Single_valued_object_fieldContext;
import org.springframework.ide.vscode.parser.jpql.JpqlParser.State_fieldContext;

public class JpqlSemanticTokens implements SemanticTokensDataProvider {

	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "class", "string", "number", "operator",
			"variable", "method", "regexp", "parameter");

	@Override
	public List<String> getTokenTypes() {
		return TOKEN_TYPES;
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		JpqlLexer lexer = new JpqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		JpqlParser parser = new JpqlParser(antlrTokens);
		Map<Token, String> semantics = new HashMap<>();

		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		parser.addParseListener(new JpqlBaseListener() {

			private void processTerminalNode(TerminalNode node) {
				int type = node.getSymbol().getType();
				switch (type) {
				case JpqlParser.STRINGLITERAL:
				case JpqlParser.CHARACTER:
					semantics.put(node.getSymbol(), "string");
					break;
				case JpqlParser.LONGLITERAL:
				case JpqlParser.INTLITERAL:
				case JpqlParser.FLOATLITERAL:
					semantics.put(node.getSymbol(), "number");
					break;
				case JpqlParser.IDENTIFICATION_VARIABLE:
					semantics.put(node.getSymbol(), "variable");
					break;
				case JpqlParser.JAVASTRINGLITERAL:
					semantics.put(node.getSymbol(), "class");
					break;
				case JpqlParser.EQUAL:
				case JpqlParser.NOT_EQUAL:
					semantics.put(node.getSymbol(), "operator");
					break;
				case JpqlParser.WS:
					break;
				case JpqlParser.SPEL:
					semantics.put(node.getSymbol(), "regexp");
					break;
				default:
					if (JpqlParser.WS < type && type <= JpqlParser.WHERE) {
						semantics.put(node.getSymbol(), "keyword");
					} else {
						semantics.put(node.getSymbol(), "operator");
					}
				}
			}

			@Override
			public void exitState_field(State_fieldContext ctx) {
				semantics.put(ctx.identification_variable().getStart(), "method");
			}

			@Override
			public void exitSingle_valued_object_field(Single_valued_object_fieldContext ctx) {
				semantics.put(ctx.identification_variable().getStart(), "method");
			}

			@Override
			public void exitCollection_valued_field(Collection_valued_fieldContext ctx) {
				semantics.put(ctx.identification_variable().getStart(), "method");
			}

			@Override
			public void exitEntity_name(Entity_nameContext ctx) {
				for (Identification_variableContext iv : ctx.identification_variable()) {
					semantics.put(iv.getStart(), "class");
				}
			}

			@Override
			public void exitEntity_type_literal(Entity_type_literalContext ctx) {
				semantics.put(ctx.identification_variable().getStart(), "type");
			}

			@Override
			public void exitInput_parameter(Input_parameterContext ctx) {
				if (ctx.identification_variable() != null && ctx.identification_variable().getStart() != null) {
					semantics.put(ctx.identification_variable().getStart(), "parameter");
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
		
		List<SemanticTokenData> tokens = semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList());
		
		Collections.sort(tokens);

		return tokens;
	}

}
