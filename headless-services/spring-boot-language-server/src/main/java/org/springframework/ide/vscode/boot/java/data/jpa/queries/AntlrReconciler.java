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

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

public class AntlrReconciler implements Reconciler {
	
	private static final Logger log = LoggerFactory.getLogger(AntlrReconciler.class);
	
	private final String prefix;
	private final Class<? extends Parser> parserClass;
	private final Class<? extends Lexer> lexerClass;
	private final String parseMethod;
	private final ProblemType problemType;

	public AntlrReconciler(String prefix, Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass,
			String parseMethod, ProblemType problemType) {
		this.prefix = prefix;
		this.parserClass = parserClass;
		this.lexerClass = lexerClass;
		this.parseMethod = parseMethod;
		this.problemType = problemType;
	}

	protected Parser createParser(String text, int startPosition, IProblemCollector problemCollector) throws Exception {
		Lexer lexer = lexerClass.getDeclaredConstructor(CharStream.class).newInstance(CharStreams.fromString(text));
		CommonTokenStream antlrTokens = new CommonTokenStream(lexer);
		Parser parser = parserClass.getDeclaredConstructor(TokenStream.class).newInstance(antlrTokens);
		
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
				problemCollector.accept(new ReconcileProblemImpl(problemType, prefix + ": " + msg, startPosition + offset, length));
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
		
		return parser;
	}

	@Override
	public void reconcile(String text, int startPosition, IProblemCollector problemCollector) {
		try {
			Parser parser = createParser(text, startPosition, problemCollector);
			parserClass.getDeclaredMethod(parseMethod).invoke(parser);
		} catch (Throwable t) {
			log.error("", t);
		}
	}

}
