// Generated from JavaProperties.g4 by ANTLR 4.5.3
package org.springframework.ide.vscode.java.properties.antlr.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JavaPropertiesParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Backslash=1, Colon=2, Equals=3, Exclamation=4, Number=5, LineBreak=6, 
		Space=7, IdentifierChar=8;
	public static final int
		RULE_parse = 0, RULE_line = 1, RULE_propertyLine = 2, RULE_commentLine = 3, 
		RULE_emptyLine = 4, RULE_keyValuePair = 5, RULE_key = 6, RULE_keyChar = 7, 
		RULE_separatorAndValue = 8, RULE_valueChar = 9, RULE_anyChar = 10;
	public static final String[] ruleNames = {
		"parse", "line", "propertyLine", "commentLine", "emptyLine", "keyValuePair", 
		"key", "keyChar", "separatorAndValue", "valueChar", "anyChar"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\\'", "':'", "'='", "'!'", "'#'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "Backslash", "Colon", "Equals", "Exclamation", "Number", "LineBreak", 
		"Space", "IdentifierChar"
	};
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
	public String getGrammarFileName() { return "JavaProperties.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JavaPropertiesParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(JavaPropertiesParser.EOF, 0); }
		public List<LineContext> line() {
			return getRuleContexts(LineContext.class);
		}
		public LineContext line(int i) {
			return getRuleContext(LineContext.class,i);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitParse(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Exclamation) | (1L << Number) | (1L << LineBreak) | (1L << Space) | (1L << IdentifierChar))) != 0)) {
				{
				{
				setState(22);
				line();
				}
				}
				setState(27);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(28);
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

	public static class LineContext extends ParserRuleContext {
		public PropertyLineContext propertyLine() {
			return getRuleContext(PropertyLineContext.class,0);
		}
		public CommentLineContext commentLine() {
			return getRuleContext(CommentLineContext.class,0);
		}
		public EmptyLineContext emptyLine() {
			return getRuleContext(EmptyLineContext.class,0);
		}
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_line);
		try {
			setState(33);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(30);
				propertyLine();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				commentLine();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(32);
				emptyLine();
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

	public static class PropertyLineContext extends ParserRuleContext {
		public KeyValuePairContext keyValuePair() {
			return getRuleContext(KeyValuePairContext.class,0);
		}
		public List<TerminalNode> Space() { return getTokens(JavaPropertiesParser.Space); }
		public TerminalNode Space(int i) {
			return getToken(JavaPropertiesParser.Space, i);
		}
		public PropertyLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterPropertyLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitPropertyLine(this);
		}
	}

	public final PropertyLineContext propertyLine() throws RecognitionException {
		PropertyLineContext _localctx = new PropertyLineContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_propertyLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(35);
				match(Space);
				}
				}
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(41);
			keyValuePair();
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

	public static class CommentLineContext extends ParserRuleContext {
		public TerminalNode Exclamation() { return getToken(JavaPropertiesParser.Exclamation, 0); }
		public TerminalNode Number() { return getToken(JavaPropertiesParser.Number, 0); }
		public List<TerminalNode> LineBreak() { return getTokens(JavaPropertiesParser.LineBreak); }
		public TerminalNode LineBreak(int i) {
			return getToken(JavaPropertiesParser.LineBreak, i);
		}
		public TerminalNode EOF() { return getToken(JavaPropertiesParser.EOF, 0); }
		public List<TerminalNode> Space() { return getTokens(JavaPropertiesParser.Space); }
		public TerminalNode Space(int i) {
			return getToken(JavaPropertiesParser.Space, i);
		}
		public CommentLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterCommentLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitCommentLine(this);
		}
	}

	public final CommentLineContext commentLine() throws RecognitionException {
		CommentLineContext _localctx = new CommentLineContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_commentLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(43);
				match(Space);
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(49);
			_la = _input.LA(1);
			if ( !(_la==Exclamation || _la==Number) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Colon) | (1L << Equals) | (1L << Exclamation) | (1L << Number) | (1L << Space) | (1L << IdentifierChar))) != 0)) {
				{
				{
				setState(50);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==LineBreak) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(56);
			_la = _input.LA(1);
			if ( !(_la==EOF || _la==LineBreak) ) {
			_errHandler.recoverInline(this);
			} else {
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

	public static class EmptyLineContext extends ParserRuleContext {
		public TerminalNode LineBreak() { return getToken(JavaPropertiesParser.LineBreak, 0); }
		public List<TerminalNode> Space() { return getTokens(JavaPropertiesParser.Space); }
		public TerminalNode Space(int i) {
			return getToken(JavaPropertiesParser.Space, i);
		}
		public EmptyLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptyLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterEmptyLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitEmptyLine(this);
		}
	}

	public final EmptyLineContext emptyLine() throws RecognitionException {
		EmptyLineContext _localctx = new EmptyLineContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_emptyLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(58);
				match(Space);
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			match(LineBreak);
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

	public static class KeyValuePairContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public SeparatorAndValueContext separatorAndValue() {
			return getRuleContext(SeparatorAndValueContext.class,0);
		}
		public TerminalNode LineBreak() { return getToken(JavaPropertiesParser.LineBreak, 0); }
		public TerminalNode EOF() { return getToken(JavaPropertiesParser.EOF, 0); }
		public KeyValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterKeyValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitKeyValuePair(this);
		}
	}

	public final KeyValuePairContext keyValuePair() throws RecognitionException {
		KeyValuePairContext _localctx = new KeyValuePairContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_keyValuePair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			key();
			setState(67);
			separatorAndValue();
			setState(68);
			_la = _input.LA(1);
			if ( !(_la==EOF || _la==LineBreak) ) {
			_errHandler.recoverInline(this);
			} else {
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

	public static class KeyContext extends ParserRuleContext {
		public List<KeyCharContext> keyChar() {
			return getRuleContexts(KeyCharContext.class);
		}
		public KeyCharContext keyChar(int i) {
			return getRuleContext(KeyCharContext.class,i);
		}
		public KeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitKey(this);
		}
	}

	public final KeyContext key() throws RecognitionException {
		KeyContext _localctx = new KeyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_key);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(70);
				keyChar();
				}
				}
				setState(73); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Backslash || _la==IdentifierChar );
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

	public static class KeyCharContext extends ParserRuleContext {
		public TerminalNode IdentifierChar() { return getToken(JavaPropertiesParser.IdentifierChar, 0); }
		public TerminalNode Backslash() { return getToken(JavaPropertiesParser.Backslash, 0); }
		public TerminalNode Colon() { return getToken(JavaPropertiesParser.Colon, 0); }
		public TerminalNode Equals() { return getToken(JavaPropertiesParser.Equals, 0); }
		public KeyCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterKeyChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitKeyChar(this);
		}
	}

	public final KeyCharContext keyChar() throws RecognitionException {
		KeyCharContext _localctx = new KeyCharContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_keyChar);
		int _la;
		try {
			setState(78);
			switch (_input.LA(1)) {
			case IdentifierChar:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				match(IdentifierChar);
				}
				break;
			case Backslash:
				enterOuterAlt(_localctx, 2);
				{
				setState(76);
				match(Backslash);
				setState(77);
				_la = _input.LA(1);
				if ( !(_la==Colon || _la==Equals) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
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

	public static class SeparatorAndValueContext extends ParserRuleContext {
		public TerminalNode Space() { return getToken(JavaPropertiesParser.Space, 0); }
		public TerminalNode Colon() { return getToken(JavaPropertiesParser.Colon, 0); }
		public TerminalNode Equals() { return getToken(JavaPropertiesParser.Equals, 0); }
		public List<ValueCharContext> valueChar() {
			return getRuleContexts(ValueCharContext.class);
		}
		public ValueCharContext valueChar(int i) {
			return getRuleContext(ValueCharContext.class,i);
		}
		public SeparatorAndValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_separatorAndValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterSeparatorAndValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitSeparatorAndValue(this);
		}
	}

	public final SeparatorAndValueContext separatorAndValue() throws RecognitionException {
		SeparatorAndValueContext _localctx = new SeparatorAndValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_separatorAndValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Colon) | (1L << Equals) | (1L << Space))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Colon) | (1L << Equals) | (1L << Exclamation) | (1L << Number) | (1L << Space) | (1L << IdentifierChar))) != 0)) {
				{
				{
				setState(81);
				valueChar();
				}
				}
				setState(86);
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

	public static class ValueCharContext extends ParserRuleContext {
		public TerminalNode IdentifierChar() { return getToken(JavaPropertiesParser.IdentifierChar, 0); }
		public TerminalNode Exclamation() { return getToken(JavaPropertiesParser.Exclamation, 0); }
		public TerminalNode Number() { return getToken(JavaPropertiesParser.Number, 0); }
		public TerminalNode Space() { return getToken(JavaPropertiesParser.Space, 0); }
		public TerminalNode Backslash() { return getToken(JavaPropertiesParser.Backslash, 0); }
		public AnyCharContext anyChar() {
			return getRuleContext(AnyCharContext.class,0);
		}
		public TerminalNode Equals() { return getToken(JavaPropertiesParser.Equals, 0); }
		public TerminalNode Colon() { return getToken(JavaPropertiesParser.Colon, 0); }
		public ValueCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterValueChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitValueChar(this);
		}
	}

	public final ValueCharContext valueChar() throws RecognitionException {
		ValueCharContext _localctx = new ValueCharContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_valueChar);
		try {
			setState(95);
			switch (_input.LA(1)) {
			case IdentifierChar:
				enterOuterAlt(_localctx, 1);
				{
				setState(87);
				match(IdentifierChar);
				}
				break;
			case Exclamation:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				match(Exclamation);
				}
				break;
			case Number:
				enterOuterAlt(_localctx, 3);
				{
				setState(89);
				match(Number);
				}
				break;
			case Space:
				enterOuterAlt(_localctx, 4);
				{
				setState(90);
				match(Space);
				}
				break;
			case Backslash:
				enterOuterAlt(_localctx, 5);
				{
				setState(91);
				match(Backslash);
				setState(92);
				anyChar();
				}
				break;
			case Equals:
				enterOuterAlt(_localctx, 6);
				{
				setState(93);
				match(Equals);
				}
				break;
			case Colon:
				enterOuterAlt(_localctx, 7);
				{
				setState(94);
				match(Colon);
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

	public static class AnyCharContext extends ParserRuleContext {
		public TerminalNode LineBreak() { return getToken(JavaPropertiesParser.LineBreak, 0); }
		public TerminalNode IdentifierChar() { return getToken(JavaPropertiesParser.IdentifierChar, 0); }
		public TerminalNode Backslash() { return getToken(JavaPropertiesParser.Backslash, 0); }
		public TerminalNode Colon() { return getToken(JavaPropertiesParser.Colon, 0); }
		public TerminalNode Equals() { return getToken(JavaPropertiesParser.Equals, 0); }
		public TerminalNode Space() { return getToken(JavaPropertiesParser.Space, 0); }
		public TerminalNode Exclamation() { return getToken(JavaPropertiesParser.Exclamation, 0); }
		public TerminalNode Number() { return getToken(JavaPropertiesParser.Number, 0); }
		public AnyCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).enterAnyChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JavaPropertiesListener ) ((JavaPropertiesListener)listener).exitAnyChar(this);
		}
	}

	public final AnyCharContext anyChar() throws RecognitionException {
		AnyCharContext _localctx = new AnyCharContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_anyChar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Colon) | (1L << Equals) | (1L << Exclamation) | (1L << Number) | (1L << LineBreak) | (1L << Space) | (1L << IdentifierChar))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
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

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\nf\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\3\2\7\2\32\n\2\f\2\16\2\35\13\2\3\2\3\2\3\3\3\3\3\3\5\3$\n\3\3"+
		"\4\7\4\'\n\4\f\4\16\4*\13\4\3\4\3\4\3\5\7\5/\n\5\f\5\16\5\62\13\5\3\5"+
		"\3\5\7\5\66\n\5\f\5\16\59\13\5\3\5\3\5\3\6\7\6>\n\6\f\6\16\6A\13\6\3\6"+
		"\3\6\3\7\3\7\3\7\3\7\3\b\6\bJ\n\b\r\b\16\bK\3\t\3\t\3\t\5\tQ\n\t\3\n\3"+
		"\n\7\nU\n\n\f\n\16\nX\13\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13"+
		"b\n\13\3\f\3\f\3\f\2\2\r\2\4\6\b\n\f\16\20\22\24\26\2\b\3\2\6\7\3\2\b"+
		"\b\3\3\b\b\3\2\4\5\4\2\4\5\t\t\3\2\3\nj\2\33\3\2\2\2\4#\3\2\2\2\6(\3\2"+
		"\2\2\b\60\3\2\2\2\n?\3\2\2\2\fD\3\2\2\2\16I\3\2\2\2\20P\3\2\2\2\22R\3"+
		"\2\2\2\24a\3\2\2\2\26c\3\2\2\2\30\32\5\4\3\2\31\30\3\2\2\2\32\35\3\2\2"+
		"\2\33\31\3\2\2\2\33\34\3\2\2\2\34\36\3\2\2\2\35\33\3\2\2\2\36\37\7\2\2"+
		"\3\37\3\3\2\2\2 $\5\6\4\2!$\5\b\5\2\"$\5\n\6\2# \3\2\2\2#!\3\2\2\2#\""+
		"\3\2\2\2$\5\3\2\2\2%\'\7\t\2\2&%\3\2\2\2\'*\3\2\2\2(&\3\2\2\2()\3\2\2"+
		"\2)+\3\2\2\2*(\3\2\2\2+,\5\f\7\2,\7\3\2\2\2-/\7\t\2\2.-\3\2\2\2/\62\3"+
		"\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\63\3\2\2\2\62\60\3\2\2\2\63\67\t\2"+
		"\2\2\64\66\n\3\2\2\65\64\3\2\2\2\669\3\2\2\2\67\65\3\2\2\2\678\3\2\2\2"+
		"8:\3\2\2\29\67\3\2\2\2:;\t\4\2\2;\t\3\2\2\2<>\7\t\2\2=<\3\2\2\2>A\3\2"+
		"\2\2?=\3\2\2\2?@\3\2\2\2@B\3\2\2\2A?\3\2\2\2BC\7\b\2\2C\13\3\2\2\2DE\5"+
		"\16\b\2EF\5\22\n\2FG\t\4\2\2G\r\3\2\2\2HJ\5\20\t\2IH\3\2\2\2JK\3\2\2\2"+
		"KI\3\2\2\2KL\3\2\2\2L\17\3\2\2\2MQ\7\n\2\2NO\7\3\2\2OQ\t\5\2\2PM\3\2\2"+
		"\2PN\3\2\2\2Q\21\3\2\2\2RV\t\6\2\2SU\5\24\13\2TS\3\2\2\2UX\3\2\2\2VT\3"+
		"\2\2\2VW\3\2\2\2W\23\3\2\2\2XV\3\2\2\2Yb\7\n\2\2Zb\7\6\2\2[b\7\7\2\2\\"+
		"b\7\t\2\2]^\7\3\2\2^b\5\26\f\2_b\7\5\2\2`b\7\4\2\2aY\3\2\2\2aZ\3\2\2\2"+
		"a[\3\2\2\2a\\\3\2\2\2a]\3\2\2\2a_\3\2\2\2a`\3\2\2\2b\25\3\2\2\2cd\t\7"+
		"\2\2d\27\3\2\2\2\f\33#(\60\67?KPVa";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}