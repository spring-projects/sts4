// Generated from SpelParser.g4 by ANTLR 4.13.1
package org.springframework.ide.vscode.parser.spel;

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

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SpelParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMICOLON=1, WS=2, INC=3, PLUS=4, DEC=5, MINUS=6, COLON=7, DOT=8, COMMA=9, 
		STAR=10, DIV=11, MOD=12, LPAREN=13, RPAREN=14, LSQUARE=15, RSQUARE=16, 
		HASH=17, BEAN_REF=18, SELECT_FIRST=19, POWER=20, NE=21, PROJECT=22, NOT=23, 
		EQ=24, ASSIGN=25, SYMBOLIC_AND=26, FACTORY_BEAN_REF=27, SYMBOLIC_OR=28, 
		SELECT=29, ELVIS=30, SAFE_NAVI=31, QMARK=32, SELECT_LAST=33, GE=34, GT=35, 
		LE=36, LT=37, LCURLY=38, RCURLY=39, BACKTICK=40, OR=41, AND=42, TRUE=43, 
		FALSE=44, NEW=45, NULL=46, T=47, MATCHES=48, GT_KEYWORD=49, GE_KEYWORD=50, 
		LE_KEYWORD=51, LT_KEYWORD=52, EQ_KEYWORD=53, NE_KEYWORD=54, IDENTIFIER=55, 
		NUMERIC_LITERAL=56, REAL_LITERAL=57, INTEGER_LITERAL=58, STRING_LITERAL=59, 
		SINGLE_QUOTED_STRING=60, DOUBLE_QUOTED_STRING=61, ESCAPED_BACKTICK=62, 
		SPEL_IN_TEMPLATE_STRING_OPEN=63, TEMPLATE_TEXT=64;
	public static final int
		RULE_script = 0, RULE_spelExpr = 1, RULE_node = 2, RULE_nonDottedNode = 3, 
		RULE_dottedNode = 4, RULE_functionOrVar = 5, RULE_methodArgs = 6, RULE_args = 7, 
		RULE_methodOrProperty = 8, RULE_projection = 9, RULE_selection = 10, RULE_startNode = 11, 
		RULE_literal = 12, RULE_parenspelExpr = 13, RULE_typeReference = 14, RULE_possiblyQualifiedId = 15, 
		RULE_nullReference = 16, RULE_constructorReference = 17, RULE_constructorArgs = 18, 
		RULE_inlineListOrMap = 19, RULE_listBindings = 20, RULE_listBinding = 21, 
		RULE_mapBindings = 22, RULE_mapBinding = 23, RULE_beanReference = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"script", "spelExpr", "node", "nonDottedNode", "dottedNode", "functionOrVar", 
			"methodArgs", "args", "methodOrProperty", "projection", "selection", 
			"startNode", "literal", "parenspelExpr", "typeReference", "possiblyQualifiedId", 
			"nullReference", "constructorReference", "constructorArgs", "inlineListOrMap", 
			"listBindings", "listBinding", "mapBindings", "mapBinding", "beanReference"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", null, "'++'", "'+'", "'--'", "'-'", "':'", "'.'", "','", 
			"'*'", "'/'", "'%'", "'('", "')'", "'['", "']'", "'#'", "'@'", "'^['", 
			"'^'", "'!='", "'!['", "'!'", "'=='", "'='", "'&&'", "'&'", "'||'", "'?['", 
			"'?:'", "'?.'", "'?'", "'$['", "'>='", "'>'", "'<='", "'<'", null, null, 
			null, "'or'", "'and'", "'true'", "'false'", "'new'", "'null'", "'T'", 
			"'matches'", "'gt'", "'ge'", "'le'", "'lt'", "'eq'", "'ne'", null, null, 
			null, null, null, null, null, "'``'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEMICOLON", "WS", "INC", "PLUS", "DEC", "MINUS", "COLON", "DOT", 
			"COMMA", "STAR", "DIV", "MOD", "LPAREN", "RPAREN", "LSQUARE", "RSQUARE", 
			"HASH", "BEAN_REF", "SELECT_FIRST", "POWER", "NE", "PROJECT", "NOT", 
			"EQ", "ASSIGN", "SYMBOLIC_AND", "FACTORY_BEAN_REF", "SYMBOLIC_OR", "SELECT", 
			"ELVIS", "SAFE_NAVI", "QMARK", "SELECT_LAST", "GE", "GT", "LE", "LT", 
			"LCURLY", "RCURLY", "BACKTICK", "OR", "AND", "TRUE", "FALSE", "NEW", 
			"NULL", "T", "MATCHES", "GT_KEYWORD", "GE_KEYWORD", "LE_KEYWORD", "LT_KEYWORD", 
			"EQ_KEYWORD", "NE_KEYWORD", "IDENTIFIER", "NUMERIC_LITERAL", "REAL_LITERAL", 
			"INTEGER_LITERAL", "STRING_LITERAL", "SINGLE_QUOTED_STRING", "DOUBLE_QUOTED_STRING", 
			"ESCAPED_BACKTICK", "SPEL_IN_TEMPLATE_STRING_OPEN", "TEMPLATE_TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SpelParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SpelParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScriptContext extends ParserRuleContext {
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SpelParser.EOF, 0); }
		public ScriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_script; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterScript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitScript(this);
		}
	}

	public final ScriptContext script() throws RecognitionException {
		ScriptContext _localctx = new ScriptContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_script);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			spelExpr(0);
			setState(51);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SpelExprContext extends ParserRuleContext {
		public List<SpelExprContext> spelExpr() {
			return getRuleContexts(SpelExprContext.class);
		}
		public SpelExprContext spelExpr(int i) {
			return getRuleContext(SpelExprContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(SpelParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SpelParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(SpelParser.NOT, 0); }
		public TerminalNode INC() { return getToken(SpelParser.INC, 0); }
		public TerminalNode DEC() { return getToken(SpelParser.DEC, 0); }
		public StartNodeContext startNode() {
			return getRuleContext(StartNodeContext.class,0);
		}
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public TerminalNode POWER() { return getToken(SpelParser.POWER, 0); }
		public TerminalNode STAR() { return getToken(SpelParser.STAR, 0); }
		public TerminalNode DIV() { return getToken(SpelParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(SpelParser.MOD, 0); }
		public TerminalNode GT() { return getToken(SpelParser.GT, 0); }
		public TerminalNode LT() { return getToken(SpelParser.LT, 0); }
		public TerminalNode LE() { return getToken(SpelParser.LE, 0); }
		public TerminalNode GE() { return getToken(SpelParser.GE, 0); }
		public TerminalNode EQ() { return getToken(SpelParser.EQ, 0); }
		public TerminalNode NE() { return getToken(SpelParser.NE, 0); }
		public TerminalNode GT_KEYWORD() { return getToken(SpelParser.GT_KEYWORD, 0); }
		public TerminalNode LT_KEYWORD() { return getToken(SpelParser.LT_KEYWORD, 0); }
		public TerminalNode LE_KEYWORD() { return getToken(SpelParser.LE_KEYWORD, 0); }
		public TerminalNode GE_KEYWORD() { return getToken(SpelParser.GE_KEYWORD, 0); }
		public TerminalNode EQ_KEYWORD() { return getToken(SpelParser.EQ_KEYWORD, 0); }
		public TerminalNode NE_KEYWORD() { return getToken(SpelParser.NE_KEYWORD, 0); }
		public TerminalNode AND() { return getToken(SpelParser.AND, 0); }
		public TerminalNode SYMBOLIC_AND() { return getToken(SpelParser.SYMBOLIC_AND, 0); }
		public TerminalNode OR() { return getToken(SpelParser.OR, 0); }
		public TerminalNode SYMBOLIC_OR() { return getToken(SpelParser.SYMBOLIC_OR, 0); }
		public TerminalNode MATCHES() { return getToken(SpelParser.MATCHES, 0); }
		public TerminalNode ASSIGN() { return getToken(SpelParser.ASSIGN, 0); }
		public TerminalNode ELVIS() { return getToken(SpelParser.ELVIS, 0); }
		public TerminalNode QMARK() { return getToken(SpelParser.QMARK, 0); }
		public TerminalNode COLON() { return getToken(SpelParser.COLON, 0); }
		public SpelExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_spelExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterSpelExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitSpelExpr(this);
		}
	}

	public final SpelExprContext spelExpr() throws RecognitionException {
		return spelExpr(0);
	}

	private SpelExprContext spelExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		SpelExprContext _localctx = new SpelExprContext(_ctx, _parentState);
		SpelExprContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_spelExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INC:
			case PLUS:
			case DEC:
			case MINUS:
			case NOT:
				{
				setState(54);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8388728L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(55);
				spelExpr(13);
				}
				break;
			case LPAREN:
			case HASH:
			case BEAN_REF:
			case SELECT_FIRST:
			case PROJECT:
			case FACTORY_BEAN_REF:
			case SELECT:
			case SELECT_LAST:
			case LCURLY:
			case TRUE:
			case FALSE:
			case NEW:
			case NULL:
			case T:
			case IDENTIFIER:
			case NUMERIC_LITERAL:
			case STRING_LITERAL:
				{
				setState(56);
				startNode();
				setState(60);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(57);
						node();
						}
						} 
					}
					setState(62);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(102);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(100);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(65);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(66);
						match(POWER);
						setState(67);
						spelExpr(12);
						}
						break;
					case 2:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(68);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(69);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7168L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(70);
						spelExpr(11);
						}
						break;
					case 3:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(71);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(72);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(73);
						spelExpr(10);
						}
						break;
					case 4:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(74);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(75);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 35466104782454784L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(76);
						spelExpr(9);
						}
						break;
					case 5:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(77);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(78);
						_la = _input.LA(1);
						if ( !(_la==SYMBOLIC_AND || _la==AND) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(79);
						spelExpr(8);
						}
						break;
					case 6:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(80);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(81);
						_la = _input.LA(1);
						if ( !(_la==SYMBOLIC_OR || _la==OR) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(82);
						spelExpr(7);
						}
						break;
					case 7:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(83);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(84);
						match(MATCHES);
						setState(85);
						spelExpr(6);
						}
						break;
					case 8:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(86);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(87);
						match(ASSIGN);
						setState(88);
						spelExpr(5);
						}
						break;
					case 9:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(89);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(90);
						match(ELVIS);
						setState(91);
						spelExpr(4);
						}
						break;
					case 10:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(92);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(93);
						match(QMARK);
						setState(94);
						spelExpr(0);
						setState(95);
						match(COLON);
						setState(96);
						spelExpr(3);
						}
						break;
					case 11:
						{
						_localctx = new SpelExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_spelExpr);
						setState(98);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(99);
						_la = _input.LA(1);
						if ( !(_la==INC || _la==DEC) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					} 
				}
				setState(104);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeContext extends ParserRuleContext {
		public DottedNodeContext dottedNode() {
			return getRuleContext(DottedNodeContext.class,0);
		}
		public NonDottedNodeContext nonDottedNode() {
			return getRuleContext(NonDottedNodeContext.class,0);
		}
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitNode(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_node);
		try {
			setState(107);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
			case HASH:
			case SELECT_FIRST:
			case PROJECT:
			case SELECT:
			case SAFE_NAVI:
			case SELECT_LAST:
				enterOuterAlt(_localctx, 1);
				{
				setState(105);
				dottedNode();
				}
				break;
			case LSQUARE:
				enterOuterAlt(_localctx, 2);
				{
				setState(106);
				nonDottedNode();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NonDottedNodeContext extends ParserRuleContext {
		public TerminalNode LSQUARE() { return getToken(SpelParser.LSQUARE, 0); }
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public TerminalNode RSQUARE() { return getToken(SpelParser.RSQUARE, 0); }
		public NonDottedNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonDottedNode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterNonDottedNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitNonDottedNode(this);
		}
	}

	public final NonDottedNodeContext nonDottedNode() throws RecognitionException {
		NonDottedNodeContext _localctx = new NonDottedNodeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_nonDottedNode);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(109);
			match(LSQUARE);
			setState(110);
			spelExpr(0);
			setState(111);
			match(RSQUARE);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DottedNodeContext extends ParserRuleContext {
		public MethodOrPropertyContext methodOrProperty() {
			return getRuleContext(MethodOrPropertyContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SpelParser.DOT, 0); }
		public TerminalNode SAFE_NAVI() { return getToken(SpelParser.SAFE_NAVI, 0); }
		public FunctionOrVarContext functionOrVar() {
			return getRuleContext(FunctionOrVarContext.class,0);
		}
		public ProjectionContext projection() {
			return getRuleContext(ProjectionContext.class,0);
		}
		public SelectionContext selection() {
			return getRuleContext(SelectionContext.class,0);
		}
		public DottedNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dottedNode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterDottedNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitDottedNode(this);
		}
	}

	public final DottedNodeContext dottedNode() throws RecognitionException {
		DottedNodeContext _localctx = new DottedNodeContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dottedNode);
		int _la;
		try {
			setState(118);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
			case SAFE_NAVI:
				enterOuterAlt(_localctx, 1);
				{
				setState(113);
				_la = _input.LA(1);
				if ( !(_la==DOT || _la==SAFE_NAVI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(114);
				methodOrProperty();
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 2);
				{
				setState(115);
				functionOrVar();
				}
				break;
			case PROJECT:
				enterOuterAlt(_localctx, 3);
				{
				setState(116);
				projection();
				}
				break;
			case SELECT_FIRST:
			case SELECT:
			case SELECT_LAST:
				enterOuterAlt(_localctx, 4);
				{
				setState(117);
				selection();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionOrVarContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(SpelParser.HASH, 0); }
		public TerminalNode IDENTIFIER() { return getToken(SpelParser.IDENTIFIER, 0); }
		public MethodArgsContext methodArgs() {
			return getRuleContext(MethodArgsContext.class,0);
		}
		public FunctionOrVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionOrVar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterFunctionOrVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitFunctionOrVar(this);
		}
	}

	public final FunctionOrVarContext functionOrVar() throws RecognitionException {
		FunctionOrVarContext _localctx = new FunctionOrVarContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionOrVar);
		try {
			setState(125);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(120);
				match(HASH);
				setState(121);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				match(HASH);
				setState(123);
				match(IDENTIFIER);
				setState(124);
				methodArgs();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodArgsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SpelParser.LPAREN, 0); }
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SpelParser.RPAREN, 0); }
		public MethodArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterMethodArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitMethodArgs(this);
		}
	}

	public final MethodArgsContext methodArgs() throws RecognitionException {
		MethodArgsContext _localctx = new MethodArgsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_methodArgs);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(LPAREN);
			setState(128);
			args();
			setState(129);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgsContext extends ParserRuleContext {
		public List<SpelExprContext> spelExpr() {
			return getRuleContexts(SpelExprContext.class);
		}
		public SpelExprContext spelExpr(int i) {
			return getRuleContext(SpelExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SpelParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SpelParser.COMMA, i);
		}
		public ArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_args; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitArgs(this);
		}
	}

	public final ArgsContext args() throws RecognitionException {
		ArgsContext _localctx = new ArgsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_args);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 684820106396442744L) != 0)) {
				{
				setState(131);
				spelExpr(0);
				}
			}

			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(134);
				match(COMMA);
				setState(135);
				spelExpr(0);
				}
				}
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodOrPropertyContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(SpelParser.IDENTIFIER, 0); }
		public MethodArgsContext methodArgs() {
			return getRuleContext(MethodArgsContext.class,0);
		}
		public MethodOrPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodOrProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterMethodOrProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitMethodOrProperty(this);
		}
	}

	public final MethodOrPropertyContext methodOrProperty() throws RecognitionException {
		MethodOrPropertyContext _localctx = new MethodOrPropertyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_methodOrProperty);
		try {
			setState(144);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(141);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				match(IDENTIFIER);
				setState(143);
				methodArgs();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProjectionContext extends ParserRuleContext {
		public TerminalNode PROJECT() { return getToken(SpelParser.PROJECT, 0); }
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public TerminalNode RSQUARE() { return getToken(SpelParser.RSQUARE, 0); }
		public ProjectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_projection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterProjection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitProjection(this);
		}
	}

	public final ProjectionContext projection() throws RecognitionException {
		ProjectionContext _localctx = new ProjectionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_projection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(PROJECT);
			setState(147);
			spelExpr(0);
			setState(148);
			match(RSQUARE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectionContext extends ParserRuleContext {
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public TerminalNode RSQUARE() { return getToken(SpelParser.RSQUARE, 0); }
		public TerminalNode SELECT() { return getToken(SpelParser.SELECT, 0); }
		public TerminalNode SELECT_FIRST() { return getToken(SpelParser.SELECT_FIRST, 0); }
		public TerminalNode SELECT_LAST() { return getToken(SpelParser.SELECT_LAST, 0); }
		public SelectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterSelection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitSelection(this);
		}
	}

	public final SelectionContext selection() throws RecognitionException {
		SelectionContext _localctx = new SelectionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_selection);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 9127329792L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(151);
			spelExpr(0);
			setState(152);
			match(RSQUARE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartNodeContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public ParenspelExprContext parenspelExpr() {
			return getRuleContext(ParenspelExprContext.class,0);
		}
		public TypeReferenceContext typeReference() {
			return getRuleContext(TypeReferenceContext.class,0);
		}
		public NullReferenceContext nullReference() {
			return getRuleContext(NullReferenceContext.class,0);
		}
		public ConstructorReferenceContext constructorReference() {
			return getRuleContext(ConstructorReferenceContext.class,0);
		}
		public MethodOrPropertyContext methodOrProperty() {
			return getRuleContext(MethodOrPropertyContext.class,0);
		}
		public FunctionOrVarContext functionOrVar() {
			return getRuleContext(FunctionOrVarContext.class,0);
		}
		public BeanReferenceContext beanReference() {
			return getRuleContext(BeanReferenceContext.class,0);
		}
		public ProjectionContext projection() {
			return getRuleContext(ProjectionContext.class,0);
		}
		public SelectionContext selection() {
			return getRuleContext(SelectionContext.class,0);
		}
		public InlineListOrMapContext inlineListOrMap() {
			return getRuleContext(InlineListOrMapContext.class,0);
		}
		public StartNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_startNode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterStartNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitStartNode(this);
		}
	}

	public final StartNodeContext startNode() throws RecognitionException {
		StartNodeContext _localctx = new StartNodeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_startNode);
		try {
			setState(165);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
			case FALSE:
			case NUMERIC_LITERAL:
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				literal();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(155);
				parenspelExpr();
				}
				break;
			case T:
				enterOuterAlt(_localctx, 3);
				{
				setState(156);
				typeReference();
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 4);
				{
				setState(157);
				nullReference();
				}
				break;
			case NEW:
				enterOuterAlt(_localctx, 5);
				{
				setState(158);
				constructorReference();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 6);
				{
				setState(159);
				methodOrProperty();
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 7);
				{
				setState(160);
				functionOrVar();
				}
				break;
			case BEAN_REF:
			case FACTORY_BEAN_REF:
				enterOuterAlt(_localctx, 8);
				{
				setState(161);
				beanReference();
				}
				break;
			case PROJECT:
				enterOuterAlt(_localctx, 9);
				{
				setState(162);
				projection();
				}
				break;
			case SELECT_FIRST:
			case SELECT:
			case SELECT_LAST:
				enterOuterAlt(_localctx, 10);
				{
				setState(163);
				selection();
				}
				break;
			case LCURLY:
				enterOuterAlt(_localctx, 11);
				{
				setState(164);
				inlineListOrMap();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode NUMERIC_LITERAL() { return getToken(SpelParser.NUMERIC_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SpelParser.STRING_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(SpelParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(SpelParser.FALSE, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 648544734620418048L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParenspelExprContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SpelParser.LPAREN, 0); }
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SpelParser.RPAREN, 0); }
		public ParenspelExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenspelExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterParenspelExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitParenspelExpr(this);
		}
	}

	public final ParenspelExprContext parenspelExpr() throws RecognitionException {
		ParenspelExprContext _localctx = new ParenspelExprContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_parenspelExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(LPAREN);
			setState(170);
			spelExpr(0);
			setState(171);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeReferenceContext extends ParserRuleContext {
		public TerminalNode T() { return getToken(SpelParser.T, 0); }
		public TerminalNode LPAREN() { return getToken(SpelParser.LPAREN, 0); }
		public PossiblyQualifiedIdContext possiblyQualifiedId() {
			return getRuleContext(PossiblyQualifiedIdContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SpelParser.RPAREN, 0); }
		public List<TerminalNode> LSQUARE() { return getTokens(SpelParser.LSQUARE); }
		public TerminalNode LSQUARE(int i) {
			return getToken(SpelParser.LSQUARE, i);
		}
		public List<TerminalNode> RSQUARE() { return getTokens(SpelParser.RSQUARE); }
		public TerminalNode RSQUARE(int i) {
			return getToken(SpelParser.RSQUARE, i);
		}
		public TypeReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterTypeReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitTypeReference(this);
		}
	}

	public final TypeReferenceContext typeReference() throws RecognitionException {
		TypeReferenceContext _localctx = new TypeReferenceContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_typeReference);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(T);
			setState(174);
			match(LPAREN);
			setState(175);
			possiblyQualifiedId();
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LSQUARE) {
				{
				{
				setState(176);
				match(LSQUARE);
				setState(177);
				match(RSQUARE);
				}
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(183);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PossiblyQualifiedIdContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(SpelParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(SpelParser.IDENTIFIER, i);
		}
		public List<TerminalNode> DOT() { return getTokens(SpelParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SpelParser.DOT, i);
		}
		public PossiblyQualifiedIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_possiblyQualifiedId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterPossiblyQualifiedId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitPossiblyQualifiedId(this);
		}
	}

	public final PossiblyQualifiedIdContext possiblyQualifiedId() throws RecognitionException {
		PossiblyQualifiedIdContext _localctx = new PossiblyQualifiedIdContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_possiblyQualifiedId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(IDENTIFIER);
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(186);
				match(DOT);
				setState(187);
				match(IDENTIFIER);
				}
				}
				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NullReferenceContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(SpelParser.NULL, 0); }
		public NullReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterNullReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitNullReference(this);
		}
	}

	public final NullReferenceContext nullReference() throws RecognitionException {
		NullReferenceContext _localctx = new NullReferenceContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_nullReference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstructorReferenceContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(SpelParser.NEW, 0); }
		public PossiblyQualifiedIdContext possiblyQualifiedId() {
			return getRuleContext(PossiblyQualifiedIdContext.class,0);
		}
		public List<TerminalNode> LSQUARE() { return getTokens(SpelParser.LSQUARE); }
		public TerminalNode LSQUARE(int i) {
			return getToken(SpelParser.LSQUARE, i);
		}
		public List<TerminalNode> RSQUARE() { return getTokens(SpelParser.RSQUARE); }
		public TerminalNode RSQUARE(int i) {
			return getToken(SpelParser.RSQUARE, i);
		}
		public InlineListOrMapContext inlineListOrMap() {
			return getRuleContext(InlineListOrMapContext.class,0);
		}
		public List<SpelExprContext> spelExpr() {
			return getRuleContexts(SpelExprContext.class);
		}
		public SpelExprContext spelExpr(int i) {
			return getRuleContext(SpelExprContext.class,i);
		}
		public ConstructorArgsContext constructorArgs() {
			return getRuleContext(ConstructorArgsContext.class,0);
		}
		public ConstructorReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterConstructorReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitConstructorReference(this);
		}
	}

	public final ConstructorReferenceContext constructorReference() throws RecognitionException {
		ConstructorReferenceContext _localctx = new ConstructorReferenceContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_constructorReference);
		int _la;
		try {
			int _alt;
			setState(213);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				match(NEW);
				setState(196);
				possiblyQualifiedId();
				setState(202); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(197);
						match(LSQUARE);
						setState(199);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 684820106396442744L) != 0)) {
							{
							setState(198);
							spelExpr(0);
							}
						}

						setState(201);
						match(RSQUARE);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(204); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(207);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(206);
					inlineListOrMap();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(209);
				match(NEW);
				setState(210);
				possiblyQualifiedId();
				setState(211);
				constructorArgs();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstructorArgsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(SpelParser.LPAREN, 0); }
		public ArgsContext args() {
			return getRuleContext(ArgsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SpelParser.RPAREN, 0); }
		public ConstructorArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterConstructorArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitConstructorArgs(this);
		}
	}

	public final ConstructorArgsContext constructorArgs() throws RecognitionException {
		ConstructorArgsContext _localctx = new ConstructorArgsContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_constructorArgs);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			match(LPAREN);
			setState(216);
			args();
			setState(217);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InlineListOrMapContext extends ParserRuleContext {
		public TerminalNode LCURLY() { return getToken(SpelParser.LCURLY, 0); }
		public TerminalNode RCURLY() { return getToken(SpelParser.RCURLY, 0); }
		public TerminalNode COLON() { return getToken(SpelParser.COLON, 0); }
		public ListBindingsContext listBindings() {
			return getRuleContext(ListBindingsContext.class,0);
		}
		public MapBindingsContext mapBindings() {
			return getRuleContext(MapBindingsContext.class,0);
		}
		public InlineListOrMapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineListOrMap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterInlineListOrMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitInlineListOrMap(this);
		}
	}

	public final InlineListOrMapContext inlineListOrMap() throws RecognitionException {
		InlineListOrMapContext _localctx = new InlineListOrMapContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_inlineListOrMap);
		try {
			setState(232);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(219);
				match(LCURLY);
				setState(220);
				match(RCURLY);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(221);
				match(LCURLY);
				setState(222);
				match(COLON);
				setState(223);
				match(RCURLY);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(224);
				match(LCURLY);
				setState(225);
				listBindings();
				setState(226);
				match(RCURLY);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(228);
				match(LCURLY);
				setState(229);
				mapBindings();
				setState(230);
				match(RCURLY);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ListBindingsContext extends ParserRuleContext {
		public ListBindingContext listBinding;
		public List<ListBindingContext> bindings = new ArrayList<ListBindingContext>();
		public List<ListBindingContext> listBinding() {
			return getRuleContexts(ListBindingContext.class);
		}
		public ListBindingContext listBinding(int i) {
			return getRuleContext(ListBindingContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SpelParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SpelParser.COMMA, i);
		}
		public ListBindingsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listBindings; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterListBindings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitListBindings(this);
		}
	}

	public final ListBindingsContext listBindings() throws RecognitionException {
		ListBindingsContext _localctx = new ListBindingsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_listBindings);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			((ListBindingsContext)_localctx).listBinding = listBinding();
			((ListBindingsContext)_localctx).bindings.add(((ListBindingsContext)_localctx).listBinding);
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(235);
				match(COMMA);
				setState(236);
				((ListBindingsContext)_localctx).listBinding = listBinding();
				((ListBindingsContext)_localctx).bindings.add(((ListBindingsContext)_localctx).listBinding);
				}
				}
				setState(241);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ListBindingContext extends ParserRuleContext {
		public SpelExprContext spelExpr() {
			return getRuleContext(SpelExprContext.class,0);
		}
		public ListBindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listBinding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterListBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitListBinding(this);
		}
	}

	public final ListBindingContext listBinding() throws RecognitionException {
		ListBindingContext _localctx = new ListBindingContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_listBinding);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			spelExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MapBindingsContext extends ParserRuleContext {
		public MapBindingContext mapBinding;
		public List<MapBindingContext> bindings = new ArrayList<MapBindingContext>();
		public List<MapBindingContext> mapBinding() {
			return getRuleContexts(MapBindingContext.class);
		}
		public MapBindingContext mapBinding(int i) {
			return getRuleContext(MapBindingContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SpelParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SpelParser.COMMA, i);
		}
		public MapBindingsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapBindings; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterMapBindings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitMapBindings(this);
		}
	}

	public final MapBindingsContext mapBindings() throws RecognitionException {
		MapBindingsContext _localctx = new MapBindingsContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_mapBindings);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			((MapBindingsContext)_localctx).mapBinding = mapBinding();
			((MapBindingsContext)_localctx).bindings.add(((MapBindingsContext)_localctx).mapBinding);
			setState(249);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(245);
				match(COMMA);
				setState(246);
				((MapBindingsContext)_localctx).mapBinding = mapBinding();
				((MapBindingsContext)_localctx).bindings.add(((MapBindingsContext)_localctx).mapBinding);
				}
				}
				setState(251);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MapBindingContext extends ParserRuleContext {
		public SpelExprContext key;
		public SpelExprContext value;
		public TerminalNode COLON() { return getToken(SpelParser.COLON, 0); }
		public List<SpelExprContext> spelExpr() {
			return getRuleContexts(SpelExprContext.class);
		}
		public SpelExprContext spelExpr(int i) {
			return getRuleContext(SpelExprContext.class,i);
		}
		public MapBindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapBinding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterMapBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitMapBinding(this);
		}
	}

	public final MapBindingContext mapBinding() throws RecognitionException {
		MapBindingContext _localctx = new MapBindingContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_mapBinding);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			((MapBindingContext)_localctx).key = spelExpr(0);
			setState(253);
			match(COLON);
			setState(254);
			((MapBindingContext)_localctx).value = spelExpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BeanReferenceContext extends ParserRuleContext {
		public TerminalNode BEAN_REF() { return getToken(SpelParser.BEAN_REF, 0); }
		public TerminalNode FACTORY_BEAN_REF() { return getToken(SpelParser.FACTORY_BEAN_REF, 0); }
		public TerminalNode IDENTIFIER() { return getToken(SpelParser.IDENTIFIER, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SpelParser.STRING_LITERAL, 0); }
		public BeanReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_beanReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).enterBeanReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SpelParserListener ) ((SpelParserListener)listener).exitBeanReference(this);
		}
	}

	public final BeanReferenceContext beanReference() throws RecognitionException {
		BeanReferenceContext _localctx = new BeanReferenceContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_beanReference);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			_la = _input.LA(1);
			if ( !(_la==BEAN_REF || _la==FACTORY_BEAN_REF) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(257);
			_la = _input.LA(1);
			if ( !(_la==IDENTIFIER || _la==STRING_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return spelExpr_sempred((SpelExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean spelExpr_sempred(SpelExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 11);
		case 1:
			return precpred(_ctx, 10);
		case 2:
			return precpred(_ctx, 9);
		case 3:
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 6);
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
		case 8:
			return precpred(_ctx, 3);
		case 9:
			return precpred(_ctx, 2);
		case 10:
			return precpred(_ctx, 12);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001@\u0104\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0005\u0001;\b\u0001\n\u0001\f\u0001>\t\u0001"+
		"\u0003\u0001@\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0005\u0001e\b\u0001\n\u0001\f\u0001h\t\u0001\u0001\u0002"+
		"\u0001\u0002\u0003\u0002l\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004w\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005~\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0003\u0007\u0085\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0005\u0007\u0089\b\u0007\n\u0007\f\u0007\u008c\t\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0003\b\u0091\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u00a6\b\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0005\u000e\u00b3\b\u000e\n\u000e\f\u000e\u00b6\t\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u00bd\b\u000f\n"+
		"\u000f\f\u000f\u00c0\t\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00c8\b\u0011\u0001\u0011\u0004"+
		"\u0011\u00cb\b\u0011\u000b\u0011\f\u0011\u00cc\u0001\u0011\u0003\u0011"+
		"\u00d0\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u00d6\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u00e9\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014"+
		"\u00ee\b\u0014\n\u0014\f\u0014\u00f1\t\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u00f8\b\u0016\n\u0016\f\u0016"+
		"\u00fb\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0000\u0001\u0002\u0019\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e"+
		" \"$&(*,.0\u0000\f\u0002\u0000\u0003\u0006\u0017\u0017\u0001\u0000\n\f"+
		"\u0002\u0000\u0004\u0004\u0006\u0006\u0004\u0000\u0015\u0015\u0018\u0018"+
		"\"%16\u0002\u0000\u001a\u001a**\u0002\u0000\u001c\u001c))\u0002\u0000"+
		"\u0003\u0003\u0005\u0005\u0002\u0000\b\b\u001f\u001f\u0003\u0000\u0013"+
		"\u0013\u001d\u001d!!\u0003\u0000+,88;;\u0002\u0000\u0012\u0012\u001b\u001b"+
		"\u0002\u000077;;\u0114\u00002\u0001\u0000\u0000\u0000\u0002?\u0001\u0000"+
		"\u0000\u0000\u0004k\u0001\u0000\u0000\u0000\u0006m\u0001\u0000\u0000\u0000"+
		"\bv\u0001\u0000\u0000\u0000\n}\u0001\u0000\u0000\u0000\f\u007f\u0001\u0000"+
		"\u0000\u0000\u000e\u0084\u0001\u0000\u0000\u0000\u0010\u0090\u0001\u0000"+
		"\u0000\u0000\u0012\u0092\u0001\u0000\u0000\u0000\u0014\u0096\u0001\u0000"+
		"\u0000\u0000\u0016\u00a5\u0001\u0000\u0000\u0000\u0018\u00a7\u0001\u0000"+
		"\u0000\u0000\u001a\u00a9\u0001\u0000\u0000\u0000\u001c\u00ad\u0001\u0000"+
		"\u0000\u0000\u001e\u00b9\u0001\u0000\u0000\u0000 \u00c1\u0001\u0000\u0000"+
		"\u0000\"\u00d5\u0001\u0000\u0000\u0000$\u00d7\u0001\u0000\u0000\u0000"+
		"&\u00e8\u0001\u0000\u0000\u0000(\u00ea\u0001\u0000\u0000\u0000*\u00f2"+
		"\u0001\u0000\u0000\u0000,\u00f4\u0001\u0000\u0000\u0000.\u00fc\u0001\u0000"+
		"\u0000\u00000\u0100\u0001\u0000\u0000\u000023\u0003\u0002\u0001\u0000"+
		"34\u0005\u0000\u0000\u00014\u0001\u0001\u0000\u0000\u000056\u0006\u0001"+
		"\uffff\uffff\u000067\u0007\u0000\u0000\u00007@\u0003\u0002\u0001\r8<\u0003"+
		"\u0016\u000b\u00009;\u0003\u0004\u0002\u0000:9\u0001\u0000\u0000\u0000"+
		";>\u0001\u0000\u0000\u0000<:\u0001\u0000\u0000\u0000<=\u0001\u0000\u0000"+
		"\u0000=@\u0001\u0000\u0000\u0000><\u0001\u0000\u0000\u0000?5\u0001\u0000"+
		"\u0000\u0000?8\u0001\u0000\u0000\u0000@f\u0001\u0000\u0000\u0000AB\n\u000b"+
		"\u0000\u0000BC\u0005\u0014\u0000\u0000Ce\u0003\u0002\u0001\fDE\n\n\u0000"+
		"\u0000EF\u0007\u0001\u0000\u0000Fe\u0003\u0002\u0001\u000bGH\n\t\u0000"+
		"\u0000HI\u0007\u0002\u0000\u0000Ie\u0003\u0002\u0001\nJK\n\b\u0000\u0000"+
		"KL\u0007\u0003\u0000\u0000Le\u0003\u0002\u0001\tMN\n\u0007\u0000\u0000"+
		"NO\u0007\u0004\u0000\u0000Oe\u0003\u0002\u0001\bPQ\n\u0006\u0000\u0000"+
		"QR\u0007\u0005\u0000\u0000Re\u0003\u0002\u0001\u0007ST\n\u0005\u0000\u0000"+
		"TU\u00050\u0000\u0000Ue\u0003\u0002\u0001\u0006VW\n\u0004\u0000\u0000"+
		"WX\u0005\u0019\u0000\u0000Xe\u0003\u0002\u0001\u0005YZ\n\u0003\u0000\u0000"+
		"Z[\u0005\u001e\u0000\u0000[e\u0003\u0002\u0001\u0004\\]\n\u0002\u0000"+
		"\u0000]^\u0005 \u0000\u0000^_\u0003\u0002\u0001\u0000_`\u0005\u0007\u0000"+
		"\u0000`a\u0003\u0002\u0001\u0003ae\u0001\u0000\u0000\u0000bc\n\f\u0000"+
		"\u0000ce\u0007\u0006\u0000\u0000dA\u0001\u0000\u0000\u0000dD\u0001\u0000"+
		"\u0000\u0000dG\u0001\u0000\u0000\u0000dJ\u0001\u0000\u0000\u0000dM\u0001"+
		"\u0000\u0000\u0000dP\u0001\u0000\u0000\u0000dS\u0001\u0000\u0000\u0000"+
		"dV\u0001\u0000\u0000\u0000dY\u0001\u0000\u0000\u0000d\\\u0001\u0000\u0000"+
		"\u0000db\u0001\u0000\u0000\u0000eh\u0001\u0000\u0000\u0000fd\u0001\u0000"+
		"\u0000\u0000fg\u0001\u0000\u0000\u0000g\u0003\u0001\u0000\u0000\u0000"+
		"hf\u0001\u0000\u0000\u0000il\u0003\b\u0004\u0000jl\u0003\u0006\u0003\u0000"+
		"ki\u0001\u0000\u0000\u0000kj\u0001\u0000\u0000\u0000l\u0005\u0001\u0000"+
		"\u0000\u0000mn\u0005\u000f\u0000\u0000no\u0003\u0002\u0001\u0000op\u0005"+
		"\u0010\u0000\u0000p\u0007\u0001\u0000\u0000\u0000qr\u0007\u0007\u0000"+
		"\u0000rw\u0003\u0010\b\u0000sw\u0003\n\u0005\u0000tw\u0003\u0012\t\u0000"+
		"uw\u0003\u0014\n\u0000vq\u0001\u0000\u0000\u0000vs\u0001\u0000\u0000\u0000"+
		"vt\u0001\u0000\u0000\u0000vu\u0001\u0000\u0000\u0000w\t\u0001\u0000\u0000"+
		"\u0000xy\u0005\u0011\u0000\u0000y~\u00057\u0000\u0000z{\u0005\u0011\u0000"+
		"\u0000{|\u00057\u0000\u0000|~\u0003\f\u0006\u0000}x\u0001\u0000\u0000"+
		"\u0000}z\u0001\u0000\u0000\u0000~\u000b\u0001\u0000\u0000\u0000\u007f"+
		"\u0080\u0005\r\u0000\u0000\u0080\u0081\u0003\u000e\u0007\u0000\u0081\u0082"+
		"\u0005\u000e\u0000\u0000\u0082\r\u0001\u0000\u0000\u0000\u0083\u0085\u0003"+
		"\u0002\u0001\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0084\u0085\u0001"+
		"\u0000\u0000\u0000\u0085\u008a\u0001\u0000\u0000\u0000\u0086\u0087\u0005"+
		"\t\u0000\u0000\u0087\u0089\u0003\u0002\u0001\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0089\u008c\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000"+
		"\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u000f\u0001\u0000"+
		"\u0000\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008d\u0091\u00057\u0000"+
		"\u0000\u008e\u008f\u00057\u0000\u0000\u008f\u0091\u0003\f\u0006\u0000"+
		"\u0090\u008d\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000"+
		"\u0091\u0011\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u0016\u0000\u0000"+
		"\u0093\u0094\u0003\u0002\u0001\u0000\u0094\u0095\u0005\u0010\u0000\u0000"+
		"\u0095\u0013\u0001\u0000\u0000\u0000\u0096\u0097\u0007\b\u0000\u0000\u0097"+
		"\u0098\u0003\u0002\u0001\u0000\u0098\u0099\u0005\u0010\u0000\u0000\u0099"+
		"\u0015\u0001\u0000\u0000\u0000\u009a\u00a6\u0003\u0018\f\u0000\u009b\u00a6"+
		"\u0003\u001a\r\u0000\u009c\u00a6\u0003\u001c\u000e\u0000\u009d\u00a6\u0003"+
		" \u0010\u0000\u009e\u00a6\u0003\"\u0011\u0000\u009f\u00a6\u0003\u0010"+
		"\b\u0000\u00a0\u00a6\u0003\n\u0005\u0000\u00a1\u00a6\u00030\u0018\u0000"+
		"\u00a2\u00a6\u0003\u0012\t\u0000\u00a3\u00a6\u0003\u0014\n\u0000\u00a4"+
		"\u00a6\u0003&\u0013\u0000\u00a5\u009a\u0001\u0000\u0000\u0000\u00a5\u009b"+
		"\u0001\u0000\u0000\u0000\u00a5\u009c\u0001\u0000\u0000\u0000\u00a5\u009d"+
		"\u0001\u0000\u0000\u0000\u00a5\u009e\u0001\u0000\u0000\u0000\u00a5\u009f"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a0\u0001\u0000\u0000\u0000\u00a5\u00a1"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a2\u0001\u0000\u0000\u0000\u00a5\u00a3"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a4\u0001\u0000\u0000\u0000\u00a6\u0017"+
		"\u0001\u0000\u0000\u0000\u00a7\u00a8\u0007\t\u0000\u0000\u00a8\u0019\u0001"+
		"\u0000\u0000\u0000\u00a9\u00aa\u0005\r\u0000\u0000\u00aa\u00ab\u0003\u0002"+
		"\u0001\u0000\u00ab\u00ac\u0005\u000e\u0000\u0000\u00ac\u001b\u0001\u0000"+
		"\u0000\u0000\u00ad\u00ae\u0005/\u0000\u0000\u00ae\u00af\u0005\r\u0000"+
		"\u0000\u00af\u00b4\u0003\u001e\u000f\u0000\u00b0\u00b1\u0005\u000f\u0000"+
		"\u0000\u00b1\u00b3\u0005\u0010\u0000\u0000\u00b2\u00b0\u0001\u0000\u0000"+
		"\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000"+
		"\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00b8\u0005\u000e\u0000"+
		"\u0000\u00b8\u001d\u0001\u0000\u0000\u0000\u00b9\u00be\u00057\u0000\u0000"+
		"\u00ba\u00bb\u0005\b\u0000\u0000\u00bb\u00bd\u00057\u0000\u0000\u00bc"+
		"\u00ba\u0001\u0000\u0000\u0000\u00bd\u00c0\u0001\u0000\u0000\u0000\u00be"+
		"\u00bc\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf"+
		"\u001f\u0001\u0000\u0000\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c1"+
		"\u00c2\u0005.\u0000\u0000\u00c2!\u0001\u0000\u0000\u0000\u00c3\u00c4\u0005"+
		"-\u0000\u0000\u00c4\u00ca\u0003\u001e\u000f\u0000\u00c5\u00c7\u0005\u000f"+
		"\u0000\u0000\u00c6\u00c8\u0003\u0002\u0001\u0000\u00c7\u00c6\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000"+
		"\u0000\u0000\u00c9\u00cb\u0005\u0010\u0000\u0000\u00ca\u00c5\u0001\u0000"+
		"\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000"+
		"\u0000\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00cf\u0001\u0000"+
		"\u0000\u0000\u00ce\u00d0\u0003&\u0013\u0000\u00cf\u00ce\u0001\u0000\u0000"+
		"\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d1\u00d2\u0005-\u0000\u0000\u00d2\u00d3\u0003\u001e\u000f\u0000"+
		"\u00d3\u00d4\u0003$\u0012\u0000\u00d4\u00d6\u0001\u0000\u0000\u0000\u00d5"+
		"\u00c3\u0001\u0000\u0000\u0000\u00d5\u00d1\u0001\u0000\u0000\u0000\u00d6"+
		"#\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005\r\u0000\u0000\u00d8\u00d9"+
		"\u0003\u000e\u0007\u0000\u00d9\u00da\u0005\u000e\u0000\u0000\u00da%\u0001"+
		"\u0000\u0000\u0000\u00db\u00dc\u0005&\u0000\u0000\u00dc\u00e9\u0005\'"+
		"\u0000\u0000\u00dd\u00de\u0005&\u0000\u0000\u00de\u00df\u0005\u0007\u0000"+
		"\u0000\u00df\u00e9\u0005\'\u0000\u0000\u00e0\u00e1\u0005&\u0000\u0000"+
		"\u00e1\u00e2\u0003(\u0014\u0000\u00e2\u00e3\u0005\'\u0000\u0000\u00e3"+
		"\u00e9\u0001\u0000\u0000\u0000\u00e4\u00e5\u0005&\u0000\u0000\u00e5\u00e6"+
		"\u0003,\u0016\u0000\u00e6\u00e7\u0005\'\u0000\u0000\u00e7\u00e9\u0001"+
		"\u0000\u0000\u0000\u00e8\u00db\u0001\u0000\u0000\u0000\u00e8\u00dd\u0001"+
		"\u0000\u0000\u0000\u00e8\u00e0\u0001\u0000\u0000\u0000\u00e8\u00e4\u0001"+
		"\u0000\u0000\u0000\u00e9\'\u0001\u0000\u0000\u0000\u00ea\u00ef\u0003*"+
		"\u0015\u0000\u00eb\u00ec\u0005\t\u0000\u0000\u00ec\u00ee\u0003*\u0015"+
		"\u0000\u00ed\u00eb\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000\u0000"+
		"\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000"+
		"\u0000\u00f0)\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000"+
		"\u00f2\u00f3\u0003\u0002\u0001\u0000\u00f3+\u0001\u0000\u0000\u0000\u00f4"+
		"\u00f9\u0003.\u0017\u0000\u00f5\u00f6\u0005\t\u0000\u0000\u00f6\u00f8"+
		"\u0003.\u0017\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000\u00f8\u00fb\u0001"+
		"\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001"+
		"\u0000\u0000\u0000\u00fa-\u0001\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000"+
		"\u0000\u0000\u00fc\u00fd\u0003\u0002\u0001\u0000\u00fd\u00fe\u0005\u0007"+
		"\u0000\u0000\u00fe\u00ff\u0003\u0002\u0001\u0000\u00ff/\u0001\u0000\u0000"+
		"\u0000\u0100\u0101\u0007\n\u0000\u0000\u0101\u0102\u0007\u000b\u0000\u0000"+
		"\u01021\u0001\u0000\u0000\u0000\u0014<?dfkv}\u0084\u008a\u0090\u00a5\u00b4"+
		"\u00be\u00c7\u00cc\u00cf\u00d5\u00e8\u00ef\u00f9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}