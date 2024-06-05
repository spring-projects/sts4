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
package org.springframework.ide.vscode.boot.java.spel;

import static org.springframework.ide.vscode.parser.spel.SpelLexer.*;

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
import org.springframework.ide.vscode.parser.spel.SpelLexer;
import org.springframework.ide.vscode.parser.spel.SpelParser;
import org.springframework.ide.vscode.parser.spel.SpelParser.BeanReferenceContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.ConstructorReferenceContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.DottedNodeContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.FunctionOrVarContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.MethodOrPropertyContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.PossiblyQualifiedIdContext;
import org.springframework.ide.vscode.parser.spel.SpelParserBaseListener;

public class SpelSemanticTokens implements SemanticTokensDataProvider {

	@Override
	public List<String> getTokenTypes() {
		return List.of("operator", "keyword", "type", "string", "number", "method", "property");
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		SpelLexer lexer = new SpelLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		SpelParser parser = new SpelParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		parser.addParseListener(new SpelParserBaseListener() {
			
			private void processTerminalNode(TerminalNode node) {
				int type = node.getSymbol().getType();
				switch (type) {
				case INC:
				case PLUS:
				case DEC:
				case MINUS:
				case DOT:
				case COMMA:
				case STAR:
				case DIV:
				case MOD:
				case LPAREN:
				case RPAREN:
				case LSQUARE:
				case RSQUARE:
				case POWER:
				case ELVIS:
				case NE:
				case NOT:	
				case EQ:
				case ASSIGN:
				case SYMBOLIC_AND:
				case SYMBOLIC_OR:
				case GE:
				case LE:
				case GT:
				case LT:
				case QMARK:
				case RCURLY:
				case LCURLY:
				case SEMICOLON:
				case COLON:	
				case SELECT_FIRST:
				case HASH:	
				case BEAN_REF:
				case PROJECT:
				case FACTORY_BEAN_REF:
				case SELECT:
				case SAFE_NAVI:
				case SELECT_LAST:
				case BACKTICK:
					semantics.put(node.getSymbol(), "operator");
					break;
				case OR:
				case AND:
				case NEW:
				case NULL:
				case TRUE:
				case FALSE:
				case T:
				case MATCHES:
				case LE_KEYWORD:
				case LT_KEYWORD:
				case GE_KEYWORD:
				case GT_KEYWORD:
				case EQ_KEYWORD:
				case NE_KEYWORD:
					semantics.put(node.getSymbol(), "keyword");
					break;
				case IDENTIFIER:
					semantics.put(node.getSymbol(), "variable");
					break;
				case NUMERIC_LITERAL:
				case INTEGER_LITERAL:
				case REAL_LITERAL:
					semantics.put(node.getSymbol(), "number");
					break;
				case STRING_LITERAL:
				case SINGLE_QUOTED_STRING:
				case DOUBLE_QUOTED_STRING:
					semantics.put(node.getSymbol(), "string");
					break;
				}
			}

			@Override
			public void exitBeanReference(BeanReferenceContext ctx) {
				if (ctx.IDENTIFIER() != null) {
					semantics.put(ctx.IDENTIFIER().getSymbol(), "type");
				}
				if (ctx.STRING_LITERAL() != null) {
					semantics.put(ctx.STRING_LITERAL().getSymbol(), "type");
				}
			}
			
			@Override
			public void exitMethodOrProperty(MethodOrPropertyContext ctx) {
				if (ctx.methodArgs() != null) {
					semantics.put(ctx.IDENTIFIER().getSymbol(), "method");
				} else {
					if (ctx.parent instanceof DottedNodeContext) {
						semantics.put(ctx.IDENTIFIER().getSymbol(), "property");
					}
				}
			}

			@Override
			public void exitFunctionOrVar(FunctionOrVarContext ctx) {
				if (ctx.methodArgs() != null) {
					semantics.put(ctx.IDENTIFIER().getSymbol(), "method");
				}
			}

			@Override
			public void exitConstructorReference(ConstructorReferenceContext ctx) {
				PossiblyQualifiedIdContext qualified = ctx.possiblyQualifiedId();
				if (qualified != null) {
					semantics.put(qualified.IDENTIFIER(qualified.IDENTIFIER().size() - 1).getSymbol(), "method");
				}
			}

			@Override
			public void exitPossiblyQualifiedId(PossiblyQualifiedIdContext ctx) {
				if (ctx.IDENTIFIER() != null) {
					for (int i = 0; i < ctx.IDENTIFIER().size() - 1; i++) {
						semantics.put(ctx.IDENTIFIER(i).getSymbol(), "namespace");
					}
					semantics.put(ctx.IDENTIFIER(ctx.IDENTIFIER().size() - 1).getSymbol(), "type");
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
		
		parser.spelExpr();

		List<SemanticTokenData> tokens = semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList());
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
