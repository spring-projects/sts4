// Generated from JavaProperties.g4 by ANTLR 4.5.3
package org.springframework.ide.vscode.properties.antlr.parser;
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
		Backslash=1, Colon=2, Equals=3, Comment=4, LineBreak=5, Space=6, IdentifierChar=7;
	public static final int
		RULE_parse = 0, RULE_line = 1, RULE_propertyLine = 2, RULE_commentLine = 3, 
		RULE_emptyLine = 4, RULE_keyValuePair = 5, RULE_key = 6, RULE_keyChar = 7, 
		RULE_separatorAndValue = 8, RULE_valueChar = 9;
	public static final String[] ruleNames = {
		"parse", "line", "propertyLine", "commentLine", "emptyLine", "keyValuePair", 
		"key", "keyChar", "separatorAndValue", "valueChar"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\\'", "':'", "'='"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "Backslash", "Colon", "Equals", "Comment", "LineBreak", "Space", 
		"IdentifierChar"
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
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Comment) | (1L << LineBreak) | (1L << Space) | (1L << IdentifierChar))) != 0)) {
				{
				{
				setState(20);
				line();
				}
				}
				setState(25);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(26);
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
			setState(31);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				propertyLine();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(29);
				commentLine();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(30);
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
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(33);
				match(Space);
				}
				}
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(39);
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
		public TerminalNode Comment() { return getToken(JavaPropertiesParser.Comment, 0); }
		public TerminalNode LineBreak() { return getToken(JavaPropertiesParser.LineBreak, 0); }
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
			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(41);
				match(Space);
				}
				}
				setState(46);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(47);
			match(Comment);
			setState(48);
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
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Space) {
				{
				{
				setState(50);
				match(Space);
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(56);
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
			setState(58);
			key();
			setState(59);
			separatorAndValue();
			setState(60);
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
			setState(63); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(62);
				keyChar();
				}
				}
				setState(65); 
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
			setState(70);
			switch (_input.LA(1)) {
			case IdentifierChar:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				match(IdentifierChar);
				}
				break;
			case Backslash:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				match(Backslash);
				setState(69);
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
			setState(72);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Colon) | (1L << Equals) | (1L << Space))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(74); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(73);
				valueChar();
				}
				}
				setState(76); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Backslash) | (1L << Colon) | (1L << Equals) | (1L << Space) | (1L << IdentifierChar))) != 0) );
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
		public TerminalNode Space() { return getToken(JavaPropertiesParser.Space, 0); }
		public TerminalNode Backslash() { return getToken(JavaPropertiesParser.Backslash, 0); }
		public TerminalNode LineBreak() { return getToken(JavaPropertiesParser.LineBreak, 0); }
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
			setState(84);
			switch (_input.LA(1)) {
			case IdentifierChar:
				enterOuterAlt(_localctx, 1);
				{
				setState(78);
				match(IdentifierChar);
				}
				break;
			case Space:
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				match(Space);
				}
				break;
			case Backslash:
				enterOuterAlt(_localctx, 3);
				{
				setState(80);
				match(Backslash);
				setState(81);
				match(LineBreak);
				}
				break;
			case Equals:
				enterOuterAlt(_localctx, 4);
				{
				setState(82);
				match(Equals);
				}
				break;
			case Colon:
				enterOuterAlt(_localctx, 5);
				{
				setState(83);
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

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\tY\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\3"+
		"\2\7\2\30\n\2\f\2\16\2\33\13\2\3\2\3\2\3\3\3\3\3\3\5\3\"\n\3\3\4\7\4%"+
		"\n\4\f\4\16\4(\13\4\3\4\3\4\3\5\7\5-\n\5\f\5\16\5\60\13\5\3\5\3\5\3\5"+
		"\3\6\7\6\66\n\6\f\6\16\69\13\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b\6\bB\n\b\r"+
		"\b\16\bC\3\t\3\t\3\t\5\tI\n\t\3\n\3\n\6\nM\n\n\r\n\16\nN\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\5\13W\n\13\3\13\2\2\f\2\4\6\b\n\f\16\20\22\24\2\5\3\3"+
		"\7\7\3\2\4\5\4\2\4\5\b\b[\2\31\3\2\2\2\4!\3\2\2\2\6&\3\2\2\2\b.\3\2\2"+
		"\2\n\67\3\2\2\2\f<\3\2\2\2\16A\3\2\2\2\20H\3\2\2\2\22J\3\2\2\2\24V\3\2"+
		"\2\2\26\30\5\4\3\2\27\26\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2"+
		"\2\2\32\34\3\2\2\2\33\31\3\2\2\2\34\35\7\2\2\3\35\3\3\2\2\2\36\"\5\6\4"+
		"\2\37\"\5\b\5\2 \"\5\n\6\2!\36\3\2\2\2!\37\3\2\2\2! \3\2\2\2\"\5\3\2\2"+
		"\2#%\7\b\2\2$#\3\2\2\2%(\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\')\3\2\2\2(&\3\2"+
		"\2\2)*\5\f\7\2*\7\3\2\2\2+-\7\b\2\2,+\3\2\2\2-\60\3\2\2\2.,\3\2\2\2./"+
		"\3\2\2\2/\61\3\2\2\2\60.\3\2\2\2\61\62\7\6\2\2\62\63\t\2\2\2\63\t\3\2"+
		"\2\2\64\66\7\b\2\2\65\64\3\2\2\2\669\3\2\2\2\67\65\3\2\2\2\678\3\2\2\2"+
		"8:\3\2\2\29\67\3\2\2\2:;\7\7\2\2;\13\3\2\2\2<=\5\16\b\2=>\5\22\n\2>?\t"+
		"\2\2\2?\r\3\2\2\2@B\5\20\t\2A@\3\2\2\2BC\3\2\2\2CA\3\2\2\2CD\3\2\2\2D"+
		"\17\3\2\2\2EI\7\t\2\2FG\7\3\2\2GI\t\3\2\2HE\3\2\2\2HF\3\2\2\2I\21\3\2"+
		"\2\2JL\t\4\2\2KM\5\24\13\2LK\3\2\2\2MN\3\2\2\2NL\3\2\2\2NO\3\2\2\2O\23"+
		"\3\2\2\2PW\7\t\2\2QW\7\b\2\2RS\7\3\2\2SW\7\7\2\2TW\7\5\2\2UW\7\4\2\2V"+
		"P\3\2\2\2VQ\3\2\2\2VR\3\2\2\2VT\3\2\2\2VU\3\2\2\2W\25\3\2\2\2\13\31!&"+
		".\67CHNV";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}