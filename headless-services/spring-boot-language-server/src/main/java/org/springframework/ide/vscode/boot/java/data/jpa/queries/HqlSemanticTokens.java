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
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
import org.springframework.ide.vscode.boot.java.spel.SpelSemanticTokens;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.parser.hql.HqlBaseListener;
import org.springframework.ide.vscode.parser.hql.HqlLexer;
import org.springframework.ide.vscode.parser.hql.HqlParser;
import org.springframework.ide.vscode.parser.hql.HqlParser.EntityNameContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.IdentifierContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.InstantiationTargetContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.ParameterContext;
import org.springframework.ide.vscode.parser.hql.HqlParser.SimplePathElementContext;

public class HqlSemanticTokens implements SemanticTokensDataProvider {

	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "class", "string", "number", "operator",
			"variable", "method", "parameter", "property");
	
	private final Optional<SpelSemanticTokens> optSpelTokens;

	private final Optional<Consumer<RecognitionException>> parseErrorHandler;
	
	public HqlSemanticTokens(Optional<SpelSemanticTokens> optSpelTokens) {
		this(optSpelTokens, Optional.empty());
	}
	
	public HqlSemanticTokens(Optional<SpelSemanticTokens> optSpelTokens, Optional<Consumer<RecognitionException>> parseErrorHandler) {
		this.optSpelTokens = optSpelTokens;
		this.parseErrorHandler = parseErrorHandler;
	}
	
	@Override
	public List<String> getTokenTypes() {
		LinkedHashSet<String> tokenTypes = new LinkedHashSet<>(TOKEN_TYPES);
		tokenTypes.addAll(optSpelTokens.map(s -> s.getTokenTypes()).orElse(Collections.emptyList()));
		return tokenTypes.stream().toList();
	}

	@Override
	public List<String> getTypeModifiers() {
		return optSpelTokens.map(s -> s.getTypeModifiers()).orElse(Collections.emptyList());
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		HqlLexer lexer = new HqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		HqlParser parser = new HqlParser(antlrTokens);		
		Map<Token, String> semantics = new HashMap<>();
		List<SemanticTokenData> tokens = new ArrayList<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
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
					tokens.addAll(JpqlSemanticTokens.computeTokensFromSpelNode(node, initialOffset, optSpelTokens));
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
			public void exitInstantiationTarget(InstantiationTargetContext ctx) {
				int offset = initialOffset + ctx.getStart().getStartIndex();
				int length = ctx.getText().length();
				tokens.add(new SemanticTokenData(offset, offset + length , "method", new String[0]));
				AntlrUtils.getAllLeafs(ctx).forEach(semantics::remove);
			}

			@Override
			public void exitSimplePathElement(SimplePathElementContext ctx) {
				semantics.put(ctx.identifier().getStart(), "property");
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
				if (ctx.INTEGER_LITERAL() != null) {
					semantics.put(ctx.INTEGER_LITERAL().getSymbol(), "parameter");
				}
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
		
		parser.ql_statement();
		
		semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.forEach(tokens::add);
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
