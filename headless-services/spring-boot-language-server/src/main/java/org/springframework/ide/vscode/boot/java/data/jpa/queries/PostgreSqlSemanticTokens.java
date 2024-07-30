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
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlLexer;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser.Data_typeContext;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser.Func_nameContext;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser.IdentifierContext;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser.ParameterContext;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParserBaseListener;

public class PostgreSqlSemanticTokens implements SemanticTokensDataProvider {
	
	private static List<String> TOKEN_TYPES = List.of("keyword", "type", "string", "number", "operator",
			"variable", "regexp", "comment", "parameter", "method");

	private Optional<SpelSemanticTokens> optSpelTokens;
	private Optional<Consumer<RecognitionException>> parseErrorHandler;
	
	public PostgreSqlSemanticTokens(Optional<SpelSemanticTokens> optSpelTokens) {
		this(optSpelTokens, Optional.empty());
	}

	public PostgreSqlSemanticTokens(Optional<SpelSemanticTokens> optSpelTokens, Optional<Consumer<RecognitionException>> parseErrorHandler) {
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
		PostgreSqlLexer lexer = new PostgreSqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		PostgreSqlParser parser = new PostgreSqlParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		List<SemanticTokenData> tokens = new ArrayList<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		parser.addParseListener(new PostgreSqlParserBaseListener() {
			
			private void processTerminalNode(TerminalNode node) {
				Token token = node.getSymbol();
				int type = token.getType();
				if (type >= PostgreSqlLexer.A_ && type <= PostgreSqlLexer.OWNED) {
					semantics.put(token, "keyword");
				} else if (type >= PostgreSqlLexer.ABSTIME && type <= PostgreSqlLexer.XML) {
					semantics.put(token, "type");
				} else if ((type >= PostgreSqlLexer.AMP && type <= PostgreSqlLexer.SEMI)
						|| (type >= PostgreSqlLexer.COMMA && type <= PostgreSqlLexer.CLOSE_BRACKET)
						|| type == PostgreSqlLexer.DOT) {
					semantics.put(token, "operator");
				} else if (type == PostgreSqlLexer.REGEX_STRING) {
					semantics.put(token, "regexp");
				} else if (type >= PostgreSqlLexer.SINGLEQ_STRING_LITERAL && type <= PostgreSqlLexer.DOUBLEQ_STRING_LITERAL) {
					semantics.put(token, "string");
				} else if ((type >= PostgreSqlLexer.NUMERIC_LITERAL && type <= PostgreSqlLexer.HEX_INTEGER_LITERAL)
						|| type == PostgreSqlLexer.DOLLAR_DEC || type == PostgreSqlLexer.BIT_STRING) {
					semantics.put(token, "number");
				} else if (type == PostgreSqlLexer.IDENTIFIER) {
					semantics.put(token, "variable");
				} else if (type >= PostgreSqlLexer.BLOCK_COMMENT && type <= PostgreSqlLexer.LINE_COMMENT) {
					semantics.put(token, "comment");
				} else if (type == PostgreSqlLexer.SPEL) {
					tokens.addAll(JpqlSemanticTokens.computeTokensFromSpelNode(node, initialOffset, optSpelTokens));
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
			public void exitIdentifier(IdentifierContext ctx) {
				if (ctx.identifier() != null) {
					for (int i = 1; i < ctx.identifier().size(); i++) {
						IdentifierContext identifier = ctx.identifier(i);
						if (identifier.IDENTIFIER() != null) {
							semantics.put(identifier.IDENTIFIER().getSymbol(), "property");
						}
					}
				}
			}

			@Override
			public void exitFunc_name(Func_nameContext funcName) {
				if (funcName.identifier() != null && funcName.identifier().IDENTIFIER() != null) {
					semantics.put(funcName.identifier().IDENTIFIER().getSymbol(), "method");
				}
			}
			
			@Override
			public void exitData_type(Data_typeContext dataType) {
				if (dataType.identifier() != null) {
					MySqlSemanticTokens.getAllLeafs(dataType.identifier())
						.filter(t -> t.getType() == PostgreSqlLexer.IDENTIFIER)
						.forEach(t -> semantics.put(t, "type"));
				}
			}

			@Override
			public void exitParameter(ParameterContext param) {
				if (param.identifier() != null) {
					MySqlSemanticTokens.getAllLeafs(param.identifier()).forEach(t -> semantics.put(t, "parameter"));
				} else if (param.INTEGER_LITERAL() != null) {
					semantics.put(param.INTEGER_LITERAL().getSymbol(), "parameter");
				} else if (param.reserved_keyword() != null) {
					MySqlSemanticTokens.getAllLeafs(param.reserved_keyword()).forEach(t -> semantics.put(t, "parameter"));
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
		
		parser.root();
		
		semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.forEach(tokens::add);
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
