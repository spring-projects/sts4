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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
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
import org.springframework.ide.vscode.parser.spel.SpelParser.InputParameterContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.MethodOrPropertyContext;
import org.springframework.ide.vscode.parser.spel.SpelParser.PossiblyQualifiedIdContext;
import org.springframework.ide.vscode.parser.spel.SpelParserBaseListener;

public class SpelSemanticTokens implements SemanticTokensDataProvider {
	
	private final Optional<Consumer<RecognitionException>> parseErrorHandler;
	private final PropertyPlaceHolderSemanticTokens propertyPlaceHolderSemanticTokens;
	
	public SpelSemanticTokens(Optional<Consumer<RecognitionException>> parseErrorHandler) {
		this.parseErrorHandler = parseErrorHandler;
		this.propertyPlaceHolderSemanticTokens = new PropertyPlaceHolderSemanticTokens(parseErrorHandler);
	}
	
	public SpelSemanticTokens() {
		this(Optional.empty());
	}

	@Override
	public List<String> getTokenTypes() {
		LinkedHashSet<String> tokenTypes = new LinkedHashSet<>(List.of("operator", "keyword", "type", "string", "number", "method", "property", "parameter"));
		tokenTypes.addAll(propertyPlaceHolderSemanticTokens.getTokenTypes());
		return tokenTypes.stream().toList();
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		SpelLexer lexer = new SpelLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		SpelParser parser = new SpelParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		List<SemanticTokenData> tokens = new ArrayList<>();
		
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
				case INTEGER_LITERAL:
				case REAL_LITERAL:
					semantics.put(node.getSymbol(), "number");
					break;
				case STRING_LITERAL:
				case SINGLE_QUOTED_STRING:
				case DOUBLE_QUOTED_STRING:
					semantics.put(node.getSymbol(), "string");
					break;
				case PROPERTY_PLACE_HOLDER:
					tokens.addAll(computeTokensFromPropertyPlaceHolderNode(node, initialOffset));
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
			public void exitInputParameter(InputParameterContext ctx) {
				semantics.remove(ctx.LSQUARE().getSymbol());
				semantics.remove(ctx.INTEGER_LITERAL().getSymbol());
				semantics.remove(ctx.RSQUARE().getSymbol());
				
				int start = ctx.getStart().getStartIndex() + initialOffset;
				int end = start + ctx.getText().length(); 
				tokens.add(new SemanticTokenData(start, end, "parameter", new String[0]));
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
		
		parser.addErrorListener(new ANTLRErrorListener() {
			
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e) {
				parseErrorHandler.ifPresent(h -> h.accept(e));
			}
			
			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
					ATNConfigSet configs) {
			}
			
			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
			}
			
			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
			}
		});
		
		
		parser.spelExpr();

		tokens.addAll(semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList()));
		
		Collections.sort(tokens);
		
		return tokens;
	}
	
	private Collection<? extends SemanticTokenData> computeTokensFromPropertyPlaceHolderNode(TerminalNode node,
			int initialOffset) {
		List<SemanticTokenData> placeHolderTokens = new ArrayList<>();

		int startPosition = initialOffset + node.getSymbol().getStartIndex();
		int placeHolderStartPosition = startPosition + 2;
		int endPosition = startPosition + node.getText().length();
		int placeHolderEndPosition = endPosition - 1;
		// '${' operator
		placeHolderTokens.add(new SemanticTokenData(startPosition, placeHolderStartPosition, "operator", new String[0]));
		// Property Place Holder contents
		placeHolderTokens.addAll(propertyPlaceHolderSemanticTokens.computeTokens(node.getText().substring(2, node.getText().length() - 1), placeHolderStartPosition));		
		// '}' operator
		placeHolderTokens.add(new SemanticTokenData(placeHolderEndPosition, endPosition, "operator", new String[0]));
		return placeHolderTokens;
	}



}
