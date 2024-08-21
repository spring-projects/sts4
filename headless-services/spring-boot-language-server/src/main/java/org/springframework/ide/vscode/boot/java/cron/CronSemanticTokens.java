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
package org.springframework.ide.vscode.boot.java.cron;

import static org.springframework.ide.vscode.parser.cron.CronLexer.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.ide.vscode.parser.cron.CronLexer;
import org.springframework.ide.vscode.parser.cron.CronParser;
import org.springframework.ide.vscode.parser.spel.SpelParserBaseListener;

public class CronSemanticTokens implements SemanticTokensDataProvider {

	private final Optional<Consumer<RecognitionException>> parseErrorHandler;
	
	public CronSemanticTokens(Optional<Consumer<RecognitionException>> parseErrorHandler) {
		this.parseErrorHandler = parseErrorHandler;
	}
	
	public CronSemanticTokens() {
		this(Optional.empty());
	}

	@Override
	public List<String> getTokenTypes() {
		return List.of("operator", "number", "enum", "macro", "method");
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		CronLexer lexer = new CronLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		CronParser parser = new CronParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		List<SemanticTokenData> tokens = new ArrayList<>();
		
		parser.addParseListener(new SpelParserBaseListener() {
			
			private void processTerminalNode(TerminalNode node) {
				Token token = node.getSymbol();
				switch (token.getType()) {
				case COMMA:
				case DASH:
				case QUESTION:
				case SLASH:	
				case STAR:
				case TAG:
					semantics.put(token, "operator");
					break;
				case L:
				case W:
				case LW:
					semantics.put(token, "method");
					break;
				case MON:
				case TUE:
				case WED:
				case THU:
				case FRI:
				case SAT:
				case SUN:
				case JAN:
				case FEB:
				case MAR:
				case APR:
				case MAY:
				case JUN:
				case JUL:
				case AUG:
				case SEP:
				case OCT:
				case NOV:
				case DEC:
				case IDENTIFIER:	
					semantics.put(token, "enum");
					break;
				case INT:
					semantics.put(token, "number");
					break;
				case MACRO:
					semantics.put(token, "macro");
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
		
		
		parser.cronExpression();

		tokens.addAll(semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList()));
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
