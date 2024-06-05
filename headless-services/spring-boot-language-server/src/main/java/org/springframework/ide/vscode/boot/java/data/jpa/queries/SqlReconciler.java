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

import java.util.BitSet;
import java.util.Optional;

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
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.boot.java.spel.SpelReconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.parser.sql.MySqlLexer;
import org.springframework.ide.vscode.parser.sql.MySqlParser;
import org.springframework.ide.vscode.parser.sql.MySqlParserBaseListener;

public class SqlReconciler implements Reconciler {

	private final Optional<SpelReconciler> spelReconciler;

	public SqlReconciler(Optional<SpelReconciler> spelReconciler) {
		this.spelReconciler = spelReconciler;
	}

	@Override
	public void reconcile(String text, int startPosition, IProblemCollector problemCollector) {
		MySqlLexer lexer = new MySqlLexer(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		MySqlParser parser = new MySqlParser(antlrTokens);
		
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		
		parser.addErrorListener(new ANTLRErrorListener() {
			
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
					String msg, RecognitionException e) {
				Token token = (Token) offendingSymbol;
				int offset = token.getStartIndex();
				int length = token.getStopIndex() - token.getStartIndex() + 1;
				if (token.getStartIndex() >= token.getStopIndex()) {
					offset = token.getStartIndex() - token.getCharPositionInLine();
					length = token.getCharPositionInLine() + 1;
				}
				problemCollector.accept(new ReconcileProblemImpl(QueryProblemType.EXPRESSION_SYNTAX, "SQL: " + msg, startPosition + offset, length));
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
		
		// Reconcile embedded SPEL
		parser.addParseListener(new MySqlParserBaseListener() {
			
			private void processTerminal(TerminalNode node) {
				if (node.getSymbol().getType() == MySqlParser.SPEL) {
					spelReconciler.ifPresent(r -> JpqlReconciler.reconcileEmbeddedSpelNode(node, startPosition, r, problemCollector));
				}
			}

			@Override
			public void visitTerminal(TerminalNode node) {
				processTerminal(node);
			}

			@Override
			public void visitErrorNode(ErrorNode node) {
				processTerminal(node);
			}
			
		});

		
		parser.sqlStatements();
		
	}

}
