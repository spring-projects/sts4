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
import org.springframework.ide.vscode.boot.java.data.jpa.queries.AntlrUtils;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderBaseListener;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderLexer;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderParser;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderParser.DefaultValueContext;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderParser.KeyContext;
import org.springframework.ide.vscode.parser.placeholder.PropertyPlaceHolderParser.ValueContext;

public class PropertyPlaceHolderSemanticTokens implements SemanticTokensDataProvider {

	private final Optional<Consumer<RecognitionException>> parseErrorHandler;
	
	public PropertyPlaceHolderSemanticTokens(Optional<Consumer<RecognitionException>> parseErrorHandler) {
		this.parseErrorHandler = parseErrorHandler;
	}

	@Override
	public List<String> getTokenTypes() {
		return List.of("property", "string", "operator");
	}

	@Override
	public List<SemanticTokenData> computeTokens(String text, int initialOffset) {
		PropertyPlaceHolderLexer lexer = new PropertyPlaceHolderLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		PropertyPlaceHolderParser parser = new PropertyPlaceHolderParser(antlrTokens);
		
		Map<Token, String> semantics = new HashMap<>();
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		List<SemanticTokenData> tokens = new ArrayList<>();
		
		parser.addParseListener(new PropertyPlaceHolderBaseListener() {
			
			@Override
			public void exitKey(KeyContext ctx) {
				// Remove all children nodes semantics as they will be replaced by a single token spanning multiple AST nodes
				AntlrUtils.getAllLeafs(ctx).forEach(semantics::remove);
				int start = ctx.getStart().getStartIndex() + initialOffset;
				int end = start + ctx.getText().length();
				tokens.add(new SemanticTokenData(start, end, "property", new String[0]));
			}
			
			@Override
			public void exitDefaultValue(DefaultValueContext ctx) {
				AntlrUtils.getAllLeafs(ctx).forEach(semantics::remove);
				if (ctx.Colon() != null) {
					semantics.put(ctx.Colon().getSymbol(), "operator");
				}
				if (ctx.value() != null) {
					ValueContext valueCtx = ctx.value();
					int start = valueCtx.getStart().getStartIndex() + initialOffset;
					int end = start + valueCtx.getText().length();
					tokens.add(new SemanticTokenData(start, end, "string", new String[0]));
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

			private void processTerminalNode(TerminalNode node) {
				Token token = node.getSymbol();
				switch (token.getType()) {
				case PropertyPlaceHolderLexer.Space:
				case PropertyPlaceHolderLexer.LineBreak:
				case PropertyPlaceHolderLexer.EOF:
					break;
				case PropertyPlaceHolderLexer.Colon:
					semantics.put(node.getSymbol(), "operator");
					break;
				default:
					semantics.put(node.getSymbol(), "property");
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
		
		
		parser.start();

		tokens.addAll(semantics.entrySet().stream()
				.map(e -> new SemanticTokenData(e.getKey().getStartIndex() + initialOffset,
						e.getKey().getStartIndex() + e.getKey().getText().length() + initialOffset, e.getValue(),
						new String[0]))
				.collect(Collectors.toList()));
		
		Collections.sort(tokens);
		
		return tokens;
	}

}
