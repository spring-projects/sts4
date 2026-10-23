// Generated from Hql.g4 by ANTLR 4.13.2
package org.springframework.ide.vscode.parser.hql;

/**
 * HQL per https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#query-language
 *
 * This is a mixture of Hibernate's BNF and missing bits of grammar. There are gaps and inconsistencies in the
 * BNF itself, explained by other fragments of their spec. Additionally, alternate labels are used to provide easier
 * management of complex rules in the generated Visitor. Finally, there are labels applied to rule elements (op=('+'|'-')
 * to simplify the processing.
 *
 * @author Greg Turnquist
 * @since 3.1
 */

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class HqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, WS=24, ALL=25, 
		AND=26, ANY=27, AS=28, ASC=29, ASTERISK=30, AVG=31, BETWEEN=32, BOTH=33, 
		BREADTH=34, BY=35, CASE=36, CAST=37, CEILING=38, COLLATE=39, COUNT=40, 
		CROSS=41, CUBE=42, CURRENT=43, CURRENT_DATE=44, CURRENT_INSTANT=45, CURRENT_TIME=46, 
		CURRENT_TIMESTAMP=47, CYCLE=48, DATE=49, DATETIME=50, DAY=51, DEFAULT=52, 
		DELETE=53, DEPTH=54, DESC=55, DISTINCT=56, ELEMENT=57, ELEMENTS=58, ELSE=59, 
		EMPTY=60, END=61, ENTRY=62, EPOCH=63, ERROR=64, ESCAPE=65, EVERY=66, EXCEPT=67, 
		EXCLUDE=68, EXISTS=69, EXP=70, EXTRACT=71, FALSE=72, FETCH=73, FILTER=74, 
		FIRST=75, FK=76, FLOOR=77, FOLLOWING=78, FOR=79, FORMAT=80, FROM=81, FULL=82, 
		FUNCTION=83, GROUP=84, GROUPS=85, HAVING=86, HOUR=87, ID=88, IGNORE=89, 
		ILIKE=90, IN=91, INDEX=92, INDICES=93, INNER=94, INSERT=95, INSTANT=96, 
		INTERSECT=97, INTO=98, IS=99, JOIN=100, KEY=101, LAST=102, LATERAL=103, 
		LEADING=104, LEFT=105, LIKE=106, LIMIT=107, LIST=108, LISTAGG=109, LN=110, 
		LOCAL=111, LOCAL_DATE=112, LOCAL_DATETIME=113, LOCAL_TIME=114, MAP=115, 
		MATERIALIZED=116, MAX=117, MAXELEMENT=118, MAXINDEX=119, MEMBER=120, MICROSECOND=121, 
		MILLISECOND=122, MIN=123, MINELEMENT=124, MININDEX=125, MINUTE=126, MONTH=127, 
		NANOSECOND=128, NATURALID=129, NEW=130, NEXT=131, NO=132, NOT=133, NULL=134, 
		NULLS=135, OBJECT=136, OF=137, OFFSET=138, OFFSET_DATETIME=139, ON=140, 
		ONLY=141, OR=142, ORDER=143, OTHERS=144, OUTER=145, OVER=146, OVERFLOW=147, 
		OVERLAY=148, PAD=149, PARTITION=150, PERCENT=151, PLACING=152, POSITION=153, 
		POWER=154, PRECEDING=155, QUARTER=156, RANGE=157, RESPECT=158, RIGHT=159, 
		ROLLUP=160, ROW=161, ROWS=162, SEARCH=163, SECOND=164, SELECT=165, SET=166, 
		SIZE=167, SOME=168, SUBSTRING=169, SUM=170, THEN=171, TIES=172, TIME=173, 
		TIMESTAMP=174, TIMEZONE_HOUR=175, TIMEZONE_MINUTE=176, TO=177, TRAILING=178, 
		TREAT=179, TRIM=180, TRUE=181, TRUNC=182, TRUNCATE=183, TYPE=184, UNBOUNDED=185, 
		UNION=186, UPDATE=187, USING=188, VALUE=189, VALUES=190, VERSION=191, 
		VERSIONED=192, WEEK=193, WHEN=194, WHERE=195, WITH=196, WITHIN=197, WITHOUT=198, 
		YEAR=199, CHARACTER=200, STRINGLITERAL=201, JAVASTRINGLITERAL=202, INTEGER_LITERAL=203, 
		FLOAT_LITERAL=204, HEXLITERAL=205, BINARY_LITERAL=206, IDENTIFICATION_VARIABLE=207, 
		SPEL=208;
	public static final int
		RULE_start = 0, RULE_ql_statement = 1, RULE_selectStatement = 2, RULE_queryExpression = 3, 
		RULE_withClause = 4, RULE_cte = 5, RULE_searchClause = 6, RULE_searchSpecifications = 7, 
		RULE_searchSpecification = 8, RULE_cycleClause = 9, RULE_cteAttributes = 10, 
		RULE_orderedQuery = 11, RULE_query = 12, RULE_queryOrder = 13, RULE_fromClause = 14, 
		RULE_entityWithJoins = 15, RULE_joinSpecifier = 16, RULE_fromRoot = 17, 
		RULE_join = 18, RULE_joinTarget = 19, RULE_setReturningFunction = 20, 
		RULE_updateStatement = 21, RULE_targetEntity = 22, RULE_setClause = 23, 
		RULE_assignment = 24, RULE_deleteStatement = 25, RULE_insertStatement = 26, 
		RULE_targetFields = 27, RULE_valuesList = 28, RULE_values = 29, RULE_instantiation = 30, 
		RULE_alias = 31, RULE_groupedItem = 32, RULE_sortedItem = 33, RULE_sortExpression = 34, 
		RULE_sortDirection = 35, RULE_nullsPrecedence = 36, RULE_limitClause = 37, 
		RULE_offsetClause = 38, RULE_fetchClause = 39, RULE_subquery = 40, RULE_selectClause = 41, 
		RULE_selectionList = 42, RULE_selection = 43, RULE_selectExpression = 44, 
		RULE_mapEntrySelection = 45, RULE_jpaSelectObjectSyntax = 46, RULE_whereClause = 47, 
		RULE_joinType = 48, RULE_crossJoin = 49, RULE_joinRestriction = 50, RULE_jpaCollectionJoin = 51, 
		RULE_groupByClause = 52, RULE_orderByClause = 53, RULE_havingClause = 54, 
		RULE_setOperator = 55, RULE_literal = 56, RULE_booleanLiteral = 57, RULE_stringLiteral = 58, 
		RULE_numericLiteral = 59, RULE_dateTimeLiteral = 60, RULE_datetimeField = 61, 
		RULE_binaryLiteral = 62, RULE_expression = 63, RULE_primaryExpression = 64, 
		RULE_identificationVariable = 65, RULE_path = 66, RULE_generalPathFragment = 67, 
		RULE_indexedPathAccessFragment = 68, RULE_simplePath = 69, RULE_simplePathElement = 70, 
		RULE_caseList = 71, RULE_simpleCaseExpression = 72, RULE_searchedCaseExpression = 73, 
		RULE_caseWhenExpressionClause = 74, RULE_caseWhenPredicateClause = 75, 
		RULE_function = 76, RULE_functionArguments = 77, RULE_filterClause = 78, 
		RULE_withinGroup = 79, RULE_overClause = 80, RULE_partitionClause = 81, 
		RULE_frameClause = 82, RULE_frameStart = 83, RULE_frameExclusion = 84, 
		RULE_frameEnd = 85, RULE_castFunction = 86, RULE_castTarget = 87, RULE_castTargetType = 88, 
		RULE_extractFunction = 89, RULE_trimFunction = 90, RULE_dateTimeFunction = 91, 
		RULE_everyFunction = 92, RULE_anyFunction = 93, RULE_treatedPath = 94, 
		RULE_pathContinutation = 95, RULE_predicate = 96, RULE_expressionOrPredicate = 97, 
		RULE_relationalExpression = 98, RULE_betweenExpression = 99, RULE_dealingWithNullExpression = 100, 
		RULE_stringPatternMatching = 101, RULE_inExpression = 102, RULE_inList = 103, 
		RULE_existsExpression = 104, RULE_collectionExpression = 105, RULE_instantiationTarget = 106, 
		RULE_instantiationArguments = 107, RULE_instantiationArgument = 108, RULE_parameterOrIntegerLiteral = 109, 
		RULE_parameterOrNumberLiteral = 110, RULE_variable = 111, RULE_parameter = 112, 
		RULE_entityName = 113, RULE_identifier = 114, RULE_character = 115, RULE_functionName = 116, 
		RULE_reservedWord = 117;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "ql_statement", "selectStatement", "queryExpression", "withClause", 
			"cte", "searchClause", "searchSpecifications", "searchSpecification", 
			"cycleClause", "cteAttributes", "orderedQuery", "query", "queryOrder", 
			"fromClause", "entityWithJoins", "joinSpecifier", "fromRoot", "join", 
			"joinTarget", "setReturningFunction", "updateStatement", "targetEntity", 
			"setClause", "assignment", "deleteStatement", "insertStatement", "targetFields", 
			"valuesList", "values", "instantiation", "alias", "groupedItem", "sortedItem", 
			"sortExpression", "sortDirection", "nullsPrecedence", "limitClause", 
			"offsetClause", "fetchClause", "subquery", "selectClause", "selectionList", 
			"selection", "selectExpression", "mapEntrySelection", "jpaSelectObjectSyntax", 
			"whereClause", "joinType", "crossJoin", "joinRestriction", "jpaCollectionJoin", 
			"groupByClause", "orderByClause", "havingClause", "setOperator", "literal", 
			"booleanLiteral", "stringLiteral", "numericLiteral", "dateTimeLiteral", 
			"datetimeField", "binaryLiteral", "expression", "primaryExpression", 
			"identificationVariable", "path", "generalPathFragment", "indexedPathAccessFragment", 
			"simplePath", "simplePathElement", "caseList", "simpleCaseExpression", 
			"searchedCaseExpression", "caseWhenExpressionClause", "caseWhenPredicateClause", 
			"function", "functionArguments", "filterClause", "withinGroup", "overClause", 
			"partitionClause", "frameClause", "frameStart", "frameExclusion", "frameEnd", 
			"castFunction", "castTarget", "castTargetType", "extractFunction", "trimFunction", 
			"dateTimeFunction", "everyFunction", "anyFunction", "treatedPath", "pathContinutation", 
			"predicate", "expressionOrPredicate", "relationalExpression", "betweenExpression", 
			"dealingWithNullExpression", "stringPatternMatching", "inExpression", 
			"inList", "existsExpression", "collectionExpression", "instantiationTarget", 
			"instantiationArguments", "instantiationArgument", "parameterOrIntegerLiteral", 
			"parameterOrNumberLiteral", "variable", "parameter", "entityName", "identifier", 
			"character", "functionName", "reservedWord"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "'('", "')'", "'='", "'%'", "'{'", "'}'", "'+'", "'-'", 
			"'/'", "'||'", "'['", "']'", "'.'", "'>'", "'>='", "'<'", "'<='", "'<>'", 
			"'!='", "'^='", "':'", "'?'", null, null, null, null, null, null, "'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"WS", "ALL", "AND", "ANY", "AS", "ASC", "ASTERISK", "AVG", "BETWEEN", 
			"BOTH", "BREADTH", "BY", "CASE", "CAST", "CEILING", "COLLATE", "COUNT", 
			"CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_INSTANT", "CURRENT_TIME", 
			"CURRENT_TIMESTAMP", "CYCLE", "DATE", "DATETIME", "DAY", "DEFAULT", "DELETE", 
			"DEPTH", "DESC", "DISTINCT", "ELEMENT", "ELEMENTS", "ELSE", "EMPTY", 
			"END", "ENTRY", "EPOCH", "ERROR", "ESCAPE", "EVERY", "EXCEPT", "EXCLUDE", 
			"EXISTS", "EXP", "EXTRACT", "FALSE", "FETCH", "FILTER", "FIRST", "FK", 
			"FLOOR", "FOLLOWING", "FOR", "FORMAT", "FROM", "FULL", "FUNCTION", "GROUP", 
			"GROUPS", "HAVING", "HOUR", "ID", "IGNORE", "ILIKE", "IN", "INDEX", "INDICES", 
			"INNER", "INSERT", "INSTANT", "INTERSECT", "INTO", "IS", "JOIN", "KEY", 
			"LAST", "LATERAL", "LEADING", "LEFT", "LIKE", "LIMIT", "LIST", "LISTAGG", 
			"LN", "LOCAL", "LOCAL_DATE", "LOCAL_DATETIME", "LOCAL_TIME", "MAP", "MATERIALIZED", 
			"MAX", "MAXELEMENT", "MAXINDEX", "MEMBER", "MICROSECOND", "MILLISECOND", 
			"MIN", "MINELEMENT", "MININDEX", "MINUTE", "MONTH", "NANOSECOND", "NATURALID", 
			"NEW", "NEXT", "NO", "NOT", "NULL", "NULLS", "OBJECT", "OF", "OFFSET", 
			"OFFSET_DATETIME", "ON", "ONLY", "OR", "ORDER", "OTHERS", "OUTER", "OVER", 
			"OVERFLOW", "OVERLAY", "PAD", "PARTITION", "PERCENT", "PLACING", "POSITION", 
			"POWER", "PRECEDING", "QUARTER", "RANGE", "RESPECT", "RIGHT", "ROLLUP", 
			"ROW", "ROWS", "SEARCH", "SECOND", "SELECT", "SET", "SIZE", "SOME", "SUBSTRING", 
			"SUM", "THEN", "TIES", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", 
			"TO", "TRAILING", "TREAT", "TRIM", "TRUE", "TRUNC", "TRUNCATE", "TYPE", 
			"UNBOUNDED", "UNION", "UPDATE", "USING", "VALUE", "VALUES", "VERSION", 
			"VERSIONED", "WEEK", "WHEN", "WHERE", "WITH", "WITHIN", "WITHOUT", "YEAR", 
			"CHARACTER", "STRINGLITERAL", "JAVASTRINGLITERAL", "INTEGER_LITERAL", 
			"FLOAT_LITERAL", "HEXLITERAL", "BINARY_LITERAL", "IDENTIFICATION_VARIABLE", 
			"SPEL"
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
	public String getGrammarFileName() { return "Hql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public HqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartContext extends ParserRuleContext {
		public Ql_statementContext ql_statement() {
			return getRuleContext(Ql_statementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(HqlParser.EOF, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitStart(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
			ql_statement();
			setState(237);
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
	public static class Ql_statementContext extends ParserRuleContext {
		public SelectStatementContext selectStatement() {
			return getRuleContext(SelectStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public Ql_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ql_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterQl_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitQl_statement(this);
		}
	}

	public final Ql_statementContext ql_statement() throws RecognitionException {
		Ql_statementContext _localctx = new Ql_statementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ql_statement);
		try {
			setState(243);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case FROM:
			case SELECT:
			case WITH:
				enterOuterAlt(_localctx, 1);
				{
				setState(239);
				selectStatement();
				}
				break;
			case UPDATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(240);
				updateStatement();
				}
				break;
			case DELETE:
				enterOuterAlt(_localctx, 3);
				{
				setState(241);
				deleteStatement();
				}
				break;
			case INSERT:
				enterOuterAlt(_localctx, 4);
				{
				setState(242);
				insertStatement();
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
	public static class SelectStatementContext extends ParserRuleContext {
		public QueryExpressionContext queryExpression() {
			return getRuleContext(QueryExpressionContext.class,0);
		}
		public SelectStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelectStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelectStatement(this);
		}
	}

	public final SelectStatementContext selectStatement() throws RecognitionException {
		SelectStatementContext _localctx = new SelectStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_selectStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			queryExpression();
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
	public static class QueryExpressionContext extends ParserRuleContext {
		public List<OrderedQueryContext> orderedQuery() {
			return getRuleContexts(OrderedQueryContext.class);
		}
		public OrderedQueryContext orderedQuery(int i) {
			return getRuleContext(OrderedQueryContext.class,i);
		}
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public List<SetOperatorContext> setOperator() {
			return getRuleContexts(SetOperatorContext.class);
		}
		public SetOperatorContext setOperator(int i) {
			return getRuleContext(SetOperatorContext.class,i);
		}
		public QueryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterQueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitQueryExpression(this);
		}
	}

	public final QueryExpressionContext queryExpression() throws RecognitionException {
		QueryExpressionContext _localctx = new QueryExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_queryExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(247);
				withClause();
				}
			}

			setState(250);
			orderedQuery();
			setState(256);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EXCEPT || _la==INTERSECT || _la==UNION) {
				{
				{
				setState(251);
				setOperator();
				setState(252);
				orderedQuery();
				}
				}
				setState(258);
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
	public static class WithClauseContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(HqlParser.WITH, 0); }
		public List<CteContext> cte() {
			return getRuleContexts(CteContext.class);
		}
		public CteContext cte(int i) {
			return getRuleContext(CteContext.class,i);
		}
		public WithClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterWithClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitWithClause(this);
		}
	}

	public final WithClauseContext withClause() throws RecognitionException {
		WithClauseContext _localctx = new WithClauseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_withClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(WITH);
			setState(260);
			cte();
			setState(265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(261);
				match(T__0);
				setState(262);
				cte();
				}
				}
				setState(267);
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
	public static class CteContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public QueryExpressionContext queryExpression() {
			return getRuleContext(QueryExpressionContext.class,0);
		}
		public TerminalNode MATERIALIZED() { return getToken(HqlParser.MATERIALIZED, 0); }
		public SearchClauseContext searchClause() {
			return getRuleContext(SearchClauseContext.class,0);
		}
		public CycleClauseContext cycleClause() {
			return getRuleContext(CycleClauseContext.class,0);
		}
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public CteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cte; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCte(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCte(this);
		}
	}

	public final CteContext cte() throws RecognitionException {
		CteContext _localctx = new CteContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_cte);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
			identifier();
			setState(269);
			match(AS);
			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MATERIALIZED || _la==NOT) {
				{
				setState(271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(270);
					match(NOT);
					}
				}

				setState(273);
				match(MATERIALIZED);
				}
			}

			setState(276);
			match(T__1);
			setState(277);
			queryExpression();
			setState(278);
			match(T__2);
			setState(280);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEARCH) {
				{
				setState(279);
				searchClause();
				}
			}

			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CYCLE) {
				{
				setState(282);
				cycleClause();
				}
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
	public static class SearchClauseContext extends ParserRuleContext {
		public TerminalNode SEARCH() { return getToken(HqlParser.SEARCH, 0); }
		public TerminalNode FIRST() { return getToken(HqlParser.FIRST, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public SearchSpecificationsContext searchSpecifications() {
			return getRuleContext(SearchSpecificationsContext.class,0);
		}
		public TerminalNode SET() { return getToken(HqlParser.SET, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode BREADTH() { return getToken(HqlParser.BREADTH, 0); }
		public TerminalNode DEPTH() { return getToken(HqlParser.DEPTH, 0); }
		public SearchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearchClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearchClause(this);
		}
	}

	public final SearchClauseContext searchClause() throws RecognitionException {
		SearchClauseContext _localctx = new SearchClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_searchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285);
			match(SEARCH);
			setState(286);
			_la = _input.LA(1);
			if ( !(_la==BREADTH || _la==DEPTH) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(287);
			match(FIRST);
			setState(288);
			match(BY);
			setState(289);
			searchSpecifications();
			setState(290);
			match(SET);
			setState(291);
			identifier();
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
	public static class SearchSpecificationsContext extends ParserRuleContext {
		public List<SearchSpecificationContext> searchSpecification() {
			return getRuleContexts(SearchSpecificationContext.class);
		}
		public SearchSpecificationContext searchSpecification(int i) {
			return getRuleContext(SearchSpecificationContext.class,i);
		}
		public SearchSpecificationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchSpecifications; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearchSpecifications(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearchSpecifications(this);
		}
	}

	public final SearchSpecificationsContext searchSpecifications() throws RecognitionException {
		SearchSpecificationsContext _localctx = new SearchSpecificationsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_searchSpecifications);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			searchSpecification();
			setState(298);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(294);
				match(T__0);
				setState(295);
				searchSpecification();
				}
				}
				setState(300);
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
	public static class SearchSpecificationContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SortDirectionContext sortDirection() {
			return getRuleContext(SortDirectionContext.class,0);
		}
		public NullsPrecedenceContext nullsPrecedence() {
			return getRuleContext(NullsPrecedenceContext.class,0);
		}
		public SearchSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearchSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearchSpecification(this);
		}
	}

	public final SearchSpecificationContext searchSpecification() throws RecognitionException {
		SearchSpecificationContext _localctx = new SearchSpecificationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_searchSpecification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
			identifier();
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(302);
				sortDirection();
				}
			}

			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NULLS) {
				{
				setState(305);
				nullsPrecedence();
				}
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
	public static class CycleClauseContext extends ParserRuleContext {
		public TerminalNode CYCLE() { return getToken(HqlParser.CYCLE, 0); }
		public CteAttributesContext cteAttributes() {
			return getRuleContext(CteAttributesContext.class,0);
		}
		public TerminalNode SET() { return getToken(HqlParser.SET, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode TO() { return getToken(HqlParser.TO, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public TerminalNode DEFAULT() { return getToken(HqlParser.DEFAULT, 0); }
		public TerminalNode USING() { return getToken(HqlParser.USING, 0); }
		public CycleClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cycleClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCycleClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCycleClause(this);
		}
	}

	public final CycleClauseContext cycleClause() throws RecognitionException {
		CycleClauseContext _localctx = new CycleClauseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_cycleClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			match(CYCLE);
			setState(309);
			cteAttributes();
			setState(310);
			match(SET);
			setState(311);
			identifier();
			setState(317);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(312);
				match(TO);
				setState(313);
				literal();
				setState(314);
				match(DEFAULT);
				setState(315);
				literal();
				}
			}

			setState(321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(319);
				match(USING);
				setState(320);
				identifier();
				}
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
	public static class CteAttributesContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public CteAttributesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteAttributes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCteAttributes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCteAttributes(this);
		}
	}

	public final CteAttributesContext cteAttributes() throws RecognitionException {
		CteAttributesContext _localctx = new CteAttributesContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_cteAttributes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			identifier();
			setState(328);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(324);
				match(T__0);
				setState(325);
				identifier();
				}
				}
				setState(330);
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
	public static class OrderedQueryContext extends ParserRuleContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public QueryExpressionContext queryExpression() {
			return getRuleContext(QueryExpressionContext.class,0);
		}
		public QueryOrderContext queryOrder() {
			return getRuleContext(QueryOrderContext.class,0);
		}
		public OrderedQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOrderedQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOrderedQuery(this);
		}
	}

	public final OrderedQueryContext orderedQuery() throws RecognitionException {
		OrderedQueryContext _localctx = new OrderedQueryContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_orderedQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FROM:
			case SELECT:
				{
				setState(331);
				query();
				}
				break;
			case T__1:
				{
				setState(332);
				match(T__1);
				setState(333);
				queryExpression();
				setState(334);
				match(T__2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(339);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(338);
				queryOrder();
				}
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
	public static class QueryContext extends ParserRuleContext {
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
	 
		public QueryContext() { }
		public void copyFrom(QueryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SelectQueryContext extends QueryContext {
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public SelectQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelectQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelectQuery(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FromQueryContext extends QueryContext {
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public FromQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFromQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFromQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_query);
		int _la;
		try {
			setState(367);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				_localctx = new SelectQueryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(341);
				selectClause();
				setState(343);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM) {
					{
					setState(342);
					fromClause();
					}
				}

				setState(346);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(345);
					whereClause();
					}
				}

				setState(352);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUP) {
					{
					setState(348);
					groupByClause();
					setState(350);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==HAVING) {
						{
						setState(349);
						havingClause();
						}
					}

					}
				}

				}
				break;
			case FROM:
				_localctx = new FromQueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
				fromClause();
				setState(356);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(355);
					whereClause();
					}
				}

				setState(362);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUP) {
					{
					setState(358);
					groupByClause();
					setState(360);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==HAVING) {
						{
						setState(359);
						havingClause();
						}
					}

					}
				}

				setState(365);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SELECT) {
					{
					setState(364);
					selectClause();
					}
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

	@SuppressWarnings("CheckReturnValue")
	public static class QueryOrderContext extends ParserRuleContext {
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public OffsetClauseContext offsetClause() {
			return getRuleContext(OffsetClauseContext.class,0);
		}
		public FetchClauseContext fetchClause() {
			return getRuleContext(FetchClauseContext.class,0);
		}
		public QueryOrderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_queryOrder; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterQueryOrder(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitQueryOrder(this);
		}
	}

	public final QueryOrderContext queryOrder() throws RecognitionException {
		QueryOrderContext _localctx = new QueryOrderContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_queryOrder);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			orderByClause();
			setState(371);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(370);
				limitClause();
				}
			}

			setState(374);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OFFSET) {
				{
				setState(373);
				offsetClause();
				}
			}

			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FETCH) {
				{
				setState(376);
				fetchClause();
				}
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
	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public List<EntityWithJoinsContext> entityWithJoins() {
			return getRuleContexts(EntityWithJoinsContext.class);
		}
		public EntityWithJoinsContext entityWithJoins(int i) {
			return getRuleContext(EntityWithJoinsContext.class,i);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFromClause(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fromClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			match(FROM);
			setState(380);
			entityWithJoins();
			setState(385);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(381);
				match(T__0);
				setState(382);
				entityWithJoins();
				}
				}
				setState(387);
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
	public static class EntityWithJoinsContext extends ParserRuleContext {
		public FromRootContext fromRoot() {
			return getRuleContext(FromRootContext.class,0);
		}
		public List<JoinSpecifierContext> joinSpecifier() {
			return getRuleContexts(JoinSpecifierContext.class);
		}
		public JoinSpecifierContext joinSpecifier(int i) {
			return getRuleContext(JoinSpecifierContext.class,i);
		}
		public EntityWithJoinsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entityWithJoins; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterEntityWithJoins(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitEntityWithJoins(this);
		}
	}

	public final EntityWithJoinsContext entityWithJoins() throws RecognitionException {
		EntityWithJoinsContext _localctx = new EntityWithJoinsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_entityWithJoins);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			fromRoot();
			setState(392);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(389);
					joinSpecifier();
					}
					} 
				}
				setState(394);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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
	public static class JoinSpecifierContext extends ParserRuleContext {
		public JoinContext join() {
			return getRuleContext(JoinContext.class,0);
		}
		public CrossJoinContext crossJoin() {
			return getRuleContext(CrossJoinContext.class,0);
		}
		public JpaCollectionJoinContext jpaCollectionJoin() {
			return getRuleContext(JpaCollectionJoinContext.class,0);
		}
		public JoinSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinSpecifier(this);
		}
	}

	public final JoinSpecifierContext joinSpecifier() throws RecognitionException {
		JoinSpecifierContext _localctx = new JoinSpecifierContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_joinSpecifier);
		try {
			setState(398);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(395);
				join();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(396);
				crossJoin();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(397);
				jpaCollectionJoin();
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
	public static class FromRootContext extends ParserRuleContext {
		public EntityNameContext entityName() {
			return getRuleContext(EntityNameContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public SetReturningFunctionContext setReturningFunction() {
			return getRuleContext(SetReturningFunctionContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(HqlParser.LATERAL, 0); }
		public FromRootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromRoot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFromRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFromRoot(this);
		}
	}

	public final FromRootContext fromRoot() throws RecognitionException {
		FromRootContext _localctx = new FromRootContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_fromRoot);
		int _la;
		try {
			setState(417);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(400);
				entityName();
				setState(402);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(401);
					variable();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(404);
				setReturningFunction();
				setState(406);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(405);
					variable();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(409);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LATERAL) {
					{
					setState(408);
					match(LATERAL);
					}
				}

				setState(411);
				match(T__1);
				setState(412);
				subquery();
				setState(413);
				match(T__2);
				setState(415);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(414);
					variable();
					}
					break;
				}
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
	public static class JoinContext extends ParserRuleContext {
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode JOIN() { return getToken(HqlParser.JOIN, 0); }
		public JoinTargetContext joinTarget() {
			return getRuleContext(JoinTargetContext.class,0);
		}
		public TerminalNode FETCH() { return getToken(HqlParser.FETCH, 0); }
		public JoinRestrictionContext joinRestriction() {
			return getRuleContext(JoinRestrictionContext.class,0);
		}
		public JoinContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoin(this);
		}
	}

	public final JoinContext join() throws RecognitionException {
		JoinContext _localctx = new JoinContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_join);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			joinType();
			setState(420);
			match(JOIN);
			setState(422);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(421);
				match(FETCH);
				}
				break;
			}
			setState(424);
			joinTarget();
			setState(426);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON || _la==WITH) {
				{
				setState(425);
				joinRestriction();
				}
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
	public static class JoinTargetContext extends ParserRuleContext {
		public JoinTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinTarget; }
	 
		public JoinTargetContext() { }
		public void copyFrom(JoinTargetContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class JoinPathContext extends JoinTargetContext {
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public JoinPathContext(JoinTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinPath(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class JoinFunctionContext extends JoinTargetContext {
		public SetReturningFunctionContext setReturningFunction() {
			return getRuleContext(SetReturningFunctionContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public JoinFunctionContext(JoinTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinFunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class JoinSubqueryContext extends JoinTargetContext {
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode LATERAL() { return getToken(HqlParser.LATERAL, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public JoinSubqueryContext(JoinTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinSubquery(this);
		}
	}

	public final JoinTargetContext joinTarget() throws RecognitionException {
		JoinTargetContext _localctx = new JoinTargetContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_joinTarget);
		int _la;
		try {
			setState(445);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				_localctx = new JoinPathContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(428);
				path();
				setState(430);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
				case 1:
					{
					setState(429);
					variable();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new JoinFunctionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(432);
				setReturningFunction();
				setState(434);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
				case 1:
					{
					setState(433);
					variable();
					}
					break;
				}
				}
				break;
			case 3:
				_localctx = new JoinSubqueryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(437);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LATERAL) {
					{
					setState(436);
					match(LATERAL);
					}
				}

				setState(439);
				match(T__1);
				setState(440);
				subquery();
				setState(441);
				match(T__2);
				setState(443);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
				case 1:
					{
					setState(442);
					variable();
					}
					break;
				}
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
	public static class SetReturningFunctionContext extends ParserRuleContext {
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public FunctionArgumentsContext functionArguments() {
			return getRuleContext(FunctionArgumentsContext.class,0);
		}
		public SetReturningFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setReturningFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSetReturningFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSetReturningFunction(this);
		}
	}

	public final SetReturningFunctionContext setReturningFunction() throws RecognitionException {
		SetReturningFunctionContext _localctx = new SetReturningFunctionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_setReturningFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			functionName();
			setState(448);
			match(T__1);
			setState(450);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -275972619452L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -70918499995713L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & -1L) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & 65535L) != 0)) {
				{
				setState(449);
				functionArguments();
				}
			}

			setState(452);
			match(T__2);
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
	public static class UpdateStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(HqlParser.UPDATE, 0); }
		public TargetEntityContext targetEntity() {
			return getRuleContext(TargetEntityContext.class,0);
		}
		public SetClauseContext setClause() {
			return getRuleContext(SetClauseContext.class,0);
		}
		public TerminalNode VERSIONED() { return getToken(HqlParser.VERSIONED, 0); }
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterUpdateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitUpdateStatement(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_updateStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(454);
			match(UPDATE);
			setState(456);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				{
				setState(455);
				match(VERSIONED);
				}
				break;
			}
			setState(458);
			targetEntity();
			setState(459);
			setClause();
			setState(461);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(460);
				whereClause();
				}
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
	public static class TargetEntityContext extends ParserRuleContext {
		public EntityNameContext entityName() {
			return getRuleContext(EntityNameContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TargetEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_targetEntity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTargetEntity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTargetEntity(this);
		}
	}

	public final TargetEntityContext targetEntity() throws RecognitionException {
		TargetEntityContext _localctx = new TargetEntityContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_targetEntity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
			entityName();
			setState(465);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				{
				setState(464);
				variable();
				}
				break;
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
	public static class SetClauseContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(HqlParser.SET, 0); }
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public SetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSetClause(this);
		}
	}

	public final SetClauseContext setClause() throws RecognitionException {
		SetClauseContext _localctx = new SetClauseContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_setClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(467);
			match(SET);
			setState(468);
			assignment();
			setState(473);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(469);
				match(T__0);
				setState(470);
				assignment();
				}
				}
				setState(475);
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
	public static class AssignmentContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAssignment(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(476);
			simplePath();
			setState(477);
			match(T__3);
			setState(478);
			expressionOrPredicate();
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
	public static class DeleteStatementContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(HqlParser.DELETE, 0); }
		public TargetEntityContext targetEntity() {
			return getRuleContext(TargetEntityContext.class,0);
		}
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDeleteStatement(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_deleteStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(480);
			match(DELETE);
			setState(482);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(481);
				match(FROM);
				}
				break;
			}
			setState(484);
			targetEntity();
			setState(486);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(485);
				whereClause();
				}
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
	public static class InsertStatementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(HqlParser.INSERT, 0); }
		public TargetEntityContext targetEntity() {
			return getRuleContext(TargetEntityContext.class,0);
		}
		public TargetFieldsContext targetFields() {
			return getRuleContext(TargetFieldsContext.class,0);
		}
		public QueryExpressionContext queryExpression() {
			return getRuleContext(QueryExpressionContext.class,0);
		}
		public ValuesListContext valuesList() {
			return getRuleContext(ValuesListContext.class,0);
		}
		public TerminalNode INTO() { return getToken(HqlParser.INTO, 0); }
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInsertStatement(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_insertStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			match(INSERT);
			setState(490);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(489);
				match(INTO);
				}
				break;
			}
			setState(492);
			targetEntity();
			setState(493);
			targetFields();
			setState(496);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
			case FROM:
			case SELECT:
			case WITH:
				{
				setState(494);
				queryExpression();
				}
				break;
			case VALUES:
				{
				setState(495);
				valuesList();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class TargetFieldsContext extends ParserRuleContext {
		public List<SimplePathContext> simplePath() {
			return getRuleContexts(SimplePathContext.class);
		}
		public SimplePathContext simplePath(int i) {
			return getRuleContext(SimplePathContext.class,i);
		}
		public TargetFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_targetFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTargetFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTargetFields(this);
		}
	}

	public final TargetFieldsContext targetFields() throws RecognitionException {
		TargetFieldsContext _localctx = new TargetFieldsContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_targetFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(498);
			match(T__1);
			setState(499);
			simplePath();
			setState(504);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(500);
				match(T__0);
				setState(501);
				simplePath();
				}
				}
				setState(506);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(507);
			match(T__2);
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
	public static class ValuesListContext extends ParserRuleContext {
		public TerminalNode VALUES() { return getToken(HqlParser.VALUES, 0); }
		public List<ValuesContext> values() {
			return getRuleContexts(ValuesContext.class);
		}
		public ValuesContext values(int i) {
			return getRuleContext(ValuesContext.class,i);
		}
		public ValuesListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuesList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterValuesList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitValuesList(this);
		}
	}

	public final ValuesListContext valuesList() throws RecognitionException {
		ValuesListContext _localctx = new ValuesListContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_valuesList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(509);
			match(VALUES);
			setState(510);
			values();
			setState(515);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(511);
				match(T__0);
				setState(512);
				values();
				}
				}
				setState(517);
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
	public static class ValuesContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitValues(this);
		}
	}

	public final ValuesContext values() throws RecognitionException {
		ValuesContext _localctx = new ValuesContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_values);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(518);
			match(T__1);
			setState(519);
			expression(0);
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(520);
				match(T__0);
				setState(521);
				expression(0);
				}
				}
				setState(526);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(527);
			match(T__2);
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
	public static class InstantiationContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(HqlParser.NEW, 0); }
		public InstantiationTargetContext instantiationTarget() {
			return getRuleContext(InstantiationTargetContext.class,0);
		}
		public InstantiationArgumentsContext instantiationArguments() {
			return getRuleContext(InstantiationArgumentsContext.class,0);
		}
		public InstantiationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instantiation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInstantiation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInstantiation(this);
		}
	}

	public final InstantiationContext instantiation() throws RecognitionException {
		InstantiationContext _localctx = new InstantiationContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_instantiation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(529);
			match(NEW);
			setState(530);
			instantiationTarget();
			setState(531);
			match(T__1);
			setState(532);
			instantiationArguments();
			setState(533);
			match(T__2);
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
	public static class AliasContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAlias(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(535);
				match(AS);
				}
				break;
			}
			setState(538);
			identifier();
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
	public static class GroupedItemContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode INTEGER_LITERAL() { return getToken(HqlParser.INTEGER_LITERAL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public GroupedItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupedItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGroupedItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGroupedItem(this);
		}
	}

	public final GroupedItemContext groupedItem() throws RecognitionException {
		GroupedItemContext _localctx = new GroupedItemContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_groupedItem);
		try {
			setState(543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(540);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(541);
				match(INTEGER_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(542);
				expression(0);
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
	public static class SortedItemContext extends ParserRuleContext {
		public SortExpressionContext sortExpression() {
			return getRuleContext(SortExpressionContext.class,0);
		}
		public SortDirectionContext sortDirection() {
			return getRuleContext(SortDirectionContext.class,0);
		}
		public NullsPrecedenceContext nullsPrecedence() {
			return getRuleContext(NullsPrecedenceContext.class,0);
		}
		public SortedItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortedItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSortedItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSortedItem(this);
		}
	}

	public final SortedItemContext sortedItem() throws RecognitionException {
		SortedItemContext _localctx = new SortedItemContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_sortedItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(545);
			sortExpression();
			setState(547);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(546);
				sortDirection();
				}
			}

			setState(550);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NULLS) {
				{
				setState(549);
				nullsPrecedence();
				}
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
	public static class SortExpressionContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode INTEGER_LITERAL() { return getToken(HqlParser.INTEGER_LITERAL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SortExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSortExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSortExpression(this);
		}
	}

	public final SortExpressionContext sortExpression() throws RecognitionException {
		SortExpressionContext _localctx = new SortExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_sortExpression);
		try {
			setState(555);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(552);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(553);
				match(INTEGER_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(554);
				expression(0);
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
	public static class SortDirectionContext extends ParserRuleContext {
		public TerminalNode ASC() { return getToken(HqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(HqlParser.DESC, 0); }
		public SortDirectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortDirection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSortDirection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSortDirection(this);
		}
	}

	public final SortDirectionContext sortDirection() throws RecognitionException {
		SortDirectionContext _localctx = new SortDirectionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_sortDirection);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(557);
			_la = _input.LA(1);
			if ( !(_la==ASC || _la==DESC) ) {
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
	public static class NullsPrecedenceContext extends ParserRuleContext {
		public TerminalNode NULLS() { return getToken(HqlParser.NULLS, 0); }
		public TerminalNode FIRST() { return getToken(HqlParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(HqlParser.LAST, 0); }
		public NullsPrecedenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullsPrecedence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNullsPrecedence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNullsPrecedence(this);
		}
	}

	public final NullsPrecedenceContext nullsPrecedence() throws RecognitionException {
		NullsPrecedenceContext _localctx = new NullsPrecedenceContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_nullsPrecedence);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(559);
			match(NULLS);
			setState(560);
			_la = _input.LA(1);
			if ( !(_la==FIRST || _la==LAST) ) {
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
	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(HqlParser.LIMIT, 0); }
		public ParameterOrIntegerLiteralContext parameterOrIntegerLiteral() {
			return getRuleContext(ParameterOrIntegerLiteralContext.class,0);
		}
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitLimitClause(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(562);
			match(LIMIT);
			setState(563);
			parameterOrIntegerLiteral();
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
	public static class OffsetClauseContext extends ParserRuleContext {
		public TerminalNode OFFSET() { return getToken(HqlParser.OFFSET, 0); }
		public ParameterOrIntegerLiteralContext parameterOrIntegerLiteral() {
			return getRuleContext(ParameterOrIntegerLiteralContext.class,0);
		}
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(HqlParser.ROWS, 0); }
		public OffsetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offsetClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOffsetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOffsetClause(this);
		}
	}

	public final OffsetClauseContext offsetClause() throws RecognitionException {
		OffsetClauseContext _localctx = new OffsetClauseContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_offsetClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(565);
			match(OFFSET);
			setState(566);
			parameterOrIntegerLiteral();
			setState(568);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROW || _la==ROWS) {
				{
				setState(567);
				_la = _input.LA(1);
				if ( !(_la==ROW || _la==ROWS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
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
	public static class FetchClauseContext extends ParserRuleContext {
		public TerminalNode FETCH() { return getToken(HqlParser.FETCH, 0); }
		public TerminalNode FIRST() { return getToken(HqlParser.FIRST, 0); }
		public TerminalNode NEXT() { return getToken(HqlParser.NEXT, 0); }
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(HqlParser.ROWS, 0); }
		public ParameterOrIntegerLiteralContext parameterOrIntegerLiteral() {
			return getRuleContext(ParameterOrIntegerLiteralContext.class,0);
		}
		public ParameterOrNumberLiteralContext parameterOrNumberLiteral() {
			return getRuleContext(ParameterOrNumberLiteralContext.class,0);
		}
		public TerminalNode ONLY() { return getToken(HqlParser.ONLY, 0); }
		public TerminalNode WITH() { return getToken(HqlParser.WITH, 0); }
		public TerminalNode TIES() { return getToken(HqlParser.TIES, 0); }
		public FetchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetchClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFetchClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFetchClause(this);
		}
	}

	public final FetchClauseContext fetchClause() throws RecognitionException {
		FetchClauseContext _localctx = new FetchClauseContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_fetchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(570);
			match(FETCH);
			setState(571);
			_la = _input.LA(1);
			if ( !(_la==FIRST || _la==NEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(576);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				{
				setState(572);
				parameterOrIntegerLiteral();
				}
				break;
			case 2:
				{
				setState(573);
				parameterOrNumberLiteral();
				setState(574);
				match(T__4);
				}
				break;
			}
			setState(578);
			_la = _input.LA(1);
			if ( !(_la==ROW || _la==ROWS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(582);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ONLY:
				{
				setState(579);
				match(ONLY);
				}
				break;
			case WITH:
				{
				setState(580);
				match(WITH);
				setState(581);
				match(TIES);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class SubqueryContext extends ParserRuleContext {
		public QueryExpressionContext queryExpression() {
			return getRuleContext(QueryExpressionContext.class,0);
		}
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSubquery(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(584);
			queryExpression();
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
	public static class SelectClauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(HqlParser.SELECT, 0); }
		public SelectionListContext selectionList() {
			return getRuleContext(SelectionListContext.class,0);
		}
		public TerminalNode DISTINCT() { return getToken(HqlParser.DISTINCT, 0); }
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelectClause(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_selectClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(586);
			match(SELECT);
			setState(588);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				{
				setState(587);
				match(DISTINCT);
				}
				break;
			}
			setState(590);
			selectionList();
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
	public static class SelectionListContext extends ParserRuleContext {
		public List<SelectionContext> selection() {
			return getRuleContexts(SelectionContext.class);
		}
		public SelectionContext selection(int i) {
			return getRuleContext(SelectionContext.class,i);
		}
		public SelectionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelectionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelectionList(this);
		}
	}

	public final SelectionListContext selectionList() throws RecognitionException {
		SelectionListContext _localctx = new SelectionListContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_selectionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(592);
			selection();
			setState(597);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(593);
				match(T__0);
				setState(594);
				selection();
				}
				}
				setState(599);
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
	public static class SelectionContext extends ParserRuleContext {
		public SelectExpressionContext selectExpression() {
			return getRuleContext(SelectExpressionContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public SelectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelection(this);
		}
	}

	public final SelectionContext selection() throws RecognitionException {
		SelectionContext _localctx = new SelectionContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_selection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(600);
			selectExpression();
			setState(602);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(601);
				variable();
				}
				break;
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
	public static class SelectExpressionContext extends ParserRuleContext {
		public InstantiationContext instantiation() {
			return getRuleContext(InstantiationContext.class,0);
		}
		public MapEntrySelectionContext mapEntrySelection() {
			return getRuleContext(MapEntrySelectionContext.class,0);
		}
		public JpaSelectObjectSyntaxContext jpaSelectObjectSyntax() {
			return getRuleContext(JpaSelectObjectSyntaxContext.class,0);
		}
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public SelectExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSelectExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSelectExpression(this);
		}
	}

	public final SelectExpressionContext selectExpression() throws RecognitionException {
		SelectExpressionContext _localctx = new SelectExpressionContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_selectExpression);
		try {
			setState(608);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(604);
				instantiation();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(605);
				mapEntrySelection();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(606);
				jpaSelectObjectSyntax();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(607);
				expressionOrPredicate();
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
	public static class MapEntrySelectionContext extends ParserRuleContext {
		public TerminalNode ENTRY() { return getToken(HqlParser.ENTRY, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public MapEntrySelectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapEntrySelection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterMapEntrySelection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitMapEntrySelection(this);
		}
	}

	public final MapEntrySelectionContext mapEntrySelection() throws RecognitionException {
		MapEntrySelectionContext _localctx = new MapEntrySelectionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_mapEntrySelection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(610);
			match(ENTRY);
			setState(611);
			match(T__1);
			setState(612);
			path();
			setState(613);
			match(T__2);
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
	public static class JpaSelectObjectSyntaxContext extends ParserRuleContext {
		public TerminalNode OBJECT() { return getToken(HqlParser.OBJECT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public JpaSelectObjectSyntaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jpaSelectObjectSyntax; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJpaSelectObjectSyntax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJpaSelectObjectSyntax(this);
		}
	}

	public final JpaSelectObjectSyntaxContext jpaSelectObjectSyntax() throws RecognitionException {
		JpaSelectObjectSyntaxContext _localctx = new JpaSelectObjectSyntaxContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_jpaSelectObjectSyntax);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(615);
			match(OBJECT);
			setState(616);
			match(T__1);
			setState(617);
			identifier();
			setState(618);
			match(T__2);
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
	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(HqlParser.WHERE, 0); }
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitWhereClause(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_whereClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(620);
			match(WHERE);
			setState(621);
			predicate(0);
			setState(626);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(622);
				match(T__0);
				setState(623);
				predicate(0);
				}
				}
				setState(628);
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
	public static class JoinTypeContext extends ParserRuleContext {
		public TerminalNode INNER() { return getToken(HqlParser.INNER, 0); }
		public TerminalNode OUTER() { return getToken(HqlParser.OUTER, 0); }
		public TerminalNode LEFT() { return getToken(HqlParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(HqlParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(HqlParser.FULL, 0); }
		public TerminalNode CROSS() { return getToken(HqlParser.CROSS, 0); }
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinType(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_joinType);
		int _la;
		try {
			setState(639);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(630);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INNER) {
					{
					setState(629);
					match(INNER);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(633);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FULL || _la==LEFT || _la==RIGHT) {
					{
					setState(632);
					_la = _input.LA(1);
					if ( !(_la==FULL || _la==LEFT || _la==RIGHT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(636);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(635);
					match(OUTER);
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(638);
				match(CROSS);
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
	public static class CrossJoinContext extends ParserRuleContext {
		public TerminalNode CROSS() { return getToken(HqlParser.CROSS, 0); }
		public TerminalNode JOIN() { return getToken(HqlParser.JOIN, 0); }
		public EntityNameContext entityName() {
			return getRuleContext(EntityNameContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public CrossJoinContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crossJoin; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCrossJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCrossJoin(this);
		}
	}

	public final CrossJoinContext crossJoin() throws RecognitionException {
		CrossJoinContext _localctx = new CrossJoinContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_crossJoin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(641);
			match(CROSS);
			setState(642);
			match(JOIN);
			setState(643);
			entityName();
			setState(645);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				{
				setState(644);
				variable();
				}
				break;
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
	public static class JoinRestrictionContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode ON() { return getToken(HqlParser.ON, 0); }
		public TerminalNode WITH() { return getToken(HqlParser.WITH, 0); }
		public JoinRestrictionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinRestriction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJoinRestriction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJoinRestriction(this);
		}
	}

	public final JoinRestrictionContext joinRestriction() throws RecognitionException {
		JoinRestrictionContext _localctx = new JoinRestrictionContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_joinRestriction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			_la = _input.LA(1);
			if ( !(_la==ON || _la==WITH) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(648);
			predicate(0);
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
	public static class JpaCollectionJoinContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(HqlParser.IN, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public JpaCollectionJoinContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jpaCollectionJoin; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterJpaCollectionJoin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitJpaCollectionJoin(this);
		}
	}

	public final JpaCollectionJoinContext jpaCollectionJoin() throws RecognitionException {
		JpaCollectionJoinContext _localctx = new JpaCollectionJoinContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_jpaCollectionJoin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(650);
			match(T__0);
			setState(651);
			match(IN);
			setState(652);
			match(T__1);
			setState(653);
			path();
			setState(654);
			match(T__2);
			setState(656);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				{
				setState(655);
				variable();
				}
				break;
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
	public static class GroupByClauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(HqlParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public List<GroupedItemContext> groupedItem() {
			return getRuleContexts(GroupedItemContext.class);
		}
		public GroupedItemContext groupedItem(int i) {
			return getRuleContext(GroupedItemContext.class,i);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGroupByClause(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(658);
			match(GROUP);
			setState(659);
			match(BY);
			setState(660);
			groupedItem();
			setState(665);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(661);
				match(T__0);
				setState(662);
				groupedItem();
				}
				}
				setState(667);
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
	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(HqlParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public List<SortedItemContext> sortedItem() {
			return getRuleContexts(SortedItemContext.class);
		}
		public SortedItemContext sortedItem(int i) {
			return getRuleContext(SortedItemContext.class,i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOrderByClause(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(668);
			match(ORDER);
			setState(669);
			match(BY);
			setState(670);
			sortedItem();
			setState(675);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(671);
				match(T__0);
				setState(672);
				sortedItem();
				}
				}
				setState(677);
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
	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(HqlParser.HAVING, 0); }
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitHavingClause(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_havingClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(678);
			match(HAVING);
			setState(679);
			predicate(0);
			setState(684);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(680);
				match(T__0);
				setState(681);
				predicate(0);
				}
				}
				setState(686);
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
	public static class SetOperatorContext extends ParserRuleContext {
		public TerminalNode UNION() { return getToken(HqlParser.UNION, 0); }
		public TerminalNode ALL() { return getToken(HqlParser.ALL, 0); }
		public TerminalNode INTERSECT() { return getToken(HqlParser.INTERSECT, 0); }
		public TerminalNode EXCEPT() { return getToken(HqlParser.EXCEPT, 0); }
		public SetOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSetOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSetOperator(this);
		}
	}

	public final SetOperatorContext setOperator() throws RecognitionException {
		SetOperatorContext _localctx = new SetOperatorContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_setOperator);
		int _la;
		try {
			setState(699);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNION:
				enterOuterAlt(_localctx, 1);
				{
				setState(687);
				match(UNION);
				setState(689);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(688);
					match(ALL);
					}
				}

				}
				break;
			case INTERSECT:
				enterOuterAlt(_localctx, 2);
				{
				setState(691);
				match(INTERSECT);
				setState(693);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(692);
					match(ALL);
					}
				}

				}
				break;
			case EXCEPT:
				enterOuterAlt(_localctx, 3);
				{
				setState(695);
				match(EXCEPT);
				setState(697);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(696);
					match(ALL);
					}
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

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public DateTimeLiteralContext dateTimeLiteral() {
			return getRuleContext(DateTimeLiteralContext.class,0);
		}
		public BinaryLiteralContext binaryLiteral() {
			return getRuleContext(BinaryLiteralContext.class,0);
		}
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_literal);
		try {
			setState(707);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(701);
				match(NULL);
				}
				break;
			case FALSE:
			case TRUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(702);
				booleanLiteral();
				}
				break;
			case CHARACTER:
			case STRINGLITERAL:
			case JAVASTRINGLITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(703);
				stringLiteral();
				}
				break;
			case INTEGER_LITERAL:
			case FLOAT_LITERAL:
			case HEXLITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(704);
				numericLiteral();
				}
				break;
			case CURRENT:
			case CURRENT_DATE:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case INSTANT:
			case LOCAL:
			case LOCAL_DATE:
			case LOCAL_DATETIME:
			case LOCAL_TIME:
			case OFFSET:
			case OFFSET_DATETIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(705);
				dateTimeLiteral();
				}
				break;
			case T__5:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(706);
				binaryLiteral();
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
	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(HqlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(HqlParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBooleanLiteral(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(709);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
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
	public static class StringLiteralContext extends ParserRuleContext {
		public TerminalNode STRINGLITERAL() { return getToken(HqlParser.STRINGLITERAL, 0); }
		public TerminalNode JAVASTRINGLITERAL() { return getToken(HqlParser.JAVASTRINGLITERAL, 0); }
		public TerminalNode CHARACTER() { return getToken(HqlParser.CHARACTER, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitStringLiteral(this);
		}
	}

	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_stringLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(711);
			_la = _input.LA(1);
			if ( !(((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & 7L) != 0)) ) {
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
	public static class NumericLiteralContext extends ParserRuleContext {
		public TerminalNode INTEGER_LITERAL() { return getToken(HqlParser.INTEGER_LITERAL, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(HqlParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEXLITERAL() { return getToken(HqlParser.HEXLITERAL, 0); }
		public NumericLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNumericLiteral(this);
		}
	}

	public final NumericLiteralContext numericLiteral() throws RecognitionException {
		NumericLiteralContext _localctx = new NumericLiteralContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_numericLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(713);
			_la = _input.LA(1);
			if ( !(((((_la - 203)) & ~0x3f) == 0 && ((1L << (_la - 203)) & 7L) != 0)) ) {
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
	public static class DateTimeLiteralContext extends ParserRuleContext {
		public TerminalNode LOCAL_DATE() { return getToken(HqlParser.LOCAL_DATE, 0); }
		public TerminalNode LOCAL_TIME() { return getToken(HqlParser.LOCAL_TIME, 0); }
		public TerminalNode LOCAL_DATETIME() { return getToken(HqlParser.LOCAL_DATETIME, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(HqlParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(HqlParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(HqlParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode OFFSET_DATETIME() { return getToken(HqlParser.OFFSET_DATETIME, 0); }
		public TerminalNode DATE() { return getToken(HqlParser.DATE, 0); }
		public TerminalNode LOCAL() { return getToken(HqlParser.LOCAL, 0); }
		public TerminalNode CURRENT() { return getToken(HqlParser.CURRENT, 0); }
		public TerminalNode TIME() { return getToken(HqlParser.TIME, 0); }
		public TerminalNode DATETIME() { return getToken(HqlParser.DATETIME, 0); }
		public TerminalNode OFFSET() { return getToken(HqlParser.OFFSET, 0); }
		public TerminalNode INSTANT() { return getToken(HqlParser.INSTANT, 0); }
		public DateTimeLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDateTimeLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDateTimeLiteral(this);
		}
	}

	public final DateTimeLiteralContext dateTimeLiteral() throws RecognitionException {
		DateTimeLiteralContext _localctx = new DateTimeLiteralContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_dateTimeLiteral);
		int _la;
		try {
			setState(729);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(715);
				match(LOCAL_DATE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(716);
				match(LOCAL_TIME);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(717);
				match(LOCAL_DATETIME);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(718);
				match(CURRENT_DATE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(719);
				match(CURRENT_TIME);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(720);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(721);
				match(OFFSET_DATETIME);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(722);
				_la = _input.LA(1);
				if ( !(_la==CURRENT || _la==LOCAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(723);
				match(DATE);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(724);
				_la = _input.LA(1);
				if ( !(_la==CURRENT || _la==LOCAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(725);
				match(TIME);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(726);
				_la = _input.LA(1);
				if ( !(_la==CURRENT || _la==LOCAL || _la==OFFSET) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(727);
				match(DATETIME);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(728);
				match(INSTANT);
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
	public static class DatetimeFieldContext extends ParserRuleContext {
		public TerminalNode YEAR() { return getToken(HqlParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(HqlParser.MONTH, 0); }
		public TerminalNode DAY() { return getToken(HqlParser.DAY, 0); }
		public TerminalNode WEEK() { return getToken(HqlParser.WEEK, 0); }
		public TerminalNode QUARTER() { return getToken(HqlParser.QUARTER, 0); }
		public TerminalNode HOUR() { return getToken(HqlParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(HqlParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(HqlParser.SECOND, 0); }
		public TerminalNode NANOSECOND() { return getToken(HqlParser.NANOSECOND, 0); }
		public TerminalNode EPOCH() { return getToken(HqlParser.EPOCH, 0); }
		public DatetimeFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datetimeField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDatetimeField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDatetimeField(this);
		}
	}

	public final DatetimeFieldContext datetimeField() throws RecognitionException {
		DatetimeFieldContext _localctx = new DatetimeFieldContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_datetimeField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(731);
			_la = _input.LA(1);
			if ( !(_la==DAY || _la==EPOCH || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & 3848290697217L) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & 8933531975937L) != 0)) ) {
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
	public static class BinaryLiteralContext extends ParserRuleContext {
		public TerminalNode BINARY_LITERAL() { return getToken(HqlParser.BINARY_LITERAL, 0); }
		public List<TerminalNode> HEXLITERAL() { return getTokens(HqlParser.HEXLITERAL); }
		public TerminalNode HEXLITERAL(int i) {
			return getToken(HqlParser.HEXLITERAL, i);
		}
		public BinaryLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binaryLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBinaryLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBinaryLiteral(this);
		}
	}

	public final BinaryLiteralContext binaryLiteral() throws RecognitionException {
		BinaryLiteralContext _localctx = new BinaryLiteralContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_binaryLiteral);
		int _la;
		try {
			setState(744);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(733);
				match(BINARY_LITERAL);
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 2);
				{
				setState(734);
				match(T__5);
				setState(735);
				match(HEXLITERAL);
				setState(740);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__0) {
					{
					{
					setState(736);
					match(T__0);
					setState(737);
					match(HEXLITERAL);
					}
					}
					setState(742);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(743);
				match(T__6);
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
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AdditionExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public AdditionExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAdditionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAdditionExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FromDurationExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public DatetimeFieldContext datetimeField() {
			return getRuleContext(DatetimeFieldContext.class,0);
		}
		public FromDurationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFromDurationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFromDurationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PlainPrimaryExpressionContext extends ExpressionContext {
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public PlainPrimaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPlainPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPlainPrimaryExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TupleExpressionContext extends ExpressionContext {
		public List<ExpressionOrPredicateContext> expressionOrPredicate() {
			return getRuleContexts(ExpressionOrPredicateContext.class);
		}
		public ExpressionOrPredicateContext expressionOrPredicate(int i) {
			return getRuleContext(ExpressionOrPredicateContext.class,i);
		}
		public TupleExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTupleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTupleExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupedExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public GroupedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGroupedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGroupedExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SignedNumericLiteralContext extends ExpressionContext {
		public Token op;
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public SignedNumericLiteralContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSignedNumericLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSignedNumericLiteral(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ToDurationExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public DatetimeFieldContext datetimeField() {
			return getRuleContext(DatetimeFieldContext.class,0);
		}
		public ToDurationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterToDurationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitToDurationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SubqueryExpressionContext extends ExpressionContext {
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public SubqueryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSubqueryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSubqueryExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DayOfMonthExpressionContext extends ExpressionContext {
		public TerminalNode DAY() { return getToken(HqlParser.DAY, 0); }
		public TerminalNode OF() { return getToken(HqlParser.OF, 0); }
		public TerminalNode MONTH() { return getToken(HqlParser.MONTH, 0); }
		public DayOfMonthExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDayOfMonthExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDayOfMonthExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DayOfWeekExpressionContext extends ExpressionContext {
		public TerminalNode DAY() { return getToken(HqlParser.DAY, 0); }
		public TerminalNode OF() { return getToken(HqlParser.OF, 0); }
		public TerminalNode WEEK() { return getToken(HqlParser.WEEK, 0); }
		public DayOfWeekExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDayOfWeekExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDayOfWeekExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WeekOfYearExpressionContext extends ExpressionContext {
		public TerminalNode WEEK() { return getToken(HqlParser.WEEK, 0); }
		public TerminalNode OF() { return getToken(HqlParser.OF, 0); }
		public TerminalNode YEAR() { return getToken(HqlParser.YEAR, 0); }
		public WeekOfYearExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterWeekOfYearExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitWeekOfYearExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HqlConcatenationExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public HqlConcatenationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterHqlConcatenationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitHqlConcatenationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicationExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ASTERISK() { return getToken(HqlParser.ASTERISK, 0); }
		public MultiplicationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterMultiplicationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitMultiplicationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SignedExpressionContext extends ExpressionContext {
		public Token op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SignedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSignedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSignedExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 126;
		enterRecursionRule(_localctx, 126, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(779);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				{
				_localctx = new GroupedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(747);
				match(T__1);
				setState(748);
				expression(0);
				setState(749);
				match(T__2);
				}
				break;
			case 2:
				{
				_localctx = new TupleExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(751);
				match(T__1);
				setState(752);
				expressionOrPredicate();
				setState(755); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(753);
					match(T__0);
					setState(754);
					expressionOrPredicate();
					}
					}
					setState(757); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__0 );
				setState(759);
				match(T__2);
				}
				break;
			case 3:
				{
				_localctx = new SubqueryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(761);
				match(T__1);
				setState(762);
				subquery();
				setState(763);
				match(T__2);
				}
				break;
			case 4:
				{
				_localctx = new PlainPrimaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(765);
				primaryExpression();
				}
				break;
			case 5:
				{
				_localctx = new SignedNumericLiteralContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(766);
				((SignedNumericLiteralContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__7 || _la==T__8) ) {
					((SignedNumericLiteralContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(767);
				numericLiteral();
				}
				break;
			case 6:
				{
				_localctx = new SignedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(768);
				((SignedExpressionContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__7 || _la==T__8) ) {
					((SignedExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(769);
				expression(9);
				}
				break;
			case 7:
				{
				_localctx = new DayOfWeekExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(770);
				match(DAY);
				setState(771);
				match(OF);
				setState(772);
				match(WEEK);
				}
				break;
			case 8:
				{
				_localctx = new DayOfMonthExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(773);
				match(DAY);
				setState(774);
				match(OF);
				setState(775);
				match(MONTH);
				}
				break;
			case 9:
				{
				_localctx = new WeekOfYearExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(776);
				match(WEEK);
				setState(777);
				match(OF);
				setState(778);
				match(YEAR);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(797);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(795);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
					case 1:
						{
						_localctx = new MultiplicationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(781);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(782);
						((MultiplicationExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__9 || _la==ASTERISK) ) {
							((MultiplicationExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(783);
						expression(7);
						}
						break;
					case 2:
						{
						_localctx = new AdditionExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(784);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(785);
						((AdditionExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__7 || _la==T__8) ) {
							((AdditionExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(786);
						expression(6);
						}
						break;
					case 3:
						{
						_localctx = new HqlConcatenationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(787);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(788);
						match(T__10);
						setState(789);
						expression(5);
						}
						break;
					case 4:
						{
						_localctx = new ToDurationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(790);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(791);
						datetimeField();
						}
						break;
					case 5:
						{
						_localctx = new FromDurationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(792);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(793);
						match(BY);
						setState(794);
						datetimeField();
						}
						break;
					}
					} 
				}
				setState(799);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
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
	public static class PrimaryExpressionContext extends ParserRuleContext {
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
	 
		public PrimaryExpressionContext() { }
		public void copyFrom(PrimaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionExpressionContext extends PrimaryExpressionContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public FunctionExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFunctionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFunctionExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralExpressionContext extends PrimaryExpressionContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public LiteralExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterLiteralExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitLiteralExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParameterExpressionContext extends PrimaryExpressionContext {
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public ParameterExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterParameterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitParameterExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GeneralPathExpressionContext extends PrimaryExpressionContext {
		public GeneralPathFragmentContext generalPathFragment() {
			return getRuleContext(GeneralPathFragmentContext.class,0);
		}
		public GeneralPathExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGeneralPathExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGeneralPathExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CaseExpressionContext extends PrimaryExpressionContext {
		public CaseListContext caseList() {
			return getRuleContext(CaseListContext.class,0);
		}
		public CaseExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCaseExpression(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_primaryExpression);
		try {
			setState(805);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				_localctx = new CaseExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(800);
				caseList();
				}
				break;
			case 2:
				_localctx = new LiteralExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(801);
				literal();
				}
				break;
			case 3:
				_localctx = new ParameterExpressionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(802);
				parameter();
				}
				break;
			case 4:
				_localctx = new FunctionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(803);
				function();
				}
				break;
			case 5:
				_localctx = new GeneralPathExpressionContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(804);
				generalPathFragment();
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
	public static class IdentificationVariableContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public IdentificationVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identificationVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterIdentificationVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitIdentificationVariable(this);
		}
	}

	public final IdentificationVariableContext identificationVariable() throws RecognitionException {
		IdentificationVariableContext _localctx = new IdentificationVariableContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_identificationVariable);
		try {
			setState(809);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(807);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(808);
				simplePath();
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
	public static class PathContext extends ParserRuleContext {
		public TreatedPathContext treatedPath() {
			return getRuleContext(TreatedPathContext.class,0);
		}
		public PathContinutationContext pathContinutation() {
			return getRuleContext(PathContinutationContext.class,0);
		}
		public GeneralPathFragmentContext generalPathFragment() {
			return getRuleContext(GeneralPathFragmentContext.class,0);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPath(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_path);
		try {
			setState(816);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(811);
				treatedPath();
				setState(813);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(812);
					pathContinutation();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(815);
				generalPathFragment();
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
	public static class GeneralPathFragmentContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public IndexedPathAccessFragmentContext indexedPathAccessFragment() {
			return getRuleContext(IndexedPathAccessFragmentContext.class,0);
		}
		public GeneralPathFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generalPathFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGeneralPathFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGeneralPathFragment(this);
		}
	}

	public final GeneralPathFragmentContext generalPathFragment() throws RecognitionException {
		GeneralPathFragmentContext _localctx = new GeneralPathFragmentContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_generalPathFragment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(818);
			simplePath();
			setState(820);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
			case 1:
				{
				setState(819);
				indexedPathAccessFragment();
				}
				break;
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
	public static class IndexedPathAccessFragmentContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public GeneralPathFragmentContext generalPathFragment() {
			return getRuleContext(GeneralPathFragmentContext.class,0);
		}
		public IndexedPathAccessFragmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexedPathAccessFragment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterIndexedPathAccessFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitIndexedPathAccessFragment(this);
		}
	}

	public final IndexedPathAccessFragmentContext indexedPathAccessFragment() throws RecognitionException {
		IndexedPathAccessFragmentContext _localctx = new IndexedPathAccessFragmentContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_indexedPathAccessFragment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(822);
			match(T__11);
			setState(823);
			expression(0);
			setState(824);
			match(T__12);
			setState(827);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				{
				setState(825);
				match(T__13);
				setState(826);
				generalPathFragment();
				}
				break;
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
	public static class SimplePathContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<SimplePathElementContext> simplePathElement() {
			return getRuleContexts(SimplePathElementContext.class);
		}
		public SimplePathElementContext simplePathElement(int i) {
			return getRuleContext(SimplePathElementContext.class,i);
		}
		public SimplePathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplePath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSimplePath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSimplePath(this);
		}
	}

	public final SimplePathContext simplePath() throws RecognitionException {
		SimplePathContext _localctx = new SimplePathContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_simplePath);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(829);
			identifier();
			setState(833);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(830);
					simplePathElement();
					}
					} 
				}
				setState(835);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
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
	public static class SimplePathElementContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public SimplePathElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplePathElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSimplePathElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSimplePathElement(this);
		}
	}

	public final SimplePathElementContext simplePathElement() throws RecognitionException {
		SimplePathElementContext _localctx = new SimplePathElementContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_simplePathElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(836);
			match(T__13);
			setState(837);
			identifier();
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
	public static class CaseListContext extends ParserRuleContext {
		public SimpleCaseExpressionContext simpleCaseExpression() {
			return getRuleContext(SimpleCaseExpressionContext.class,0);
		}
		public SearchedCaseExpressionContext searchedCaseExpression() {
			return getRuleContext(SearchedCaseExpressionContext.class,0);
		}
		public CaseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCaseList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCaseList(this);
		}
	}

	public final CaseListContext caseList() throws RecognitionException {
		CaseListContext _localctx = new CaseListContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_caseList);
		try {
			setState(841);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(839);
				simpleCaseExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(840);
				searchedCaseExpression();
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
	public static class SimpleCaseExpressionContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(HqlParser.CASE, 0); }
		public List<ExpressionOrPredicateContext> expressionOrPredicate() {
			return getRuleContexts(ExpressionOrPredicateContext.class);
		}
		public ExpressionOrPredicateContext expressionOrPredicate(int i) {
			return getRuleContext(ExpressionOrPredicateContext.class,i);
		}
		public TerminalNode END() { return getToken(HqlParser.END, 0); }
		public List<CaseWhenExpressionClauseContext> caseWhenExpressionClause() {
			return getRuleContexts(CaseWhenExpressionClauseContext.class);
		}
		public CaseWhenExpressionClauseContext caseWhenExpressionClause(int i) {
			return getRuleContext(CaseWhenExpressionClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(HqlParser.ELSE, 0); }
		public SimpleCaseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleCaseExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSimpleCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSimpleCaseExpression(this);
		}
	}

	public final SimpleCaseExpressionContext simpleCaseExpression() throws RecognitionException {
		SimpleCaseExpressionContext _localctx = new SimpleCaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_simpleCaseExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(843);
			match(CASE);
			setState(844);
			expressionOrPredicate();
			setState(846); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(845);
				caseWhenExpressionClause();
				}
				}
				setState(848); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(852);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(850);
				match(ELSE);
				setState(851);
				expressionOrPredicate();
				}
			}

			setState(854);
			match(END);
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
	public static class SearchedCaseExpressionContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(HqlParser.CASE, 0); }
		public TerminalNode END() { return getToken(HqlParser.END, 0); }
		public List<CaseWhenPredicateClauseContext> caseWhenPredicateClause() {
			return getRuleContexts(CaseWhenPredicateClauseContext.class);
		}
		public CaseWhenPredicateClauseContext caseWhenPredicateClause(int i) {
			return getRuleContext(CaseWhenPredicateClauseContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(HqlParser.ELSE, 0); }
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public SearchedCaseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchedCaseExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterSearchedCaseExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitSearchedCaseExpression(this);
		}
	}

	public final SearchedCaseExpressionContext searchedCaseExpression() throws RecognitionException {
		SearchedCaseExpressionContext _localctx = new SearchedCaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_searchedCaseExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(856);
			match(CASE);
			setState(858); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(857);
				caseWhenPredicateClause();
				}
				}
				setState(860); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(864);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(862);
				match(ELSE);
				setState(863);
				expressionOrPredicate();
				}
			}

			setState(866);
			match(END);
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
	public static class CaseWhenExpressionClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(HqlParser.WHEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode THEN() { return getToken(HqlParser.THEN, 0); }
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public CaseWhenExpressionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhenExpressionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCaseWhenExpressionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCaseWhenExpressionClause(this);
		}
	}

	public final CaseWhenExpressionClauseContext caseWhenExpressionClause() throws RecognitionException {
		CaseWhenExpressionClauseContext _localctx = new CaseWhenExpressionClauseContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_caseWhenExpressionClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(868);
			match(WHEN);
			setState(869);
			expression(0);
			setState(870);
			match(THEN);
			setState(871);
			expressionOrPredicate();
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
	public static class CaseWhenPredicateClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(HqlParser.WHEN, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode THEN() { return getToken(HqlParser.THEN, 0); }
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public CaseWhenPredicateClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhenPredicateClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCaseWhenPredicateClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCaseWhenPredicateClause(this);
		}
	}

	public final CaseWhenPredicateClauseContext caseWhenPredicateClause() throws RecognitionException {
		CaseWhenPredicateClauseContext _localctx = new CaseWhenPredicateClauseContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_caseWhenPredicateClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(873);
			match(WHEN);
			setState(874);
			predicate(0);
			setState(875);
			match(THEN);
			setState(876);
			expressionOrPredicate();
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
	public static class FunctionContext extends ParserRuleContext {
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
	 
		public FunctionContext() { }
		public void copyFrom(FunctionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastFunctionInvocationContext extends FunctionContext {
		public CastFunctionContext castFunction() {
			return getRuleContext(CastFunctionContext.class,0);
		}
		public CastFunctionInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCastFunctionInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCastFunctionInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TreatedPathInvocationContext extends FunctionContext {
		public TreatedPathContext treatedPath() {
			return getRuleContext(TreatedPathContext.class,0);
		}
		public TreatedPathInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTreatedPathInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTreatedPathInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionWithSubqueryContext extends FunctionContext {
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public FunctionWithSubqueryContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFunctionWithSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFunctionWithSubquery(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AnyFunctionInvocationContext extends FunctionContext {
		public AnyFunctionContext anyFunction() {
			return getRuleContext(AnyFunctionContext.class,0);
		}
		public AnyFunctionInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAnyFunctionInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAnyFunctionInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExtractFunctionInvocationContext extends FunctionContext {
		public ExtractFunctionContext extractFunction() {
			return getRuleContext(ExtractFunctionContext.class,0);
		}
		public ExtractFunctionInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExtractFunctionInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExtractFunctionInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericFunctionContext extends FunctionContext {
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public FunctionArgumentsContext functionArguments() {
			return getRuleContext(FunctionArgumentsContext.class,0);
		}
		public TerminalNode ASTERISK() { return getToken(HqlParser.ASTERISK, 0); }
		public PathContinutationContext pathContinutation() {
			return getRuleContext(PathContinutationContext.class,0);
		}
		public FilterClauseContext filterClause() {
			return getRuleContext(FilterClauseContext.class,0);
		}
		public WithinGroupContext withinGroup() {
			return getRuleContext(WithinGroupContext.class,0);
		}
		public OverClauseContext overClause() {
			return getRuleContext(OverClauseContext.class,0);
		}
		public GenericFunctionContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGenericFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGenericFunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EveryFunctionInvocationContext extends FunctionContext {
		public EveryFunctionContext everyFunction() {
			return getRuleContext(EveryFunctionContext.class,0);
		}
		public EveryFunctionInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterEveryFunctionInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitEveryFunctionInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TrimFunctionInvocationContext extends FunctionContext {
		public TrimFunctionContext trimFunction() {
			return getRuleContext(TrimFunctionContext.class,0);
		}
		public TrimFunctionInvocationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTrimFunctionInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTrimFunctionInvocation(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_function);
		try {
			setState(908);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
			case 1:
				_localctx = new GenericFunctionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(878);
				functionName();
				setState(879);
				match(T__1);
				setState(882);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__1:
				case T__5:
				case T__7:
				case T__8:
				case T__21:
				case T__22:
				case ALL:
				case AND:
				case ANY:
				case AS:
				case ASC:
				case AVG:
				case BETWEEN:
				case BOTH:
				case BREADTH:
				case BY:
				case CASE:
				case CAST:
				case COLLATE:
				case COUNT:
				case CROSS:
				case CUBE:
				case CURRENT:
				case CURRENT_DATE:
				case CURRENT_INSTANT:
				case CURRENT_TIME:
				case CURRENT_TIMESTAMP:
				case CYCLE:
				case DATE:
				case DATETIME:
				case DAY:
				case DEFAULT:
				case DELETE:
				case DEPTH:
				case DESC:
				case DISTINCT:
				case ELEMENT:
				case ELEMENTS:
				case ELSE:
				case EMPTY:
				case END:
				case ENTRY:
				case EPOCH:
				case ERROR:
				case ESCAPE:
				case EVERY:
				case EXCEPT:
				case EXCLUDE:
				case EXISTS:
				case EXTRACT:
				case FALSE:
				case FETCH:
				case FILTER:
				case FIRST:
				case FLOOR:
				case FOLLOWING:
				case FOR:
				case FORMAT:
				case FROM:
				case FULL:
				case FUNCTION:
				case GROUP:
				case GROUPS:
				case HAVING:
				case HOUR:
				case ID:
				case IGNORE:
				case ILIKE:
				case IN:
				case INDEX:
				case INDICES:
				case INNER:
				case INSERT:
				case INSTANT:
				case INTERSECT:
				case INTO:
				case IS:
				case JOIN:
				case KEY:
				case LAST:
				case LEADING:
				case LEFT:
				case LIKE:
				case LIMIT:
				case LIST:
				case LISTAGG:
				case LOCAL:
				case LOCAL_DATE:
				case LOCAL_DATETIME:
				case LOCAL_TIME:
				case MAP:
				case MATERIALIZED:
				case MAX:
				case MAXELEMENT:
				case MAXINDEX:
				case MEMBER:
				case MICROSECOND:
				case MILLISECOND:
				case MIN:
				case MINELEMENT:
				case MININDEX:
				case MINUTE:
				case MONTH:
				case NANOSECOND:
				case NATURALID:
				case NEW:
				case NEXT:
				case NO:
				case NOT:
				case NULL:
				case NULLS:
				case OBJECT:
				case OF:
				case OFFSET:
				case OFFSET_DATETIME:
				case ON:
				case ONLY:
				case OR:
				case ORDER:
				case OTHERS:
				case OUTER:
				case OVER:
				case OVERFLOW:
				case OVERLAY:
				case PAD:
				case PARTITION:
				case PERCENT:
				case PLACING:
				case POSITION:
				case POWER:
				case PRECEDING:
				case QUARTER:
				case RANGE:
				case RESPECT:
				case RIGHT:
				case ROLLUP:
				case ROW:
				case ROWS:
				case SEARCH:
				case SECOND:
				case SELECT:
				case SET:
				case SIZE:
				case SOME:
				case SUBSTRING:
				case SUM:
				case THEN:
				case TIES:
				case TIME:
				case TIMESTAMP:
				case TIMEZONE_HOUR:
				case TIMEZONE_MINUTE:
				case TO:
				case TRAILING:
				case TREAT:
				case TRIM:
				case TRUE:
				case TRUNC:
				case TRUNCATE:
				case TYPE:
				case UNBOUNDED:
				case UNION:
				case UPDATE:
				case USING:
				case VALUE:
				case VALUES:
				case VERSION:
				case VERSIONED:
				case WEEK:
				case WHEN:
				case WHERE:
				case WITH:
				case WITHIN:
				case WITHOUT:
				case YEAR:
				case CHARACTER:
				case STRINGLITERAL:
				case JAVASTRINGLITERAL:
				case INTEGER_LITERAL:
				case FLOAT_LITERAL:
				case HEXLITERAL:
				case BINARY_LITERAL:
				case IDENTIFICATION_VARIABLE:
					{
					setState(880);
					functionArguments();
					}
					break;
				case ASTERISK:
					{
					setState(881);
					match(ASTERISK);
					}
					break;
				case T__2:
					break;
				default:
					break;
				}
				setState(884);
				match(T__2);
				setState(886);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
				case 1:
					{
					setState(885);
					pathContinutation();
					}
					break;
				}
				setState(889);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
				case 1:
					{
					setState(888);
					filterClause();
					}
					break;
				}
				setState(892);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,104,_ctx) ) {
				case 1:
					{
					setState(891);
					withinGroup();
					}
					break;
				}
				setState(895);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
				case 1:
					{
					setState(894);
					overClause();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new FunctionWithSubqueryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(897);
				functionName();
				setState(898);
				match(T__1);
				setState(899);
				subquery();
				setState(900);
				match(T__2);
				}
				break;
			case 3:
				_localctx = new CastFunctionInvocationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(902);
				castFunction();
				}
				break;
			case 4:
				_localctx = new ExtractFunctionInvocationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(903);
				extractFunction();
				}
				break;
			case 5:
				_localctx = new TrimFunctionInvocationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(904);
				trimFunction();
				}
				break;
			case 6:
				_localctx = new EveryFunctionInvocationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(905);
				everyFunction();
				}
				break;
			case 7:
				_localctx = new AnyFunctionInvocationContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(906);
				anyFunction();
				}
				break;
			case 8:
				_localctx = new TreatedPathInvocationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(907);
				treatedPath();
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
	public static class FunctionArgumentsContext extends ParserRuleContext {
		public List<ExpressionOrPredicateContext> expressionOrPredicate() {
			return getRuleContexts(ExpressionOrPredicateContext.class);
		}
		public ExpressionOrPredicateContext expressionOrPredicate(int i) {
			return getRuleContext(ExpressionOrPredicateContext.class,i);
		}
		public TerminalNode DISTINCT() { return getToken(HqlParser.DISTINCT, 0); }
		public FunctionArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFunctionArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFunctionArguments(this);
		}
	}

	public final FunctionArgumentsContext functionArguments() throws RecognitionException {
		FunctionArgumentsContext _localctx = new FunctionArgumentsContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_functionArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(911);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
			case 1:
				{
				setState(910);
				match(DISTINCT);
				}
				break;
			}
			setState(913);
			expressionOrPredicate();
			setState(918);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(914);
				match(T__0);
				setState(915);
				expressionOrPredicate();
				}
				}
				setState(920);
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
	public static class FilterClauseContext extends ParserRuleContext {
		public TerminalNode FILTER() { return getToken(HqlParser.FILTER, 0); }
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public FilterClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFilterClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFilterClause(this);
		}
	}

	public final FilterClauseContext filterClause() throws RecognitionException {
		FilterClauseContext _localctx = new FilterClauseContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_filterClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			match(FILTER);
			setState(922);
			match(T__1);
			setState(923);
			whereClause();
			setState(924);
			match(T__2);
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
	public static class WithinGroupContext extends ParserRuleContext {
		public TerminalNode WITHIN() { return getToken(HqlParser.WITHIN, 0); }
		public TerminalNode GROUP() { return getToken(HqlParser.GROUP, 0); }
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public WithinGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withinGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterWithinGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitWithinGroup(this);
		}
	}

	public final WithinGroupContext withinGroup() throws RecognitionException {
		WithinGroupContext _localctx = new WithinGroupContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_withinGroup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(926);
			match(WITHIN);
			setState(927);
			match(GROUP);
			setState(928);
			match(T__1);
			setState(929);
			orderByClause();
			setState(930);
			match(T__2);
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
	public static class OverClauseContext extends ParserRuleContext {
		public TerminalNode OVER() { return getToken(HqlParser.OVER, 0); }
		public PartitionClauseContext partitionClause() {
			return getRuleContext(PartitionClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public FrameClauseContext frameClause() {
			return getRuleContext(FrameClauseContext.class,0);
		}
		public OverClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_overClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOverClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOverClause(this);
		}
	}

	public final OverClauseContext overClause() throws RecognitionException {
		OverClauseContext _localctx = new OverClauseContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_overClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(932);
			match(OVER);
			setState(933);
			match(T__1);
			setState(935);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(934);
				partitionClause();
				}
			}

			setState(938);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(937);
				orderByClause();
				}
			}

			setState(941);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUPS || _la==RANGE || _la==ROWS) {
				{
				setState(940);
				frameClause();
				}
			}

			setState(943);
			match(T__2);
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
	public static class PartitionClauseContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(HqlParser.PARTITION, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public PartitionClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPartitionClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPartitionClause(this);
		}
	}

	public final PartitionClauseContext partitionClause() throws RecognitionException {
		PartitionClauseContext _localctx = new PartitionClauseContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_partitionClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(945);
			match(PARTITION);
			setState(946);
			match(BY);
			setState(947);
			expression(0);
			setState(952);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(948);
				match(T__0);
				setState(949);
				expression(0);
				}
				}
				setState(954);
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
	public static class FrameClauseContext extends ParserRuleContext {
		public FrameStartContext frameStart() {
			return getRuleContext(FrameStartContext.class,0);
		}
		public TerminalNode RANGE() { return getToken(HqlParser.RANGE, 0); }
		public TerminalNode ROWS() { return getToken(HqlParser.ROWS, 0); }
		public TerminalNode GROUPS() { return getToken(HqlParser.GROUPS, 0); }
		public FrameExclusionContext frameExclusion() {
			return getRuleContext(FrameExclusionContext.class,0);
		}
		public TerminalNode BETWEEN() { return getToken(HqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public FrameEndContext frameEnd() {
			return getRuleContext(FrameEndContext.class,0);
		}
		public FrameClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFrameClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFrameClause(this);
		}
	}

	public final FrameClauseContext frameClause() throws RecognitionException {
		FrameClauseContext _localctx = new FrameClauseContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_frameClause);
		int _la;
		try {
			setState(968);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(955);
				_la = _input.LA(1);
				if ( !(_la==GROUPS || _la==RANGE || _la==ROWS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(956);
				frameStart();
				setState(958);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXCLUDE) {
					{
					setState(957);
					frameExclusion();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(960);
				_la = _input.LA(1);
				if ( !(_la==GROUPS || _la==RANGE || _la==ROWS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(961);
				match(BETWEEN);
				setState(962);
				frameStart();
				setState(963);
				match(AND);
				setState(964);
				frameEnd();
				setState(966);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXCLUDE) {
					{
					setState(965);
					frameExclusion();
					}
				}

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
	public static class FrameStartContext extends ParserRuleContext {
		public FrameStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameStart; }
	 
		public FrameStartContext() { }
		public void copyFrom(FrameStartContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionFollowingFrameStartContext extends FrameStartContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode FOLLOWING() { return getToken(HqlParser.FOLLOWING, 0); }
		public ExpressionFollowingFrameStartContext(FrameStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionFollowingFrameStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionFollowingFrameStart(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionPrecedingFrameStartContext extends FrameStartContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PRECEDING() { return getToken(HqlParser.PRECEDING, 0); }
		public ExpressionPrecedingFrameStartContext(FrameStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionPrecedingFrameStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionPrecedingFrameStart(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CurrentRowFrameStartContext extends FrameStartContext {
		public TerminalNode CURRENT() { return getToken(HqlParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public CurrentRowFrameStartContext(FrameStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCurrentRowFrameStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCurrentRowFrameStart(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnboundedPrecedingFrameStartContext extends FrameStartContext {
		public TerminalNode UNBOUNDED() { return getToken(HqlParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(HqlParser.PRECEDING, 0); }
		public UnboundedPrecedingFrameStartContext(FrameStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterUnboundedPrecedingFrameStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitUnboundedPrecedingFrameStart(this);
		}
	}

	public final FrameStartContext frameStart() throws RecognitionException {
		FrameStartContext _localctx = new FrameStartContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_frameStart);
		try {
			setState(980);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
			case 1:
				_localctx = new UnboundedPrecedingFrameStartContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(970);
				match(UNBOUNDED);
				setState(971);
				match(PRECEDING);
				}
				break;
			case 2:
				_localctx = new ExpressionPrecedingFrameStartContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(972);
				expression(0);
				setState(973);
				match(PRECEDING);
				}
				break;
			case 3:
				_localctx = new CurrentRowFrameStartContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(975);
				match(CURRENT);
				setState(976);
				match(ROW);
				}
				break;
			case 4:
				_localctx = new ExpressionFollowingFrameStartContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(977);
				expression(0);
				setState(978);
				match(FOLLOWING);
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
	public static class FrameExclusionContext extends ParserRuleContext {
		public FrameExclusionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameExclusion; }
	 
		public FrameExclusionContext() { }
		public void copyFrom(FrameExclusionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NoOthersFrameExclusionContext extends FrameExclusionContext {
		public TerminalNode EXCLUDE() { return getToken(HqlParser.EXCLUDE, 0); }
		public TerminalNode NO() { return getToken(HqlParser.NO, 0); }
		public TerminalNode OTHERS() { return getToken(HqlParser.OTHERS, 0); }
		public NoOthersFrameExclusionContext(FrameExclusionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNoOthersFrameExclusion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNoOthersFrameExclusion(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupFrameExclusionContext extends FrameExclusionContext {
		public TerminalNode EXCLUDE() { return getToken(HqlParser.EXCLUDE, 0); }
		public TerminalNode GROUP() { return getToken(HqlParser.GROUP, 0); }
		public GroupFrameExclusionContext(FrameExclusionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGroupFrameExclusion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGroupFrameExclusion(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TiesFrameExclusionContext extends FrameExclusionContext {
		public TerminalNode EXCLUDE() { return getToken(HqlParser.EXCLUDE, 0); }
		public TerminalNode TIES() { return getToken(HqlParser.TIES, 0); }
		public TiesFrameExclusionContext(FrameExclusionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTiesFrameExclusion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTiesFrameExclusion(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CurrentRowFrameExclusionContext extends FrameExclusionContext {
		public TerminalNode EXCLUDE() { return getToken(HqlParser.EXCLUDE, 0); }
		public TerminalNode CURRENT() { return getToken(HqlParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public CurrentRowFrameExclusionContext(FrameExclusionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCurrentRowFrameExclusion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCurrentRowFrameExclusion(this);
		}
	}

	public final FrameExclusionContext frameExclusion() throws RecognitionException {
		FrameExclusionContext _localctx = new FrameExclusionContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_frameExclusion);
		try {
			setState(992);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				_localctx = new CurrentRowFrameExclusionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(982);
				match(EXCLUDE);
				setState(983);
				match(CURRENT);
				setState(984);
				match(ROW);
				}
				break;
			case 2:
				_localctx = new GroupFrameExclusionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(985);
				match(EXCLUDE);
				setState(986);
				match(GROUP);
				}
				break;
			case 3:
				_localctx = new TiesFrameExclusionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(987);
				match(EXCLUDE);
				setState(988);
				match(TIES);
				}
				break;
			case 4:
				_localctx = new NoOthersFrameExclusionContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(989);
				match(EXCLUDE);
				setState(990);
				match(NO);
				setState(991);
				match(OTHERS);
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
	public static class FrameEndContext extends ParserRuleContext {
		public FrameEndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameEnd; }
	 
		public FrameEndContext() { }
		public void copyFrom(FrameEndContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CurrentRowFrameEndContext extends FrameEndContext {
		public TerminalNode CURRENT() { return getToken(HqlParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public CurrentRowFrameEndContext(FrameEndContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCurrentRowFrameEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCurrentRowFrameEnd(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionPrecedingFrameEndContext extends FrameEndContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PRECEDING() { return getToken(HqlParser.PRECEDING, 0); }
		public ExpressionPrecedingFrameEndContext(FrameEndContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionPrecedingFrameEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionPrecedingFrameEnd(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionFollowingFrameEndContext extends FrameEndContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode FOLLOWING() { return getToken(HqlParser.FOLLOWING, 0); }
		public ExpressionFollowingFrameEndContext(FrameEndContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionFollowingFrameEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionFollowingFrameEnd(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnboundedFollowingFrameEndContext extends FrameEndContext {
		public TerminalNode UNBOUNDED() { return getToken(HqlParser.UNBOUNDED, 0); }
		public TerminalNode FOLLOWING() { return getToken(HqlParser.FOLLOWING, 0); }
		public UnboundedFollowingFrameEndContext(FrameEndContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterUnboundedFollowingFrameEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitUnboundedFollowingFrameEnd(this);
		}
	}

	public final FrameEndContext frameEnd() throws RecognitionException {
		FrameEndContext _localctx = new FrameEndContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_frameEnd);
		try {
			setState(1004);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
			case 1:
				_localctx = new ExpressionPrecedingFrameEndContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(994);
				expression(0);
				setState(995);
				match(PRECEDING);
				}
				break;
			case 2:
				_localctx = new CurrentRowFrameEndContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(997);
				match(CURRENT);
				setState(998);
				match(ROW);
				}
				break;
			case 3:
				_localctx = new ExpressionFollowingFrameEndContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(999);
				expression(0);
				setState(1000);
				match(FOLLOWING);
				}
				break;
			case 4:
				_localctx = new UnboundedFollowingFrameEndContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1002);
				match(UNBOUNDED);
				setState(1003);
				match(FOLLOWING);
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
	public static class CastFunctionContext extends ParserRuleContext {
		public TerminalNode CAST() { return getToken(HqlParser.CAST, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public CastTargetContext castTarget() {
			return getRuleContext(CastTargetContext.class,0);
		}
		public CastFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCastFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCastFunction(this);
		}
	}

	public final CastFunctionContext castFunction() throws RecognitionException {
		CastFunctionContext _localctx = new CastFunctionContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_castFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1006);
			match(CAST);
			setState(1007);
			match(T__1);
			setState(1008);
			expression(0);
			setState(1009);
			match(AS);
			setState(1010);
			castTarget();
			setState(1011);
			match(T__2);
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
	public static class CastTargetContext extends ParserRuleContext {
		public CastTargetTypeContext castTargetType() {
			return getRuleContext(CastTargetTypeContext.class,0);
		}
		public List<TerminalNode> INTEGER_LITERAL() { return getTokens(HqlParser.INTEGER_LITERAL); }
		public TerminalNode INTEGER_LITERAL(int i) {
			return getToken(HqlParser.INTEGER_LITERAL, i);
		}
		public CastTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castTarget; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCastTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCastTarget(this);
		}
	}

	public final CastTargetContext castTarget() throws RecognitionException {
		CastTargetContext _localctx = new CastTargetContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_castTarget);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013);
			castTargetType();
			setState(1021);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(1014);
				match(T__1);
				setState(1015);
				match(INTEGER_LITERAL);
				setState(1018);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(1016);
					match(T__0);
					setState(1017);
					match(INTEGER_LITERAL);
					}
				}

				setState(1020);
				match(T__2);
				}
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
	public static class CastTargetTypeContext extends ParserRuleContext {
		public String fullTargetName;
		public IdentifierContext i;
		public IdentifierContext c;
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public CastTargetTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castTargetType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCastTargetType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCastTargetType(this);
		}
	}

	public final CastTargetTypeContext castTargetType() throws RecognitionException {
		CastTargetTypeContext _localctx = new CastTargetTypeContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_castTargetType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(1023);
			((CastTargetTypeContext)_localctx).i = identifier();
			 ((CastTargetTypeContext)_localctx).fullTargetName =  _localctx.i.getText(); 
			}
			setState(1032);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(1026);
				match(T__13);
				setState(1027);
				((CastTargetTypeContext)_localctx).c = identifier();
				 _localctx.fullTargetName += ("." + _localctx.c.getText() ); 
				}
				}
				setState(1034);
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
	public static class ExtractFunctionContext extends ParserRuleContext {
		public TerminalNode EXTRACT() { return getToken(HqlParser.EXTRACT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public DateTimeFunctionContext dateTimeFunction() {
			return getRuleContext(DateTimeFunctionContext.class,0);
		}
		public ExtractFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExtractFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExtractFunction(this);
		}
	}

	public final ExtractFunctionContext extractFunction() throws RecognitionException {
		ExtractFunctionContext _localctx = new ExtractFunctionContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_extractFunction);
		try {
			setState(1047);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXTRACT:
				enterOuterAlt(_localctx, 1);
				{
				setState(1035);
				match(EXTRACT);
				setState(1036);
				match(T__1);
				setState(1037);
				expression(0);
				setState(1038);
				match(FROM);
				setState(1039);
				expression(0);
				setState(1040);
				match(T__2);
				}
				break;
			case DAY:
			case EPOCH:
			case HOUR:
			case MINUTE:
			case MONTH:
			case NANOSECOND:
			case QUARTER:
			case SECOND:
			case WEEK:
			case YEAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(1042);
				dateTimeFunction();
				setState(1043);
				match(T__1);
				setState(1044);
				expression(0);
				setState(1045);
				match(T__2);
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
	public static class TrimFunctionContext extends ParserRuleContext {
		public TerminalNode TRIM() { return getToken(HqlParser.TRIM, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public TerminalNode LEADING() { return getToken(HqlParser.LEADING, 0); }
		public TerminalNode TRAILING() { return getToken(HqlParser.TRAILING, 0); }
		public TerminalNode BOTH() { return getToken(HqlParser.BOTH, 0); }
		public TrimFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTrimFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTrimFunction(this);
		}
	}

	public final TrimFunctionContext trimFunction() throws RecognitionException {
		TrimFunctionContext _localctx = new TrimFunctionContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_trimFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1049);
			match(TRIM);
			setState(1050);
			match(T__1);
			setState(1052);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
			case 1:
				{
				setState(1051);
				_la = _input.LA(1);
				if ( !(_la==BOTH || _la==LEADING || _la==TRAILING) ) {
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
			setState(1055);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,124,_ctx) ) {
			case 1:
				{
				setState(1054);
				stringLiteral();
				}
				break;
			}
			setState(1058);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				{
				setState(1057);
				match(FROM);
				}
				break;
			}
			setState(1060);
			expression(0);
			setState(1061);
			match(T__2);
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
	public static class DateTimeFunctionContext extends ParserRuleContext {
		public Token d;
		public TerminalNode YEAR() { return getToken(HqlParser.YEAR, 0); }
		public TerminalNode MONTH() { return getToken(HqlParser.MONTH, 0); }
		public TerminalNode DAY() { return getToken(HqlParser.DAY, 0); }
		public TerminalNode WEEK() { return getToken(HqlParser.WEEK, 0); }
		public TerminalNode QUARTER() { return getToken(HqlParser.QUARTER, 0); }
		public TerminalNode HOUR() { return getToken(HqlParser.HOUR, 0); }
		public TerminalNode MINUTE() { return getToken(HqlParser.MINUTE, 0); }
		public TerminalNode SECOND() { return getToken(HqlParser.SECOND, 0); }
		public TerminalNode NANOSECOND() { return getToken(HqlParser.NANOSECOND, 0); }
		public TerminalNode EPOCH() { return getToken(HqlParser.EPOCH, 0); }
		public DateTimeFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDateTimeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDateTimeFunction(this);
		}
	}

	public final DateTimeFunctionContext dateTimeFunction() throws RecognitionException {
		DateTimeFunctionContext _localctx = new DateTimeFunctionContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_dateTimeFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1063);
			((DateTimeFunctionContext)_localctx).d = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==DAY || _la==EPOCH || ((((_la - 87)) & ~0x3f) == 0 && ((1L << (_la - 87)) & 3848290697217L) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & 8933531975937L) != 0)) ) {
				((DateTimeFunctionContext)_localctx).d = (Token)_errHandler.recoverInline(this);
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
	public static class EveryFunctionContext extends ParserRuleContext {
		public Token every;
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode EVERY() { return getToken(HqlParser.EVERY, 0); }
		public TerminalNode ALL() { return getToken(HqlParser.ALL, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode ELEMENTS() { return getToken(HqlParser.ELEMENTS, 0); }
		public TerminalNode INDICES() { return getToken(HqlParser.INDICES, 0); }
		public EveryFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_everyFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterEveryFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitEveryFunction(this);
		}
	}

	public final EveryFunctionContext everyFunction() throws RecognitionException {
		EveryFunctionContext _localctx = new EveryFunctionContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_everyFunction);
		int _la;
		try {
			setState(1081);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1065);
				((EveryFunctionContext)_localctx).every = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==EVERY) ) {
					((EveryFunctionContext)_localctx).every = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1066);
				match(T__1);
				setState(1067);
				predicate(0);
				setState(1068);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1070);
				((EveryFunctionContext)_localctx).every = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==EVERY) ) {
					((EveryFunctionContext)_localctx).every = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1071);
				match(T__1);
				setState(1072);
				subquery();
				setState(1073);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1075);
				((EveryFunctionContext)_localctx).every = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==EVERY) ) {
					((EveryFunctionContext)_localctx).every = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1076);
				_la = _input.LA(1);
				if ( !(_la==ELEMENTS || _la==INDICES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1077);
				match(T__1);
				setState(1078);
				simplePath();
				setState(1079);
				match(T__2);
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
	public static class AnyFunctionContext extends ParserRuleContext {
		public Token any;
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode ANY() { return getToken(HqlParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(HqlParser.SOME, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode ELEMENTS() { return getToken(HqlParser.ELEMENTS, 0); }
		public TerminalNode INDICES() { return getToken(HqlParser.INDICES, 0); }
		public AnyFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAnyFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAnyFunction(this);
		}
	}

	public final AnyFunctionContext anyFunction() throws RecognitionException {
		AnyFunctionContext _localctx = new AnyFunctionContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_anyFunction);
		int _la;
		try {
			setState(1099);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1083);
				((AnyFunctionContext)_localctx).any = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ANY || _la==SOME) ) {
					((AnyFunctionContext)_localctx).any = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1084);
				match(T__1);
				setState(1085);
				predicate(0);
				setState(1086);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1088);
				((AnyFunctionContext)_localctx).any = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ANY || _la==SOME) ) {
					((AnyFunctionContext)_localctx).any = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1089);
				match(T__1);
				setState(1090);
				subquery();
				setState(1091);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1093);
				((AnyFunctionContext)_localctx).any = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ANY || _la==SOME) ) {
					((AnyFunctionContext)_localctx).any = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1094);
				_la = _input.LA(1);
				if ( !(_la==ELEMENTS || _la==INDICES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1095);
				match(T__1);
				setState(1096);
				simplePath();
				setState(1097);
				match(T__2);
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
	public static class TreatedPathContext extends ParserRuleContext {
		public TerminalNode TREAT() { return getToken(HqlParser.TREAT, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public PathContinutationContext pathContinutation() {
			return getRuleContext(PathContinutationContext.class,0);
		}
		public TreatedPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_treatedPath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterTreatedPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitTreatedPath(this);
		}
	}

	public final TreatedPathContext treatedPath() throws RecognitionException {
		TreatedPathContext _localctx = new TreatedPathContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_treatedPath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1101);
			match(TREAT);
			setState(1102);
			match(T__1);
			setState(1103);
			path();
			setState(1104);
			match(AS);
			setState(1105);
			simplePath();
			setState(1106);
			match(T__2);
			setState(1108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
			case 1:
				{
				setState(1107);
				pathContinutation();
				}
				break;
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
	public static class PathContinutationContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public PathContinutationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathContinutation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterPathContinutation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitPathContinutation(this);
		}
	}

	public final PathContinutationContext pathContinutation() throws RecognitionException {
		PathContinutationContext _localctx = new PathContinutationContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_pathContinutation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1110);
			match(T__13);
			setState(1111);
			simplePath();
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
	public static class PredicateContext extends ParserRuleContext {
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
	 
		public PredicateContext() { }
		public void copyFrom(PredicateContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullExpressionPredicateContext extends PredicateContext {
		public DealingWithNullExpressionContext dealingWithNullExpression() {
			return getRuleContext(DealingWithNullExpressionContext.class,0);
		}
		public NullExpressionPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNullExpressionPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNullExpressionPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BetweenPredicateContext extends PredicateContext {
		public BetweenExpressionContext betweenExpression() {
			return getRuleContext(BetweenExpressionContext.class,0);
		}
		public BetweenPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBetweenPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBetweenPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrPredicateContext extends PredicateContext {
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public TerminalNode OR() { return getToken(HqlParser.OR, 0); }
		public OrPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterOrPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitOrPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RelationalPredicateContext extends PredicateContext {
		public RelationalExpressionContext relationalExpression() {
			return getRuleContext(RelationalExpressionContext.class,0);
		}
		public RelationalPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterRelationalPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitRelationalPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExistsPredicateContext extends PredicateContext {
		public ExistsExpressionContext existsExpression() {
			return getRuleContext(ExistsExpressionContext.class,0);
		}
		public ExistsPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExistsPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExistsPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CollectionPredicateContext extends PredicateContext {
		public CollectionExpressionContext collectionExpression() {
			return getRuleContext(CollectionExpressionContext.class,0);
		}
		public CollectionPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCollectionPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCollectionPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndPredicateContext extends PredicateContext {
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public AndPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterAndPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitAndPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupedPredicateContext extends PredicateContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public GroupedPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterGroupedPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitGroupedPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LikePredicateContext extends PredicateContext {
		public StringPatternMatchingContext stringPatternMatching() {
			return getRuleContext(StringPatternMatchingContext.class,0);
		}
		public LikePredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterLikePredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitLikePredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InPredicateContext extends PredicateContext {
		public InExpressionContext inExpression() {
			return getRuleContext(InExpressionContext.class,0);
		}
		public InPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotPredicateContext extends PredicateContext {
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public NotPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterNotPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitNotPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionPredicateContext extends PredicateContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionPredicate(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		return predicate(0);
	}

	private PredicateContext predicate(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PredicateContext _localctx = new PredicateContext(_ctx, _parentState);
		PredicateContext _prevctx = _localctx;
		int _startState = 192;
		enterRecursionRule(_localctx, 192, RULE_predicate, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
			case 1:
				{
				_localctx = new GroupedPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1114);
				match(T__1);
				setState(1115);
				predicate(0);
				setState(1116);
				match(T__2);
				}
				break;
			case 2:
				{
				_localctx = new NullExpressionPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1118);
				dealingWithNullExpression();
				}
				break;
			case 3:
				{
				_localctx = new InPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1119);
				inExpression();
				}
				break;
			case 4:
				{
				_localctx = new BetweenPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1120);
				betweenExpression();
				}
				break;
			case 5:
				{
				_localctx = new RelationalPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1121);
				relationalExpression();
				}
				break;
			case 6:
				{
				_localctx = new LikePredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1122);
				stringPatternMatching();
				}
				break;
			case 7:
				{
				_localctx = new ExistsPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1123);
				existsExpression();
				}
				break;
			case 8:
				{
				_localctx = new CollectionPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1124);
				collectionExpression();
				}
				break;
			case 9:
				{
				_localctx = new NotPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1125);
				match(NOT);
				setState(1126);
				predicate(4);
				}
				break;
			case 10:
				{
				_localctx = new ExpressionPredicateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1127);
				expression(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1138);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1136);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
					case 1:
						{
						_localctx = new AndPredicateContext(new PredicateContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_predicate);
						setState(1130);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1131);
						match(AND);
						setState(1132);
						predicate(4);
						}
						break;
					case 2:
						{
						_localctx = new OrPredicateContext(new PredicateContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_predicate);
						setState(1133);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1134);
						match(OR);
						setState(1135);
						predicate(3);
						}
						break;
					}
					} 
				}
				setState(1140);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,131,_ctx);
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
	public static class ExpressionOrPredicateContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public ExpressionOrPredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionOrPredicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExpressionOrPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExpressionOrPredicate(this);
		}
	}

	public final ExpressionOrPredicateContext expressionOrPredicate() throws RecognitionException {
		ExpressionOrPredicateContext _localctx = new ExpressionOrPredicateContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_expressionOrPredicate);
		try {
			setState(1143);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1141);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1142);
				predicate(0);
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
	public static class RelationalExpressionContext extends ParserRuleContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitRelationalExpression(this);
		}
	}

	public final RelationalExpressionContext relationalExpression() throws RecognitionException {
		RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_relationalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1145);
			expression(0);
			setState(1146);
			((RelationalExpressionContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4161552L) != 0)) ) {
				((RelationalExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1147);
			expression(0);
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
	public static class BetweenExpressionContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(HqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public BetweenExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_betweenExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterBetweenExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitBetweenExpression(this);
		}
	}

	public final BetweenExpressionContext betweenExpression() throws RecognitionException {
		BetweenExpressionContext _localctx = new BetweenExpressionContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_betweenExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1149);
			expression(0);
			setState(1151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(1150);
				match(NOT);
				}
			}

			setState(1153);
			match(BETWEEN);
			setState(1154);
			expression(0);
			setState(1155);
			match(AND);
			setState(1156);
			expression(0);
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
	public static class DealingWithNullExpressionContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IS() { return getToken(HqlParser.IS, 0); }
		public TerminalNode NULL() { return getToken(HqlParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode DISTINCT() { return getToken(HqlParser.DISTINCT, 0); }
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public DealingWithNullExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dealingWithNullExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterDealingWithNullExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitDealingWithNullExpression(this);
		}
	}

	public final DealingWithNullExpressionContext dealingWithNullExpression() throws RecognitionException {
		DealingWithNullExpressionContext _localctx = new DealingWithNullExpressionContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_dealingWithNullExpression);
		int _la;
		try {
			setState(1174);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1158);
				expression(0);
				setState(1159);
				match(IS);
				setState(1161);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1160);
					match(NOT);
					}
				}

				setState(1163);
				match(NULL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1165);
				expression(0);
				setState(1166);
				match(IS);
				setState(1168);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1167);
					match(NOT);
					}
				}

				setState(1170);
				match(DISTINCT);
				setState(1171);
				match(FROM);
				setState(1172);
				expression(0);
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
	public static class StringPatternMatchingContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(HqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(HqlParser.ILIKE, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode ESCAPE() { return getToken(HqlParser.ESCAPE, 0); }
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public StringPatternMatchingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringPatternMatching; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterStringPatternMatching(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitStringPatternMatching(this);
		}
	}

	public final StringPatternMatchingContext stringPatternMatching() throws RecognitionException {
		StringPatternMatchingContext _localctx = new StringPatternMatchingContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_stringPatternMatching);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1176);
			expression(0);
			setState(1178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(1177);
				match(NOT);
				}
			}

			setState(1180);
			_la = _input.LA(1);
			if ( !(_la==ILIKE || _la==LIKE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(1181);
				match(T__4);
				}
			}

			setState(1184);
			expression(0);
			setState(1186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
			case 1:
				{
				setState(1185);
				match(T__4);
				}
				break;
			}
			setState(1193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
			case 1:
				{
				setState(1188);
				match(ESCAPE);
				setState(1191);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CHARACTER:
				case STRINGLITERAL:
				case JAVASTRINGLITERAL:
					{
					setState(1189);
					stringLiteral();
					}
					break;
				case T__21:
				case T__22:
					{
					setState(1190);
					parameter();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
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
	public static class InExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(HqlParser.IN, 0); }
		public InListContext inList() {
			return getRuleContext(InListContext.class,0);
		}
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public InExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInExpression(this);
		}
	}

	public final InExpressionContext inExpression() throws RecognitionException {
		InExpressionContext _localctx = new InExpressionContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_inExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1195);
			expression(0);
			setState(1197);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(1196);
				match(NOT);
				}
			}

			setState(1199);
			match(IN);
			setState(1200);
			inList();
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
	public static class InListContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode ELEMENTS() { return getToken(HqlParser.ELEMENTS, 0); }
		public TerminalNode INDICES() { return getToken(HqlParser.INDICES, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public List<ExpressionOrPredicateContext> expressionOrPredicate() {
			return getRuleContexts(ExpressionOrPredicateContext.class);
		}
		public ExpressionOrPredicateContext expressionOrPredicate(int i) {
			return getRuleContext(ExpressionOrPredicateContext.class,i);
		}
		public InListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInList(this);
		}
	}

	public final InListContext inList() throws RecognitionException {
		InListContext _localctx = new InListContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_inList);
		int _la;
		try {
			setState(1224);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1202);
				_la = _input.LA(1);
				if ( !(_la==ELEMENTS || _la==INDICES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1203);
				match(T__1);
				setState(1204);
				simplePath();
				setState(1205);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1207);
				match(T__1);
				setState(1208);
				subquery();
				setState(1209);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1211);
				parameter();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1212);
				match(T__1);
				setState(1221);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -275972619452L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -70918499995713L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & -1L) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & 65535L) != 0)) {
					{
					setState(1213);
					expressionOrPredicate();
					setState(1218);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__0) {
						{
						{
						setState(1214);
						match(T__0);
						setState(1215);
						expressionOrPredicate();
						}
						}
						setState(1220);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(1223);
				match(T__2);
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
	public static class ExistsExpressionContext extends ParserRuleContext {
		public TerminalNode EXISTS() { return getToken(HqlParser.EXISTS, 0); }
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode ELEMENTS() { return getToken(HqlParser.ELEMENTS, 0); }
		public TerminalNode INDICES() { return getToken(HqlParser.INDICES, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExistsExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_existsExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterExistsExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitExistsExpression(this);
		}
	}

	public final ExistsExpressionContext existsExpression() throws RecognitionException {
		ExistsExpressionContext _localctx = new ExistsExpressionContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_existsExpression);
		int _la;
		try {
			setState(1234);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1226);
				match(EXISTS);
				setState(1227);
				_la = _input.LA(1);
				if ( !(_la==ELEMENTS || _la==INDICES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1228);
				match(T__1);
				setState(1229);
				simplePath();
				setState(1230);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1232);
				match(EXISTS);
				setState(1233);
				expression(0);
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
	public static class CollectionExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IS() { return getToken(HqlParser.IS, 0); }
		public TerminalNode EMPTY() { return getToken(HqlParser.EMPTY, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode MEMBER() { return getToken(HqlParser.MEMBER, 0); }
		public TerminalNode OF() { return getToken(HqlParser.OF, 0); }
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public CollectionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collectionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCollectionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCollectionExpression(this);
		}
	}

	public final CollectionExpressionContext collectionExpression() throws RecognitionException {
		CollectionExpressionContext _localctx = new CollectionExpressionContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_collectionExpression);
		int _la;
		try {
			setState(1251);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1236);
				expression(0);
				setState(1237);
				match(IS);
				setState(1239);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1238);
					match(NOT);
					}
				}

				setState(1241);
				match(EMPTY);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1243);
				expression(0);
				setState(1245);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1244);
					match(NOT);
					}
				}

				setState(1247);
				match(MEMBER);
				setState(1248);
				match(OF);
				setState(1249);
				path();
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
	public static class InstantiationTargetContext extends ParserRuleContext {
		public TerminalNode LIST() { return getToken(HqlParser.LIST, 0); }
		public TerminalNode MAP() { return getToken(HqlParser.MAP, 0); }
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public InstantiationTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instantiationTarget; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInstantiationTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInstantiationTarget(this);
		}
	}

	public final InstantiationTargetContext instantiationTarget() throws RecognitionException {
		InstantiationTargetContext _localctx = new InstantiationTargetContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_instantiationTarget);
		try {
			setState(1256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1253);
				match(LIST);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1254);
				match(MAP);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1255);
				simplePath();
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
	public static class InstantiationArgumentsContext extends ParserRuleContext {
		public List<InstantiationArgumentContext> instantiationArgument() {
			return getRuleContexts(InstantiationArgumentContext.class);
		}
		public InstantiationArgumentContext instantiationArgument(int i) {
			return getRuleContext(InstantiationArgumentContext.class,i);
		}
		public InstantiationArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instantiationArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInstantiationArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInstantiationArguments(this);
		}
	}

	public final InstantiationArgumentsContext instantiationArguments() throws RecognitionException {
		InstantiationArgumentsContext _localctx = new InstantiationArgumentsContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_instantiationArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1258);
			instantiationArgument();
			setState(1263);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(1259);
				match(T__0);
				setState(1260);
				instantiationArgument();
				}
				}
				setState(1265);
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
	public static class InstantiationArgumentContext extends ParserRuleContext {
		public ExpressionOrPredicateContext expressionOrPredicate() {
			return getRuleContext(ExpressionOrPredicateContext.class,0);
		}
		public InstantiationContext instantiation() {
			return getRuleContext(InstantiationContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public InstantiationArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instantiationArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterInstantiationArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitInstantiationArgument(this);
		}
	}

	public final InstantiationArgumentContext instantiationArgument() throws RecognitionException {
		InstantiationArgumentContext _localctx = new InstantiationArgumentContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_instantiationArgument);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1268);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
			case 1:
				{
				setState(1266);
				expressionOrPredicate();
				}
				break;
			case 2:
				{
				setState(1267);
				instantiation();
				}
				break;
			}
			setState(1271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 25)) & ~0x3f) == 0 && ((1L << (_la - 25)) & -2427721674137633L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & -35184374202369L) != 0) || ((((_la - 153)) & ~0x3f) == 0 && ((1L << (_la - 153)) & 18155135729401855L) != 0)) {
				{
				setState(1270);
				variable();
				}
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
	public static class ParameterOrIntegerLiteralContext extends ParserRuleContext {
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public TerminalNode INTEGER_LITERAL() { return getToken(HqlParser.INTEGER_LITERAL, 0); }
		public ParameterOrIntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterOrIntegerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterParameterOrIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitParameterOrIntegerLiteral(this);
		}
	}

	public final ParameterOrIntegerLiteralContext parameterOrIntegerLiteral() throws RecognitionException {
		ParameterOrIntegerLiteralContext _localctx = new ParameterOrIntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_parameterOrIntegerLiteral);
		try {
			setState(1275);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__21:
			case T__22:
				enterOuterAlt(_localctx, 1);
				{
				setState(1273);
				parameter();
				}
				break;
			case INTEGER_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1274);
				match(INTEGER_LITERAL);
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
	public static class ParameterOrNumberLiteralContext extends ParserRuleContext {
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public NumericLiteralContext numericLiteral() {
			return getRuleContext(NumericLiteralContext.class,0);
		}
		public ParameterOrNumberLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterOrNumberLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterParameterOrNumberLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitParameterOrNumberLiteral(this);
		}
	}

	public final ParameterOrNumberLiteralContext parameterOrNumberLiteral() throws RecognitionException {
		ParameterOrNumberLiteralContext _localctx = new ParameterOrNumberLiteralContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_parameterOrNumberLiteral);
		try {
			setState(1279);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__21:
			case T__22:
				enterOuterAlt(_localctx, 1);
				{
				setState(1277);
				parameter();
				}
				break;
			case INTEGER_LITERAL:
			case FLOAT_LITERAL:
			case HEXLITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1278);
				numericLiteral();
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
	public static class VariableContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ReservedWordContext reservedWord() {
			return getRuleContext(ReservedWordContext.class,0);
		}
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_variable);
		try {
			setState(1284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1281);
				match(AS);
				setState(1282);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1283);
				reservedWord();
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
	public static class ParameterContext extends ParserRuleContext {
		public Token prefix;
		public TerminalNode SPEL() { return getToken(HqlParser.SPEL, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode INTEGER_LITERAL() { return getToken(HqlParser.INTEGER_LITERAL, 0); }
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitParameter(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_parameter);
		try {
			setState(1296);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1286);
				((ParameterContext)_localctx).prefix = match(T__21);
				setState(1287);
				match(SPEL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1288);
				((ParameterContext)_localctx).prefix = match(T__21);
				setState(1289);
				identifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1290);
				((ParameterContext)_localctx).prefix = match(T__22);
				setState(1291);
				match(SPEL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1292);
				((ParameterContext)_localctx).prefix = match(T__22);
				setState(1294);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
				case 1:
					{
					setState(1293);
					match(INTEGER_LITERAL);
					}
					break;
				}
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
	public static class EntityNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode SPEL() { return getToken(HqlParser.SPEL, 0); }
		public EntityNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entityName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterEntityName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitEntityName(this);
		}
	}

	public final EntityNameContext entityName() throws RecognitionException {
		EntityNameContext _localctx = new EntityNameContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_entityName);
		int _la;
		try {
			setState(1307);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
			case AND:
			case ANY:
			case AS:
			case ASC:
			case AVG:
			case BETWEEN:
			case BOTH:
			case BREADTH:
			case BY:
			case CASE:
			case CAST:
			case COLLATE:
			case COUNT:
			case CROSS:
			case CUBE:
			case CURRENT:
			case CURRENT_DATE:
			case CURRENT_INSTANT:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case CYCLE:
			case DATE:
			case DATETIME:
			case DAY:
			case DEFAULT:
			case DELETE:
			case DEPTH:
			case DESC:
			case DISTINCT:
			case ELEMENT:
			case ELEMENTS:
			case ELSE:
			case EMPTY:
			case END:
			case ENTRY:
			case EPOCH:
			case ERROR:
			case ESCAPE:
			case EVERY:
			case EXCEPT:
			case EXCLUDE:
			case EXISTS:
			case EXTRACT:
			case FETCH:
			case FILTER:
			case FIRST:
			case FLOOR:
			case FOLLOWING:
			case FOR:
			case FORMAT:
			case FROM:
			case FULL:
			case FUNCTION:
			case GROUP:
			case GROUPS:
			case HAVING:
			case HOUR:
			case ID:
			case IGNORE:
			case ILIKE:
			case IN:
			case INDEX:
			case INDICES:
			case INNER:
			case INSERT:
			case INSTANT:
			case INTERSECT:
			case INTO:
			case IS:
			case JOIN:
			case KEY:
			case LAST:
			case LEADING:
			case LEFT:
			case LIKE:
			case LIMIT:
			case LIST:
			case LISTAGG:
			case LOCAL:
			case LOCAL_DATE:
			case LOCAL_DATETIME:
			case LOCAL_TIME:
			case MAP:
			case MATERIALIZED:
			case MAX:
			case MAXELEMENT:
			case MAXINDEX:
			case MEMBER:
			case MICROSECOND:
			case MILLISECOND:
			case MIN:
			case MINELEMENT:
			case MININDEX:
			case MINUTE:
			case MONTH:
			case NANOSECOND:
			case NATURALID:
			case NEW:
			case NEXT:
			case NO:
			case NOT:
			case NULLS:
			case OBJECT:
			case OF:
			case OFFSET:
			case OFFSET_DATETIME:
			case ON:
			case ONLY:
			case OR:
			case ORDER:
			case OTHERS:
			case OUTER:
			case OVER:
			case OVERFLOW:
			case OVERLAY:
			case PAD:
			case PARTITION:
			case PERCENT:
			case PLACING:
			case POSITION:
			case POWER:
			case PRECEDING:
			case QUARTER:
			case RANGE:
			case RESPECT:
			case RIGHT:
			case ROLLUP:
			case ROW:
			case ROWS:
			case SEARCH:
			case SECOND:
			case SELECT:
			case SET:
			case SIZE:
			case SOME:
			case SUBSTRING:
			case SUM:
			case THEN:
			case TIES:
			case TIME:
			case TIMESTAMP:
			case TIMEZONE_HOUR:
			case TIMEZONE_MINUTE:
			case TO:
			case TRAILING:
			case TREAT:
			case TRIM:
			case TRUNC:
			case TRUNCATE:
			case TYPE:
			case UNBOUNDED:
			case UNION:
			case UPDATE:
			case USING:
			case VALUE:
			case VALUES:
			case VERSION:
			case VERSIONED:
			case WEEK:
			case WHEN:
			case WHERE:
			case WITH:
			case WITHIN:
			case WITHOUT:
			case YEAR:
			case IDENTIFICATION_VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1298);
				identifier();
				setState(1303);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__13) {
					{
					{
					setState(1299);
					match(T__13);
					setState(1300);
					identifier();
					}
					}
					setState(1305);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case SPEL:
				enterOuterAlt(_localctx, 2);
				{
				setState(1306);
				match(SPEL);
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
	public static class IdentifierContext extends ParserRuleContext {
		public ReservedWordContext reservedWord() {
			return getRuleContext(ReservedWordContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_identifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1309);
			reservedWord();
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
	public static class CharacterContext extends ParserRuleContext {
		public TerminalNode CHARACTER() { return getToken(HqlParser.CHARACTER, 0); }
		public CharacterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_character; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterCharacter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitCharacter(this);
		}
	}

	public final CharacterContext character() throws RecognitionException {
		CharacterContext _localctx = new CharacterContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_character);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1311);
			match(CHARACTER);
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
	public static class FunctionNameContext extends ParserRuleContext {
		public List<ReservedWordContext> reservedWord() {
			return getRuleContexts(ReservedWordContext.class);
		}
		public ReservedWordContext reservedWord(int i) {
			return getRuleContext(ReservedWordContext.class,i);
		}
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitFunctionName(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_functionName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1313);
			reservedWord();
			setState(1318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(1314);
				match(T__13);
				setState(1315);
				reservedWord();
				}
				}
				setState(1320);
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
	public static class ReservedWordContext extends ParserRuleContext {
		public Token f;
		public TerminalNode IDENTIFICATION_VARIABLE() { return getToken(HqlParser.IDENTIFICATION_VARIABLE, 0); }
		public TerminalNode ALL() { return getToken(HqlParser.ALL, 0); }
		public TerminalNode AND() { return getToken(HqlParser.AND, 0); }
		public TerminalNode ANY() { return getToken(HqlParser.ANY, 0); }
		public TerminalNode AS() { return getToken(HqlParser.AS, 0); }
		public TerminalNode ASC() { return getToken(HqlParser.ASC, 0); }
		public TerminalNode AVG() { return getToken(HqlParser.AVG, 0); }
		public TerminalNode BETWEEN() { return getToken(HqlParser.BETWEEN, 0); }
		public TerminalNode BOTH() { return getToken(HqlParser.BOTH, 0); }
		public TerminalNode BREADTH() { return getToken(HqlParser.BREADTH, 0); }
		public TerminalNode BY() { return getToken(HqlParser.BY, 0); }
		public TerminalNode CASE() { return getToken(HqlParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(HqlParser.CAST, 0); }
		public TerminalNode COLLATE() { return getToken(HqlParser.COLLATE, 0); }
		public TerminalNode COUNT() { return getToken(HqlParser.COUNT, 0); }
		public TerminalNode CROSS() { return getToken(HqlParser.CROSS, 0); }
		public TerminalNode CUBE() { return getToken(HqlParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(HqlParser.CURRENT, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(HqlParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_INSTANT() { return getToken(HqlParser.CURRENT_INSTANT, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(HqlParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(HqlParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CYCLE() { return getToken(HqlParser.CYCLE, 0); }
		public TerminalNode DATE() { return getToken(HqlParser.DATE, 0); }
		public TerminalNode DATETIME() { return getToken(HqlParser.DATETIME, 0); }
		public TerminalNode DAY() { return getToken(HqlParser.DAY, 0); }
		public TerminalNode DEFAULT() { return getToken(HqlParser.DEFAULT, 0); }
		public TerminalNode DELETE() { return getToken(HqlParser.DELETE, 0); }
		public TerminalNode DEPTH() { return getToken(HqlParser.DEPTH, 0); }
		public TerminalNode DESC() { return getToken(HqlParser.DESC, 0); }
		public TerminalNode DISTINCT() { return getToken(HqlParser.DISTINCT, 0); }
		public TerminalNode ELEMENT() { return getToken(HqlParser.ELEMENT, 0); }
		public TerminalNode ELEMENTS() { return getToken(HqlParser.ELEMENTS, 0); }
		public TerminalNode ELSE() { return getToken(HqlParser.ELSE, 0); }
		public TerminalNode EMPTY() { return getToken(HqlParser.EMPTY, 0); }
		public TerminalNode END() { return getToken(HqlParser.END, 0); }
		public TerminalNode ENTRY() { return getToken(HqlParser.ENTRY, 0); }
		public TerminalNode EPOCH() { return getToken(HqlParser.EPOCH, 0); }
		public TerminalNode ERROR() { return getToken(HqlParser.ERROR, 0); }
		public TerminalNode ESCAPE() { return getToken(HqlParser.ESCAPE, 0); }
		public TerminalNode EVERY() { return getToken(HqlParser.EVERY, 0); }
		public TerminalNode EXCEPT() { return getToken(HqlParser.EXCEPT, 0); }
		public TerminalNode EXCLUDE() { return getToken(HqlParser.EXCLUDE, 0); }
		public TerminalNode EXISTS() { return getToken(HqlParser.EXISTS, 0); }
		public TerminalNode EXTRACT() { return getToken(HqlParser.EXTRACT, 0); }
		public TerminalNode FETCH() { return getToken(HqlParser.FETCH, 0); }
		public TerminalNode FILTER() { return getToken(HqlParser.FILTER, 0); }
		public TerminalNode FIRST() { return getToken(HqlParser.FIRST, 0); }
		public TerminalNode FLOOR() { return getToken(HqlParser.FLOOR, 0); }
		public TerminalNode FOLLOWING() { return getToken(HqlParser.FOLLOWING, 0); }
		public TerminalNode FOR() { return getToken(HqlParser.FOR, 0); }
		public TerminalNode FORMAT() { return getToken(HqlParser.FORMAT, 0); }
		public TerminalNode FROM() { return getToken(HqlParser.FROM, 0); }
		public TerminalNode FULL() { return getToken(HqlParser.FULL, 0); }
		public TerminalNode FUNCTION() { return getToken(HqlParser.FUNCTION, 0); }
		public TerminalNode GROUP() { return getToken(HqlParser.GROUP, 0); }
		public TerminalNode GROUPS() { return getToken(HqlParser.GROUPS, 0); }
		public TerminalNode HAVING() { return getToken(HqlParser.HAVING, 0); }
		public TerminalNode HOUR() { return getToken(HqlParser.HOUR, 0); }
		public TerminalNode ID() { return getToken(HqlParser.ID, 0); }
		public TerminalNode IGNORE() { return getToken(HqlParser.IGNORE, 0); }
		public TerminalNode ILIKE() { return getToken(HqlParser.ILIKE, 0); }
		public TerminalNode IN() { return getToken(HqlParser.IN, 0); }
		public TerminalNode INDEX() { return getToken(HqlParser.INDEX, 0); }
		public TerminalNode INDICES() { return getToken(HqlParser.INDICES, 0); }
		public TerminalNode INNER() { return getToken(HqlParser.INNER, 0); }
		public TerminalNode INSERT() { return getToken(HqlParser.INSERT, 0); }
		public TerminalNode INSTANT() { return getToken(HqlParser.INSTANT, 0); }
		public TerminalNode INTERSECT() { return getToken(HqlParser.INTERSECT, 0); }
		public TerminalNode INTO() { return getToken(HqlParser.INTO, 0); }
		public TerminalNode IS() { return getToken(HqlParser.IS, 0); }
		public TerminalNode JOIN() { return getToken(HqlParser.JOIN, 0); }
		public TerminalNode KEY() { return getToken(HqlParser.KEY, 0); }
		public TerminalNode LAST() { return getToken(HqlParser.LAST, 0); }
		public TerminalNode LEADING() { return getToken(HqlParser.LEADING, 0); }
		public TerminalNode LEFT() { return getToken(HqlParser.LEFT, 0); }
		public TerminalNode LIKE() { return getToken(HqlParser.LIKE, 0); }
		public TerminalNode LIMIT() { return getToken(HqlParser.LIMIT, 0); }
		public TerminalNode LIST() { return getToken(HqlParser.LIST, 0); }
		public TerminalNode LISTAGG() { return getToken(HqlParser.LISTAGG, 0); }
		public TerminalNode LOCAL() { return getToken(HqlParser.LOCAL, 0); }
		public TerminalNode LOCAL_DATE() { return getToken(HqlParser.LOCAL_DATE, 0); }
		public TerminalNode LOCAL_DATETIME() { return getToken(HqlParser.LOCAL_DATETIME, 0); }
		public TerminalNode LOCAL_TIME() { return getToken(HqlParser.LOCAL_TIME, 0); }
		public TerminalNode MAP() { return getToken(HqlParser.MAP, 0); }
		public TerminalNode MATERIALIZED() { return getToken(HqlParser.MATERIALIZED, 0); }
		public TerminalNode MAX() { return getToken(HqlParser.MAX, 0); }
		public TerminalNode MAXELEMENT() { return getToken(HqlParser.MAXELEMENT, 0); }
		public TerminalNode MAXINDEX() { return getToken(HqlParser.MAXINDEX, 0); }
		public TerminalNode MEMBER() { return getToken(HqlParser.MEMBER, 0); }
		public TerminalNode MICROSECOND() { return getToken(HqlParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(HqlParser.MILLISECOND, 0); }
		public TerminalNode MIN() { return getToken(HqlParser.MIN, 0); }
		public TerminalNode MINELEMENT() { return getToken(HqlParser.MINELEMENT, 0); }
		public TerminalNode MININDEX() { return getToken(HqlParser.MININDEX, 0); }
		public TerminalNode MINUTE() { return getToken(HqlParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(HqlParser.MONTH, 0); }
		public TerminalNode NANOSECOND() { return getToken(HqlParser.NANOSECOND, 0); }
		public TerminalNode NATURALID() { return getToken(HqlParser.NATURALID, 0); }
		public TerminalNode NEW() { return getToken(HqlParser.NEW, 0); }
		public TerminalNode NEXT() { return getToken(HqlParser.NEXT, 0); }
		public TerminalNode NO() { return getToken(HqlParser.NO, 0); }
		public TerminalNode NOT() { return getToken(HqlParser.NOT, 0); }
		public TerminalNode NULLS() { return getToken(HqlParser.NULLS, 0); }
		public TerminalNode OBJECT() { return getToken(HqlParser.OBJECT, 0); }
		public TerminalNode OF() { return getToken(HqlParser.OF, 0); }
		public TerminalNode OFFSET() { return getToken(HqlParser.OFFSET, 0); }
		public TerminalNode OFFSET_DATETIME() { return getToken(HqlParser.OFFSET_DATETIME, 0); }
		public TerminalNode ON() { return getToken(HqlParser.ON, 0); }
		public TerminalNode ONLY() { return getToken(HqlParser.ONLY, 0); }
		public TerminalNode OR() { return getToken(HqlParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(HqlParser.ORDER, 0); }
		public TerminalNode OTHERS() { return getToken(HqlParser.OTHERS, 0); }
		public TerminalNode OUTER() { return getToken(HqlParser.OUTER, 0); }
		public TerminalNode OVER() { return getToken(HqlParser.OVER, 0); }
		public TerminalNode OVERFLOW() { return getToken(HqlParser.OVERFLOW, 0); }
		public TerminalNode OVERLAY() { return getToken(HqlParser.OVERLAY, 0); }
		public TerminalNode PAD() { return getToken(HqlParser.PAD, 0); }
		public TerminalNode PARTITION() { return getToken(HqlParser.PARTITION, 0); }
		public TerminalNode PERCENT() { return getToken(HqlParser.PERCENT, 0); }
		public TerminalNode PLACING() { return getToken(HqlParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(HqlParser.POSITION, 0); }
		public TerminalNode POWER() { return getToken(HqlParser.POWER, 0); }
		public TerminalNode PRECEDING() { return getToken(HqlParser.PRECEDING, 0); }
		public TerminalNode QUARTER() { return getToken(HqlParser.QUARTER, 0); }
		public TerminalNode RANGE() { return getToken(HqlParser.RANGE, 0); }
		public TerminalNode RESPECT() { return getToken(HqlParser.RESPECT, 0); }
		public TerminalNode RIGHT() { return getToken(HqlParser.RIGHT, 0); }
		public TerminalNode ROLLUP() { return getToken(HqlParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(HqlParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(HqlParser.ROWS, 0); }
		public TerminalNode SEARCH() { return getToken(HqlParser.SEARCH, 0); }
		public TerminalNode SECOND() { return getToken(HqlParser.SECOND, 0); }
		public TerminalNode SELECT() { return getToken(HqlParser.SELECT, 0); }
		public TerminalNode SET() { return getToken(HqlParser.SET, 0); }
		public TerminalNode SIZE() { return getToken(HqlParser.SIZE, 0); }
		public TerminalNode SOME() { return getToken(HqlParser.SOME, 0); }
		public TerminalNode SUBSTRING() { return getToken(HqlParser.SUBSTRING, 0); }
		public TerminalNode SUM() { return getToken(HqlParser.SUM, 0); }
		public TerminalNode THEN() { return getToken(HqlParser.THEN, 0); }
		public TerminalNode TIES() { return getToken(HqlParser.TIES, 0); }
		public TerminalNode TIME() { return getToken(HqlParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(HqlParser.TIMESTAMP, 0); }
		public TerminalNode TIMEZONE_HOUR() { return getToken(HqlParser.TIMEZONE_HOUR, 0); }
		public TerminalNode TIMEZONE_MINUTE() { return getToken(HqlParser.TIMEZONE_MINUTE, 0); }
		public TerminalNode TO() { return getToken(HqlParser.TO, 0); }
		public TerminalNode TRAILING() { return getToken(HqlParser.TRAILING, 0); }
		public TerminalNode TREAT() { return getToken(HqlParser.TREAT, 0); }
		public TerminalNode TRIM() { return getToken(HqlParser.TRIM, 0); }
		public TerminalNode TRUNC() { return getToken(HqlParser.TRUNC, 0); }
		public TerminalNode TRUNCATE() { return getToken(HqlParser.TRUNCATE, 0); }
		public TerminalNode TYPE() { return getToken(HqlParser.TYPE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(HqlParser.UNBOUNDED, 0); }
		public TerminalNode UNION() { return getToken(HqlParser.UNION, 0); }
		public TerminalNode UPDATE() { return getToken(HqlParser.UPDATE, 0); }
		public TerminalNode USING() { return getToken(HqlParser.USING, 0); }
		public TerminalNode VALUE() { return getToken(HqlParser.VALUE, 0); }
		public TerminalNode VALUES() { return getToken(HqlParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(HqlParser.VERSION, 0); }
		public TerminalNode VERSIONED() { return getToken(HqlParser.VERSIONED, 0); }
		public TerminalNode WEEK() { return getToken(HqlParser.WEEK, 0); }
		public TerminalNode WHEN() { return getToken(HqlParser.WHEN, 0); }
		public TerminalNode WHERE() { return getToken(HqlParser.WHERE, 0); }
		public TerminalNode WITH() { return getToken(HqlParser.WITH, 0); }
		public TerminalNode WITHIN() { return getToken(HqlParser.WITHIN, 0); }
		public TerminalNode WITHOUT() { return getToken(HqlParser.WITHOUT, 0); }
		public TerminalNode YEAR() { return getToken(HqlParser.YEAR, 0); }
		public ReservedWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reservedWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).enterReservedWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof HqlListener ) ((HqlListener)listener).exitReservedWord(this);
		}
	}

	public final ReservedWordContext reservedWord() throws RecognitionException {
		ReservedWordContext _localctx = new ReservedWordContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_reservedWord);
		int _la;
		try {
			setState(1323);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFICATION_VARIABLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1321);
				match(IDENTIFICATION_VARIABLE);
				}
				break;
			case ALL:
			case AND:
			case ANY:
			case AS:
			case ASC:
			case AVG:
			case BETWEEN:
			case BOTH:
			case BREADTH:
			case BY:
			case CASE:
			case CAST:
			case COLLATE:
			case COUNT:
			case CROSS:
			case CUBE:
			case CURRENT:
			case CURRENT_DATE:
			case CURRENT_INSTANT:
			case CURRENT_TIME:
			case CURRENT_TIMESTAMP:
			case CYCLE:
			case DATE:
			case DATETIME:
			case DAY:
			case DEFAULT:
			case DELETE:
			case DEPTH:
			case DESC:
			case DISTINCT:
			case ELEMENT:
			case ELEMENTS:
			case ELSE:
			case EMPTY:
			case END:
			case ENTRY:
			case EPOCH:
			case ERROR:
			case ESCAPE:
			case EVERY:
			case EXCEPT:
			case EXCLUDE:
			case EXISTS:
			case EXTRACT:
			case FETCH:
			case FILTER:
			case FIRST:
			case FLOOR:
			case FOLLOWING:
			case FOR:
			case FORMAT:
			case FROM:
			case FULL:
			case FUNCTION:
			case GROUP:
			case GROUPS:
			case HAVING:
			case HOUR:
			case ID:
			case IGNORE:
			case ILIKE:
			case IN:
			case INDEX:
			case INDICES:
			case INNER:
			case INSERT:
			case INSTANT:
			case INTERSECT:
			case INTO:
			case IS:
			case JOIN:
			case KEY:
			case LAST:
			case LEADING:
			case LEFT:
			case LIKE:
			case LIMIT:
			case LIST:
			case LISTAGG:
			case LOCAL:
			case LOCAL_DATE:
			case LOCAL_DATETIME:
			case LOCAL_TIME:
			case MAP:
			case MATERIALIZED:
			case MAX:
			case MAXELEMENT:
			case MAXINDEX:
			case MEMBER:
			case MICROSECOND:
			case MILLISECOND:
			case MIN:
			case MINELEMENT:
			case MININDEX:
			case MINUTE:
			case MONTH:
			case NANOSECOND:
			case NATURALID:
			case NEW:
			case NEXT:
			case NO:
			case NOT:
			case NULLS:
			case OBJECT:
			case OF:
			case OFFSET:
			case OFFSET_DATETIME:
			case ON:
			case ONLY:
			case OR:
			case ORDER:
			case OTHERS:
			case OUTER:
			case OVER:
			case OVERFLOW:
			case OVERLAY:
			case PAD:
			case PARTITION:
			case PERCENT:
			case PLACING:
			case POSITION:
			case POWER:
			case PRECEDING:
			case QUARTER:
			case RANGE:
			case RESPECT:
			case RIGHT:
			case ROLLUP:
			case ROW:
			case ROWS:
			case SEARCH:
			case SECOND:
			case SELECT:
			case SET:
			case SIZE:
			case SOME:
			case SUBSTRING:
			case SUM:
			case THEN:
			case TIES:
			case TIME:
			case TIMESTAMP:
			case TIMEZONE_HOUR:
			case TIMEZONE_MINUTE:
			case TO:
			case TRAILING:
			case TREAT:
			case TRIM:
			case TRUNC:
			case TRUNCATE:
			case TYPE:
			case UNBOUNDED:
			case UNION:
			case UPDATE:
			case USING:
			case VALUE:
			case VALUES:
			case VERSION:
			case VERSIONED:
			case WEEK:
			case WHEN:
			case WHERE:
			case WITH:
			case WITHIN:
			case WITHOUT:
			case YEAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(1322);
				((ReservedWordContext)_localctx).f = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 25)) & ~0x3f) == 0 && ((1L << (_la - 25)) & -2427721674137633L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & -35184374202369L) != 0) || ((((_la - 153)) & ~0x3f) == 0 && ((1L << (_la - 153)) & 140737219919871L) != 0)) ) {
					((ReservedWordContext)_localctx).f = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 63:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 96:
			return predicate_sempred((PredicateContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 6);
		case 1:
			return precpred(_ctx, 5);
		case 2:
			return precpred(_ctx, 4);
		case 3:
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		}
		return true;
	}
	private boolean predicate_sempred(PredicateContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 3);
		case 6:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u00d0\u052e\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0002s\u0007s\u0002t\u0007t\u0002u\u0007u\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00f4"+
		"\b\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0003\u0003\u00f9\b\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00ff\b\u0003"+
		"\n\u0003\f\u0003\u0102\t\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0005\u0004\u0108\b\u0004\n\u0004\f\u0004\u010b\t\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u0110\b\u0005\u0001\u0005\u0003\u0005"+
		"\u0113\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005"+
		"\u0119\b\u0005\u0001\u0005\u0003\u0005\u011c\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0129\b\u0007\n"+
		"\u0007\f\u0007\u012c\t\u0007\u0001\b\u0001\b\u0003\b\u0130\b\b\u0001\b"+
		"\u0003\b\u0133\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\t\u013e\b\t\u0001\t\u0001\t\u0003\t\u0142\b\t"+
		"\u0001\n\u0001\n\u0001\n\u0005\n\u0147\b\n\n\n\f\n\u014a\t\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0151\b\u000b"+
		"\u0001\u000b\u0003\u000b\u0154\b\u000b\u0001\f\u0001\f\u0003\f\u0158\b"+
		"\f\u0001\f\u0003\f\u015b\b\f\u0001\f\u0001\f\u0003\f\u015f\b\f\u0003\f"+
		"\u0161\b\f\u0001\f\u0001\f\u0003\f\u0165\b\f\u0001\f\u0001\f\u0003\f\u0169"+
		"\b\f\u0003\f\u016b\b\f\u0001\f\u0003\f\u016e\b\f\u0003\f\u0170\b\f\u0001"+
		"\r\u0001\r\u0003\r\u0174\b\r\u0001\r\u0003\r\u0177\b\r\u0001\r\u0003\r"+
		"\u017a\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e"+
		"\u0180\b\u000e\n\u000e\f\u000e\u0183\t\u000e\u0001\u000f\u0001\u000f\u0005"+
		"\u000f\u0187\b\u000f\n\u000f\f\u000f\u018a\t\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u018f\b\u0010\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u0193\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0197\b\u0011\u0001"+
		"\u0011\u0003\u0011\u019a\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u01a0\b\u0011\u0003\u0011\u01a2\b\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u01a7\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0003\u0012\u01ab\b\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u01af\b"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01b3\b\u0013\u0001\u0013\u0003"+
		"\u0013\u01b6\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003"+
		"\u0013\u01bc\b\u0013\u0003\u0013\u01be\b\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0003\u0014\u01c3\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u01c9\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0003\u0015\u01ce\b\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u01d2\b"+
		"\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u01d8"+
		"\b\u0017\n\u0017\f\u0017\u01db\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0019\u0001\u0019\u0003\u0019\u01e3\b\u0019\u0001\u0019"+
		"\u0001\u0019\u0003\u0019\u01e7\b\u0019\u0001\u001a\u0001\u001a\u0003\u001a"+
		"\u01eb\b\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a"+
		"\u01f1\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001b"+
		"\u01f7\b\u001b\n\u001b\f\u001b\u01fa\t\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u0202\b\u001c\n"+
		"\u001c\f\u001c\u0205\t\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0005\u001d\u020b\b\u001d\n\u001d\f\u001d\u020e\t\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001f\u0003\u001f\u0219\b\u001f\u0001\u001f\u0001\u001f"+
		"\u0001 \u0001 \u0001 \u0003 \u0220\b \u0001!\u0001!\u0003!\u0224\b!\u0001"+
		"!\u0003!\u0227\b!\u0001\"\u0001\"\u0001\"\u0003\"\u022c\b\"\u0001#\u0001"+
		"#\u0001$\u0001$\u0001$\u0001%\u0001%\u0001%\u0001&\u0001&\u0001&\u0003"+
		"&\u0239\b&\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u0241"+
		"\b\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u0247\b\'\u0001(\u0001(\u0001"+
		")\u0001)\u0003)\u024d\b)\u0001)\u0001)\u0001*\u0001*\u0001*\u0005*\u0254"+
		"\b*\n*\f*\u0257\t*\u0001+\u0001+\u0003+\u025b\b+\u0001,\u0001,\u0001,"+
		"\u0001,\u0003,\u0261\b,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001.\u0001"+
		".\u0001.\u0001.\u0001.\u0001/\u0001/\u0001/\u0001/\u0005/\u0271\b/\n/"+
		"\f/\u0274\t/\u00010\u00030\u0277\b0\u00010\u00030\u027a\b0\u00010\u0003"+
		"0\u027d\b0\u00010\u00030\u0280\b0\u00011\u00011\u00011\u00011\u00031\u0286"+
		"\b1\u00012\u00012\u00012\u00013\u00013\u00013\u00013\u00013\u00013\u0003"+
		"3\u0291\b3\u00014\u00014\u00014\u00014\u00014\u00054\u0298\b4\n4\f4\u029b"+
		"\t4\u00015\u00015\u00015\u00015\u00015\u00055\u02a2\b5\n5\f5\u02a5\t5"+
		"\u00016\u00016\u00016\u00016\u00056\u02ab\b6\n6\f6\u02ae\t6\u00017\u0001"+
		"7\u00037\u02b2\b7\u00017\u00017\u00037\u02b6\b7\u00017\u00017\u00037\u02ba"+
		"\b7\u00037\u02bc\b7\u00018\u00018\u00018\u00018\u00018\u00018\u00038\u02c4"+
		"\b8\u00019\u00019\u0001:\u0001:\u0001;\u0001;\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0003<\u02da\b<\u0001=\u0001=\u0001>\u0001>\u0001>\u0001>\u0001>\u0005"+
		">\u02e3\b>\n>\f>\u02e6\t>\u0001>\u0003>\u02e9\b>\u0001?\u0001?\u0001?"+
		"\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0004?\u02f4\b?\u000b?\f?\u02f5"+
		"\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001"+
		"?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001"+
		"?\u0003?\u030c\b?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001"+
		"?\u0001?\u0001?\u0001?\u0001?\u0001?\u0001?\u0005?\u031c\b?\n?\f?\u031f"+
		"\t?\u0001@\u0001@\u0001@\u0001@\u0001@\u0003@\u0326\b@\u0001A\u0001A\u0003"+
		"A\u032a\bA\u0001B\u0001B\u0003B\u032e\bB\u0001B\u0003B\u0331\bB\u0001"+
		"C\u0001C\u0003C\u0335\bC\u0001D\u0001D\u0001D\u0001D\u0001D\u0003D\u033c"+
		"\bD\u0001E\u0001E\u0005E\u0340\bE\nE\fE\u0343\tE\u0001F\u0001F\u0001F"+
		"\u0001G\u0001G\u0003G\u034a\bG\u0001H\u0001H\u0001H\u0004H\u034f\bH\u000b"+
		"H\fH\u0350\u0001H\u0001H\u0003H\u0355\bH\u0001H\u0001H\u0001I\u0001I\u0004"+
		"I\u035b\bI\u000bI\fI\u035c\u0001I\u0001I\u0003I\u0361\bI\u0001I\u0001"+
		"I\u0001J\u0001J\u0001J\u0001J\u0001J\u0001K\u0001K\u0001K\u0001K\u0001"+
		"K\u0001L\u0001L\u0001L\u0001L\u0003L\u0373\bL\u0001L\u0001L\u0003L\u0377"+
		"\bL\u0001L\u0003L\u037a\bL\u0001L\u0003L\u037d\bL\u0001L\u0003L\u0380"+
		"\bL\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001"+
		"L\u0001L\u0003L\u038d\bL\u0001M\u0003M\u0390\bM\u0001M\u0001M\u0001M\u0005"+
		"M\u0395\bM\nM\fM\u0398\tM\u0001N\u0001N\u0001N\u0001N\u0001N\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001P\u0001P\u0001P\u0003P\u03a8\bP\u0001"+
		"P\u0003P\u03ab\bP\u0001P\u0003P\u03ae\bP\u0001P\u0001P\u0001Q\u0001Q\u0001"+
		"Q\u0001Q\u0001Q\u0005Q\u03b7\bQ\nQ\fQ\u03ba\tQ\u0001R\u0001R\u0001R\u0003"+
		"R\u03bf\bR\u0001R\u0001R\u0001R\u0001R\u0001R\u0001R\u0003R\u03c7\bR\u0003"+
		"R\u03c9\bR\u0001S\u0001S\u0001S\u0001S\u0001S\u0001S\u0001S\u0001S\u0001"+
		"S\u0001S\u0003S\u03d5\bS\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0001T\u0001T\u0003T\u03e1\bT\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0003U\u03ed\bU\u0001V\u0001V\u0001"+
		"V\u0001V\u0001V\u0001V\u0001V\u0001W\u0001W\u0001W\u0001W\u0001W\u0003"+
		"W\u03fb\bW\u0001W\u0003W\u03fe\bW\u0001X\u0001X\u0001X\u0001X\u0001X\u0001"+
		"X\u0001X\u0005X\u0407\bX\nX\fX\u040a\tX\u0001Y\u0001Y\u0001Y\u0001Y\u0001"+
		"Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0003Y\u0418\bY\u0001"+
		"Z\u0001Z\u0001Z\u0003Z\u041d\bZ\u0001Z\u0003Z\u0420\bZ\u0001Z\u0003Z\u0423"+
		"\bZ\u0001Z\u0001Z\u0001Z\u0001[\u0001[\u0001\\\u0001\\\u0001\\\u0001\\"+
		"\u0001\\\u0001\\\u0001\\\u0001\\\u0001\\\u0001\\\u0001\\\u0001\\\u0001"+
		"\\\u0001\\\u0001\\\u0001\\\u0003\\\u043a\b\\\u0001]\u0001]\u0001]\u0001"+
		"]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0001"+
		"]\u0001]\u0001]\u0003]\u044c\b]\u0001^\u0001^\u0001^\u0001^\u0001^\u0001"+
		"^\u0001^\u0003^\u0455\b^\u0001_\u0001_\u0001_\u0001`\u0001`\u0001`\u0001"+
		"`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001"+
		"`\u0001`\u0003`\u0469\b`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0005"+
		"`\u0471\b`\n`\f`\u0474\t`\u0001a\u0001a\u0003a\u0478\ba\u0001b\u0001b"+
		"\u0001b\u0001b\u0001c\u0001c\u0003c\u0480\bc\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0001d\u0001d\u0001d\u0003d\u048a\bd\u0001d\u0001d\u0001d\u0001"+
		"d\u0001d\u0003d\u0491\bd\u0001d\u0001d\u0001d\u0001d\u0003d\u0497\bd\u0001"+
		"e\u0001e\u0003e\u049b\be\u0001e\u0001e\u0003e\u049f\be\u0001e\u0001e\u0003"+
		"e\u04a3\be\u0001e\u0001e\u0001e\u0003e\u04a8\be\u0003e\u04aa\be\u0001"+
		"f\u0001f\u0003f\u04ae\bf\u0001f\u0001f\u0001f\u0001g\u0001g\u0001g\u0001"+
		"g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001g\u0001"+
		"g\u0005g\u04c1\bg\ng\fg\u04c4\tg\u0003g\u04c6\bg\u0001g\u0003g\u04c9\b"+
		"g\u0001h\u0001h\u0001h\u0001h\u0001h\u0001h\u0001h\u0001h\u0003h\u04d3"+
		"\bh\u0001i\u0001i\u0001i\u0003i\u04d8\bi\u0001i\u0001i\u0001i\u0001i\u0003"+
		"i\u04de\bi\u0001i\u0001i\u0001i\u0001i\u0003i\u04e4\bi\u0001j\u0001j\u0001"+
		"j\u0003j\u04e9\bj\u0001k\u0001k\u0001k\u0005k\u04ee\bk\nk\fk\u04f1\tk"+
		"\u0001l\u0001l\u0003l\u04f5\bl\u0001l\u0003l\u04f8\bl\u0001m\u0001m\u0003"+
		"m\u04fc\bm\u0001n\u0001n\u0003n\u0500\bn\u0001o\u0001o\u0001o\u0003o\u0505"+
		"\bo\u0001p\u0001p\u0001p\u0001p\u0001p\u0001p\u0001p\u0001p\u0003p\u050f"+
		"\bp\u0003p\u0511\bp\u0001q\u0001q\u0001q\u0005q\u0516\bq\nq\fq\u0519\t"+
		"q\u0001q\u0003q\u051c\bq\u0001r\u0001r\u0001s\u0001s\u0001t\u0001t\u0001"+
		"t\u0005t\u0525\bt\nt\ft\u0528\tt\u0001u\u0001u\u0003u\u052c\bu\u0001u"+
		"\u0000\u0002~\u00c0v\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfh"+
		"jlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092"+
		"\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa"+
		"\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2"+
		"\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da"+
		"\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u0000\u0017\u0002\u0000"+
		"\"\"66\u0002\u0000\u001d\u001d77\u0002\u0000KKff\u0001\u0000\u00a1\u00a2"+
		"\u0002\u0000KK\u0083\u0083\u0003\u0000RRii\u009f\u009f\u0002\u0000\u008c"+
		"\u008c\u00c4\u00c4\u0002\u0000HH\u00b5\u00b5\u0001\u0000\u00c8\u00ca\u0001"+
		"\u0000\u00cb\u00cd\u0002\u0000++oo\u0003\u0000++oo\u008a\u008a\b\u0000"+
		"33??WW~\u0080\u009c\u009c\u00a4\u00a4\u00c1\u00c1\u00c7\u00c7\u0001\u0000"+
		"\b\t\u0002\u0000\n\n\u001e\u001e\u0003\u0000UU\u009d\u009d\u00a2\u00a2"+
		"\u0003\u0000!!hh\u00b2\u00b2\u0002\u0000\u0019\u0019BB\u0002\u0000::]"+
		"]\u0002\u0000\u001b\u001b\u00a8\u00a8\u0002\u0000\u0004\u0004\u000f\u0015"+
		"\u0002\u0000ZZjj\n\u0000\u0019\u001d\u001f%\'EGGIKMfhmo\u0085\u0087\u00b4"+
		"\u00b6\u00c7\u059b\u0000\u00ec\u0001\u0000\u0000\u0000\u0002\u00f3\u0001"+
		"\u0000\u0000\u0000\u0004\u00f5\u0001\u0000\u0000\u0000\u0006\u00f8\u0001"+
		"\u0000\u0000\u0000\b\u0103\u0001\u0000\u0000\u0000\n\u010c\u0001\u0000"+
		"\u0000\u0000\f\u011d\u0001\u0000\u0000\u0000\u000e\u0125\u0001\u0000\u0000"+
		"\u0000\u0010\u012d\u0001\u0000\u0000\u0000\u0012\u0134\u0001\u0000\u0000"+
		"\u0000\u0014\u0143\u0001\u0000\u0000\u0000\u0016\u0150\u0001\u0000\u0000"+
		"\u0000\u0018\u016f\u0001\u0000\u0000\u0000\u001a\u0171\u0001\u0000\u0000"+
		"\u0000\u001c\u017b\u0001\u0000\u0000\u0000\u001e\u0184\u0001\u0000\u0000"+
		"\u0000 \u018e\u0001\u0000\u0000\u0000\"\u01a1\u0001\u0000\u0000\u0000"+
		"$\u01a3\u0001\u0000\u0000\u0000&\u01bd\u0001\u0000\u0000\u0000(\u01bf"+
		"\u0001\u0000\u0000\u0000*\u01c6\u0001\u0000\u0000\u0000,\u01cf\u0001\u0000"+
		"\u0000\u0000.\u01d3\u0001\u0000\u0000\u00000\u01dc\u0001\u0000\u0000\u0000"+
		"2\u01e0\u0001\u0000\u0000\u00004\u01e8\u0001\u0000\u0000\u00006\u01f2"+
		"\u0001\u0000\u0000\u00008\u01fd\u0001\u0000\u0000\u0000:\u0206\u0001\u0000"+
		"\u0000\u0000<\u0211\u0001\u0000\u0000\u0000>\u0218\u0001\u0000\u0000\u0000"+
		"@\u021f\u0001\u0000\u0000\u0000B\u0221\u0001\u0000\u0000\u0000D\u022b"+
		"\u0001\u0000\u0000\u0000F\u022d\u0001\u0000\u0000\u0000H\u022f\u0001\u0000"+
		"\u0000\u0000J\u0232\u0001\u0000\u0000\u0000L\u0235\u0001\u0000\u0000\u0000"+
		"N\u023a\u0001\u0000\u0000\u0000P\u0248\u0001\u0000\u0000\u0000R\u024a"+
		"\u0001\u0000\u0000\u0000T\u0250\u0001\u0000\u0000\u0000V\u0258\u0001\u0000"+
		"\u0000\u0000X\u0260\u0001\u0000\u0000\u0000Z\u0262\u0001\u0000\u0000\u0000"+
		"\\\u0267\u0001\u0000\u0000\u0000^\u026c\u0001\u0000\u0000\u0000`\u027f"+
		"\u0001\u0000\u0000\u0000b\u0281\u0001\u0000\u0000\u0000d\u0287\u0001\u0000"+
		"\u0000\u0000f\u028a\u0001\u0000\u0000\u0000h\u0292\u0001\u0000\u0000\u0000"+
		"j\u029c\u0001\u0000\u0000\u0000l\u02a6\u0001\u0000\u0000\u0000n\u02bb"+
		"\u0001\u0000\u0000\u0000p\u02c3\u0001\u0000\u0000\u0000r\u02c5\u0001\u0000"+
		"\u0000\u0000t\u02c7\u0001\u0000\u0000\u0000v\u02c9\u0001\u0000\u0000\u0000"+
		"x\u02d9\u0001\u0000\u0000\u0000z\u02db\u0001\u0000\u0000\u0000|\u02e8"+
		"\u0001\u0000\u0000\u0000~\u030b\u0001\u0000\u0000\u0000\u0080\u0325\u0001"+
		"\u0000\u0000\u0000\u0082\u0329\u0001\u0000\u0000\u0000\u0084\u0330\u0001"+
		"\u0000\u0000\u0000\u0086\u0332\u0001\u0000\u0000\u0000\u0088\u0336\u0001"+
		"\u0000\u0000\u0000\u008a\u033d\u0001\u0000\u0000\u0000\u008c\u0344\u0001"+
		"\u0000\u0000\u0000\u008e\u0349\u0001\u0000\u0000\u0000\u0090\u034b\u0001"+
		"\u0000\u0000\u0000\u0092\u0358\u0001\u0000\u0000\u0000\u0094\u0364\u0001"+
		"\u0000\u0000\u0000\u0096\u0369\u0001\u0000\u0000\u0000\u0098\u038c\u0001"+
		"\u0000\u0000\u0000\u009a\u038f\u0001\u0000\u0000\u0000\u009c\u0399\u0001"+
		"\u0000\u0000\u0000\u009e\u039e\u0001\u0000\u0000\u0000\u00a0\u03a4\u0001"+
		"\u0000\u0000\u0000\u00a2\u03b1\u0001\u0000\u0000\u0000\u00a4\u03c8\u0001"+
		"\u0000\u0000\u0000\u00a6\u03d4\u0001\u0000\u0000\u0000\u00a8\u03e0\u0001"+
		"\u0000\u0000\u0000\u00aa\u03ec\u0001\u0000\u0000\u0000\u00ac\u03ee\u0001"+
		"\u0000\u0000\u0000\u00ae\u03f5\u0001\u0000\u0000\u0000\u00b0\u03ff\u0001"+
		"\u0000\u0000\u0000\u00b2\u0417\u0001\u0000\u0000\u0000\u00b4\u0419\u0001"+
		"\u0000\u0000\u0000\u00b6\u0427\u0001\u0000\u0000\u0000\u00b8\u0439\u0001"+
		"\u0000\u0000\u0000\u00ba\u044b\u0001\u0000\u0000\u0000\u00bc\u044d\u0001"+
		"\u0000\u0000\u0000\u00be\u0456\u0001\u0000\u0000\u0000\u00c0\u0468\u0001"+
		"\u0000\u0000\u0000\u00c2\u0477\u0001\u0000\u0000\u0000\u00c4\u0479\u0001"+
		"\u0000\u0000\u0000\u00c6\u047d\u0001\u0000\u0000\u0000\u00c8\u0496\u0001"+
		"\u0000\u0000\u0000\u00ca\u0498\u0001\u0000\u0000\u0000\u00cc\u04ab\u0001"+
		"\u0000\u0000\u0000\u00ce\u04c8\u0001\u0000\u0000\u0000\u00d0\u04d2\u0001"+
		"\u0000\u0000\u0000\u00d2\u04e3\u0001\u0000\u0000\u0000\u00d4\u04e8\u0001"+
		"\u0000\u0000\u0000\u00d6\u04ea\u0001\u0000\u0000\u0000\u00d8\u04f4\u0001"+
		"\u0000\u0000\u0000\u00da\u04fb\u0001\u0000\u0000\u0000\u00dc\u04ff\u0001"+
		"\u0000\u0000\u0000\u00de\u0504\u0001\u0000\u0000\u0000\u00e0\u0510\u0001"+
		"\u0000\u0000\u0000\u00e2\u051b\u0001\u0000\u0000\u0000\u00e4\u051d\u0001"+
		"\u0000\u0000\u0000\u00e6\u051f\u0001\u0000\u0000\u0000\u00e8\u0521\u0001"+
		"\u0000\u0000\u0000\u00ea\u052b\u0001\u0000\u0000\u0000\u00ec\u00ed\u0003"+
		"\u0002\u0001\u0000\u00ed\u00ee\u0005\u0000\u0000\u0001\u00ee\u0001\u0001"+
		"\u0000\u0000\u0000\u00ef\u00f4\u0003\u0004\u0002\u0000\u00f0\u00f4\u0003"+
		"*\u0015\u0000\u00f1\u00f4\u00032\u0019\u0000\u00f2\u00f4\u00034\u001a"+
		"\u0000\u00f3\u00ef\u0001\u0000\u0000\u0000\u00f3\u00f0\u0001\u0000\u0000"+
		"\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f2\u0001\u0000\u0000"+
		"\u0000\u00f4\u0003\u0001\u0000\u0000\u0000\u00f5\u00f6\u0003\u0006\u0003"+
		"\u0000\u00f6\u0005\u0001\u0000\u0000\u0000\u00f7\u00f9\u0003\b\u0004\u0000"+
		"\u00f8\u00f7\u0001\u0000\u0000\u0000\u00f8\u00f9\u0001\u0000\u0000\u0000"+
		"\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u0100\u0003\u0016\u000b\u0000"+
		"\u00fb\u00fc\u0003n7\u0000\u00fc\u00fd\u0003\u0016\u000b\u0000\u00fd\u00ff"+
		"\u0001\u0000\u0000\u0000\u00fe\u00fb\u0001\u0000\u0000\u0000\u00ff\u0102"+
		"\u0001\u0000\u0000\u0000\u0100\u00fe\u0001\u0000\u0000\u0000\u0100\u0101"+
		"\u0001\u0000\u0000\u0000\u0101\u0007\u0001\u0000\u0000\u0000\u0102\u0100"+
		"\u0001\u0000\u0000\u0000\u0103\u0104\u0005\u00c4\u0000\u0000\u0104\u0109"+
		"\u0003\n\u0005\u0000\u0105\u0106\u0005\u0001\u0000\u0000\u0106\u0108\u0003"+
		"\n\u0005\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0108\u010b\u0001\u0000"+
		"\u0000\u0000\u0109\u0107\u0001\u0000\u0000\u0000\u0109\u010a\u0001\u0000"+
		"\u0000\u0000\u010a\t\u0001\u0000\u0000\u0000\u010b\u0109\u0001\u0000\u0000"+
		"\u0000\u010c\u010d\u0003\u00e4r\u0000\u010d\u0112\u0005\u001c\u0000\u0000"+
		"\u010e\u0110\u0005\u0085\u0000\u0000\u010f\u010e\u0001\u0000\u0000\u0000"+
		"\u010f\u0110\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000"+
		"\u0111\u0113\u0005t\u0000\u0000\u0112\u010f\u0001\u0000\u0000\u0000\u0112"+
		"\u0113\u0001\u0000\u0000\u0000\u0113\u0114\u0001\u0000\u0000\u0000\u0114"+
		"\u0115\u0005\u0002\u0000\u0000\u0115\u0116\u0003\u0006\u0003\u0000\u0116"+
		"\u0118\u0005\u0003\u0000\u0000\u0117\u0119\u0003\f\u0006\u0000\u0118\u0117"+
		"\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000\u0000\u0119\u011b"+
		"\u0001\u0000\u0000\u0000\u011a\u011c\u0003\u0012\t\u0000\u011b\u011a\u0001"+
		"\u0000\u0000\u0000\u011b\u011c\u0001\u0000\u0000\u0000\u011c\u000b\u0001"+
		"\u0000\u0000\u0000\u011d\u011e\u0005\u00a3\u0000\u0000\u011e\u011f\u0007"+
		"\u0000\u0000\u0000\u011f\u0120\u0005K\u0000\u0000\u0120\u0121\u0005#\u0000"+
		"\u0000\u0121\u0122\u0003\u000e\u0007\u0000\u0122\u0123\u0005\u00a6\u0000"+
		"\u0000\u0123\u0124\u0003\u00e4r\u0000\u0124\r\u0001\u0000\u0000\u0000"+
		"\u0125\u012a\u0003\u0010\b\u0000\u0126\u0127\u0005\u0001\u0000\u0000\u0127"+
		"\u0129\u0003\u0010\b\u0000\u0128\u0126\u0001\u0000\u0000\u0000\u0129\u012c"+
		"\u0001\u0000\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012a\u012b"+
		"\u0001\u0000\u0000\u0000\u012b\u000f\u0001\u0000\u0000\u0000\u012c\u012a"+
		"\u0001\u0000\u0000\u0000\u012d\u012f\u0003\u00e4r\u0000\u012e\u0130\u0003"+
		"F#\u0000\u012f\u012e\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000\u0000"+
		"\u0000\u0130\u0132\u0001\u0000\u0000\u0000\u0131\u0133\u0003H$\u0000\u0132"+
		"\u0131\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133"+
		"\u0011\u0001\u0000\u0000\u0000\u0134\u0135\u00050\u0000\u0000\u0135\u0136"+
		"\u0003\u0014\n\u0000\u0136\u0137\u0005\u00a6\u0000\u0000\u0137\u013d\u0003"+
		"\u00e4r\u0000\u0138\u0139\u0005\u00b1\u0000\u0000\u0139\u013a\u0003p8"+
		"\u0000\u013a\u013b\u00054\u0000\u0000\u013b\u013c\u0003p8\u0000\u013c"+
		"\u013e\u0001\u0000\u0000\u0000\u013d\u0138\u0001\u0000\u0000\u0000\u013d"+
		"\u013e\u0001\u0000\u0000\u0000\u013e\u0141\u0001\u0000\u0000\u0000\u013f"+
		"\u0140\u0005\u00bc\u0000\u0000\u0140\u0142\u0003\u00e4r\u0000\u0141\u013f"+
		"\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000\u0142\u0013"+
		"\u0001\u0000\u0000\u0000\u0143\u0148\u0003\u00e4r\u0000\u0144\u0145\u0005"+
		"\u0001\u0000\u0000\u0145\u0147\u0003\u00e4r\u0000\u0146\u0144\u0001\u0000"+
		"\u0000\u0000\u0147\u014a\u0001\u0000\u0000\u0000\u0148\u0146\u0001\u0000"+
		"\u0000\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u0015\u0001\u0000"+
		"\u0000\u0000\u014a\u0148\u0001\u0000\u0000\u0000\u014b\u0151\u0003\u0018"+
		"\f\u0000\u014c\u014d\u0005\u0002\u0000\u0000\u014d\u014e\u0003\u0006\u0003"+
		"\u0000\u014e\u014f\u0005\u0003\u0000\u0000\u014f\u0151\u0001\u0000\u0000"+
		"\u0000\u0150\u014b\u0001\u0000\u0000\u0000\u0150\u014c\u0001\u0000\u0000"+
		"\u0000\u0151\u0153\u0001\u0000\u0000\u0000\u0152\u0154\u0003\u001a\r\u0000"+
		"\u0153\u0152\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000"+
		"\u0154\u0017\u0001\u0000\u0000\u0000\u0155\u0157\u0003R)\u0000\u0156\u0158"+
		"\u0003\u001c\u000e\u0000\u0157\u0156\u0001\u0000\u0000\u0000\u0157\u0158"+
		"\u0001\u0000\u0000\u0000\u0158\u015a\u0001\u0000\u0000\u0000\u0159\u015b"+
		"\u0003^/\u0000\u015a\u0159\u0001\u0000\u0000\u0000\u015a\u015b\u0001\u0000"+
		"\u0000\u0000\u015b\u0160\u0001\u0000\u0000\u0000\u015c\u015e\u0003h4\u0000"+
		"\u015d\u015f\u0003l6\u0000\u015e\u015d\u0001\u0000\u0000\u0000\u015e\u015f"+
		"\u0001\u0000\u0000\u0000\u015f\u0161\u0001\u0000\u0000\u0000\u0160\u015c"+
		"\u0001\u0000\u0000\u0000\u0160\u0161\u0001\u0000\u0000\u0000\u0161\u0170"+
		"\u0001\u0000\u0000\u0000\u0162\u0164\u0003\u001c\u000e\u0000\u0163\u0165"+
		"\u0003^/\u0000\u0164\u0163\u0001\u0000\u0000\u0000\u0164\u0165\u0001\u0000"+
		"\u0000\u0000\u0165\u016a\u0001\u0000\u0000\u0000\u0166\u0168\u0003h4\u0000"+
		"\u0167\u0169\u0003l6\u0000\u0168\u0167\u0001\u0000\u0000\u0000\u0168\u0169"+
		"\u0001\u0000\u0000\u0000\u0169\u016b\u0001\u0000\u0000\u0000\u016a\u0166"+
		"\u0001\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000\u016b\u016d"+
		"\u0001\u0000\u0000\u0000\u016c\u016e\u0003R)\u0000\u016d\u016c\u0001\u0000"+
		"\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0001\u0000"+
		"\u0000\u0000\u016f\u0155\u0001\u0000\u0000\u0000\u016f\u0162\u0001\u0000"+
		"\u0000\u0000\u0170\u0019\u0001\u0000\u0000\u0000\u0171\u0173\u0003j5\u0000"+
		"\u0172\u0174\u0003J%\u0000\u0173\u0172\u0001\u0000\u0000\u0000\u0173\u0174"+
		"\u0001\u0000\u0000\u0000\u0174\u0176\u0001\u0000\u0000\u0000\u0175\u0177"+
		"\u0003L&\u0000\u0176\u0175\u0001\u0000\u0000\u0000\u0176\u0177\u0001\u0000"+
		"\u0000\u0000\u0177\u0179\u0001\u0000\u0000\u0000\u0178\u017a\u0003N\'"+
		"\u0000\u0179\u0178\u0001\u0000\u0000\u0000\u0179\u017a\u0001\u0000\u0000"+
		"\u0000\u017a\u001b\u0001\u0000\u0000\u0000\u017b\u017c\u0005Q\u0000\u0000"+
		"\u017c\u0181\u0003\u001e\u000f\u0000\u017d\u017e\u0005\u0001\u0000\u0000"+
		"\u017e\u0180\u0003\u001e\u000f\u0000\u017f\u017d\u0001\u0000\u0000\u0000"+
		"\u0180\u0183\u0001\u0000\u0000\u0000\u0181\u017f\u0001\u0000\u0000\u0000"+
		"\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u001d\u0001\u0000\u0000\u0000"+
		"\u0183\u0181\u0001\u0000\u0000\u0000\u0184\u0188\u0003\"\u0011\u0000\u0185"+
		"\u0187\u0003 \u0010\u0000\u0186\u0185\u0001\u0000\u0000\u0000\u0187\u018a"+
		"\u0001\u0000\u0000\u0000\u0188\u0186\u0001\u0000\u0000\u0000\u0188\u0189"+
		"\u0001\u0000\u0000\u0000\u0189\u001f\u0001\u0000\u0000\u0000\u018a\u0188"+
		"\u0001\u0000\u0000\u0000\u018b\u018f\u0003$\u0012\u0000\u018c\u018f\u0003"+
		"b1\u0000\u018d\u018f\u0003f3\u0000\u018e\u018b\u0001\u0000\u0000\u0000"+
		"\u018e\u018c\u0001\u0000\u0000\u0000\u018e\u018d\u0001\u0000\u0000\u0000"+
		"\u018f!\u0001\u0000\u0000\u0000\u0190\u0192\u0003\u00e2q\u0000\u0191\u0193"+
		"\u0003\u00deo\u0000\u0192\u0191\u0001\u0000\u0000\u0000\u0192\u0193\u0001"+
		"\u0000\u0000\u0000\u0193\u01a2\u0001\u0000\u0000\u0000\u0194\u0196\u0003"+
		"(\u0014\u0000\u0195\u0197\u0003\u00deo\u0000\u0196\u0195\u0001\u0000\u0000"+
		"\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u01a2\u0001\u0000\u0000"+
		"\u0000\u0198\u019a\u0005g\u0000\u0000\u0199\u0198\u0001\u0000\u0000\u0000"+
		"\u0199\u019a\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000\u0000\u0000"+
		"\u019b\u019c\u0005\u0002\u0000\u0000\u019c\u019d\u0003P(\u0000\u019d\u019f"+
		"\u0005\u0003\u0000\u0000\u019e\u01a0\u0003\u00deo\u0000\u019f\u019e\u0001"+
		"\u0000\u0000\u0000\u019f\u01a0\u0001\u0000\u0000\u0000\u01a0\u01a2\u0001"+
		"\u0000\u0000\u0000\u01a1\u0190\u0001\u0000\u0000\u0000\u01a1\u0194\u0001"+
		"\u0000\u0000\u0000\u01a1\u0199\u0001\u0000\u0000\u0000\u01a2#\u0001\u0000"+
		"\u0000\u0000\u01a3\u01a4\u0003`0\u0000\u01a4\u01a6\u0005d\u0000\u0000"+
		"\u01a5\u01a7\u0005I\u0000\u0000\u01a6\u01a5\u0001\u0000\u0000\u0000\u01a6"+
		"\u01a7\u0001\u0000\u0000\u0000\u01a7\u01a8\u0001\u0000\u0000\u0000\u01a8"+
		"\u01aa\u0003&\u0013\u0000\u01a9\u01ab\u0003d2\u0000\u01aa\u01a9\u0001"+
		"\u0000\u0000\u0000\u01aa\u01ab\u0001\u0000\u0000\u0000\u01ab%\u0001\u0000"+
		"\u0000\u0000\u01ac\u01ae\u0003\u0084B\u0000\u01ad\u01af\u0003\u00deo\u0000"+
		"\u01ae\u01ad\u0001\u0000\u0000\u0000\u01ae\u01af\u0001\u0000\u0000\u0000"+
		"\u01af\u01be\u0001\u0000\u0000\u0000\u01b0\u01b2\u0003(\u0014\u0000\u01b1"+
		"\u01b3\u0003\u00deo\u0000\u01b2\u01b1\u0001\u0000\u0000\u0000\u01b2\u01b3"+
		"\u0001\u0000\u0000\u0000\u01b3\u01be\u0001\u0000\u0000\u0000\u01b4\u01b6"+
		"\u0005g\u0000\u0000\u01b5\u01b4\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001"+
		"\u0000\u0000\u0000\u01b6\u01b7\u0001\u0000\u0000\u0000\u01b7\u01b8\u0005"+
		"\u0002\u0000\u0000\u01b8\u01b9\u0003P(\u0000\u01b9\u01bb\u0005\u0003\u0000"+
		"\u0000\u01ba\u01bc\u0003\u00deo\u0000\u01bb\u01ba\u0001\u0000\u0000\u0000"+
		"\u01bb\u01bc\u0001\u0000\u0000\u0000\u01bc\u01be\u0001\u0000\u0000\u0000"+
		"\u01bd\u01ac\u0001\u0000\u0000\u0000\u01bd\u01b0\u0001\u0000\u0000\u0000"+
		"\u01bd\u01b5\u0001\u0000\u0000\u0000\u01be\'\u0001\u0000\u0000\u0000\u01bf"+
		"\u01c0\u0003\u00e8t\u0000\u01c0\u01c2\u0005\u0002\u0000\u0000\u01c1\u01c3"+
		"\u0003\u009aM\u0000\u01c2\u01c1\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001"+
		"\u0000\u0000\u0000\u01c3\u01c4\u0001\u0000\u0000\u0000\u01c4\u01c5\u0005"+
		"\u0003\u0000\u0000\u01c5)\u0001\u0000\u0000\u0000\u01c6\u01c8\u0005\u00bb"+
		"\u0000\u0000\u01c7\u01c9\u0005\u00c0\u0000\u0000\u01c8\u01c7\u0001\u0000"+
		"\u0000\u0000\u01c8\u01c9\u0001\u0000\u0000\u0000\u01c9\u01ca\u0001\u0000"+
		"\u0000\u0000\u01ca\u01cb\u0003,\u0016\u0000\u01cb\u01cd\u0003.\u0017\u0000"+
		"\u01cc\u01ce\u0003^/\u0000\u01cd\u01cc\u0001\u0000\u0000\u0000\u01cd\u01ce"+
		"\u0001\u0000\u0000\u0000\u01ce+\u0001\u0000\u0000\u0000\u01cf\u01d1\u0003"+
		"\u00e2q\u0000\u01d0\u01d2\u0003\u00deo\u0000\u01d1\u01d0\u0001\u0000\u0000"+
		"\u0000\u01d1\u01d2\u0001\u0000\u0000\u0000\u01d2-\u0001\u0000\u0000\u0000"+
		"\u01d3\u01d4\u0005\u00a6\u0000\u0000\u01d4\u01d9\u00030\u0018\u0000\u01d5"+
		"\u01d6\u0005\u0001\u0000\u0000\u01d6\u01d8\u00030\u0018\u0000\u01d7\u01d5"+
		"\u0001\u0000\u0000\u0000\u01d8\u01db\u0001\u0000\u0000\u0000\u01d9\u01d7"+
		"\u0001\u0000\u0000\u0000\u01d9\u01da\u0001\u0000\u0000\u0000\u01da/\u0001"+
		"\u0000\u0000\u0000\u01db\u01d9\u0001\u0000\u0000\u0000\u01dc\u01dd\u0003"+
		"\u008aE\u0000\u01dd\u01de\u0005\u0004\u0000\u0000\u01de\u01df\u0003\u00c2"+
		"a\u0000\u01df1\u0001\u0000\u0000\u0000\u01e0\u01e2\u00055\u0000\u0000"+
		"\u01e1\u01e3\u0005Q\u0000\u0000\u01e2\u01e1\u0001\u0000\u0000\u0000\u01e2"+
		"\u01e3\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4"+
		"\u01e6\u0003,\u0016\u0000\u01e5\u01e7\u0003^/\u0000\u01e6\u01e5\u0001"+
		"\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000\u0000\u0000\u01e73\u0001\u0000"+
		"\u0000\u0000\u01e8\u01ea\u0005_\u0000\u0000\u01e9\u01eb\u0005b\u0000\u0000"+
		"\u01ea\u01e9\u0001\u0000\u0000\u0000\u01ea\u01eb\u0001\u0000\u0000\u0000"+
		"\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ed\u0003,\u0016\u0000\u01ed"+
		"\u01f0\u00036\u001b\u0000\u01ee\u01f1\u0003\u0006\u0003\u0000\u01ef\u01f1"+
		"\u00038\u001c\u0000\u01f0\u01ee\u0001\u0000\u0000\u0000\u01f0\u01ef\u0001"+
		"\u0000\u0000\u0000\u01f15\u0001\u0000\u0000\u0000\u01f2\u01f3\u0005\u0002"+
		"\u0000\u0000\u01f3\u01f8\u0003\u008aE\u0000\u01f4\u01f5\u0005\u0001\u0000"+
		"\u0000\u01f5\u01f7\u0003\u008aE\u0000\u01f6\u01f4\u0001\u0000\u0000\u0000"+
		"\u01f7\u01fa\u0001\u0000\u0000\u0000\u01f8\u01f6\u0001\u0000\u0000\u0000"+
		"\u01f8\u01f9\u0001\u0000\u0000\u0000\u01f9\u01fb\u0001\u0000\u0000\u0000"+
		"\u01fa\u01f8\u0001\u0000\u0000\u0000\u01fb\u01fc\u0005\u0003\u0000\u0000"+
		"\u01fc7\u0001\u0000\u0000\u0000\u01fd\u01fe\u0005\u00be\u0000\u0000\u01fe"+
		"\u0203\u0003:\u001d\u0000\u01ff\u0200\u0005\u0001\u0000\u0000\u0200\u0202"+
		"\u0003:\u001d\u0000\u0201\u01ff\u0001\u0000\u0000\u0000\u0202\u0205\u0001"+
		"\u0000\u0000\u0000\u0203\u0201\u0001\u0000\u0000\u0000\u0203\u0204\u0001"+
		"\u0000\u0000\u0000\u02049\u0001\u0000\u0000\u0000\u0205\u0203\u0001\u0000"+
		"\u0000\u0000\u0206\u0207\u0005\u0002\u0000\u0000\u0207\u020c\u0003~?\u0000"+
		"\u0208\u0209\u0005\u0001\u0000\u0000\u0209\u020b\u0003~?\u0000\u020a\u0208"+
		"\u0001\u0000\u0000\u0000\u020b\u020e\u0001\u0000\u0000\u0000\u020c\u020a"+
		"\u0001\u0000\u0000\u0000\u020c\u020d\u0001\u0000\u0000\u0000\u020d\u020f"+
		"\u0001\u0000\u0000\u0000\u020e\u020c\u0001\u0000\u0000\u0000\u020f\u0210"+
		"\u0005\u0003\u0000\u0000\u0210;\u0001\u0000\u0000\u0000\u0211\u0212\u0005"+
		"\u0082\u0000\u0000\u0212\u0213\u0003\u00d4j\u0000\u0213\u0214\u0005\u0002"+
		"\u0000\u0000\u0214\u0215\u0003\u00d6k\u0000\u0215\u0216\u0005\u0003\u0000"+
		"\u0000\u0216=\u0001\u0000\u0000\u0000\u0217\u0219\u0005\u001c\u0000\u0000"+
		"\u0218\u0217\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000\u0000\u0000"+
		"\u0219\u021a\u0001\u0000\u0000\u0000\u021a\u021b\u0003\u00e4r\u0000\u021b"+
		"?\u0001\u0000\u0000\u0000\u021c\u0220\u0003\u00e4r\u0000\u021d\u0220\u0005"+
		"\u00cb\u0000\u0000\u021e\u0220\u0003~?\u0000\u021f\u021c\u0001\u0000\u0000"+
		"\u0000\u021f\u021d\u0001\u0000\u0000\u0000\u021f\u021e\u0001\u0000\u0000"+
		"\u0000\u0220A\u0001\u0000\u0000\u0000\u0221\u0223\u0003D\"\u0000\u0222"+
		"\u0224\u0003F#\u0000\u0223\u0222\u0001\u0000\u0000\u0000\u0223\u0224\u0001"+
		"\u0000\u0000\u0000\u0224\u0226\u0001\u0000\u0000\u0000\u0225\u0227\u0003"+
		"H$\u0000\u0226\u0225\u0001\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000"+
		"\u0000\u0227C\u0001\u0000\u0000\u0000\u0228\u022c\u0003\u00e4r\u0000\u0229"+
		"\u022c\u0005\u00cb\u0000\u0000\u022a\u022c\u0003~?\u0000\u022b\u0228\u0001"+
		"\u0000\u0000\u0000\u022b\u0229\u0001\u0000\u0000\u0000\u022b\u022a\u0001"+
		"\u0000\u0000\u0000\u022cE\u0001\u0000\u0000\u0000\u022d\u022e\u0007\u0001"+
		"\u0000\u0000\u022eG\u0001\u0000\u0000\u0000\u022f\u0230\u0005\u0087\u0000"+
		"\u0000\u0230\u0231\u0007\u0002\u0000\u0000\u0231I\u0001\u0000\u0000\u0000"+
		"\u0232\u0233\u0005k\u0000\u0000\u0233\u0234\u0003\u00dam\u0000\u0234K"+
		"\u0001\u0000\u0000\u0000\u0235\u0236\u0005\u008a\u0000\u0000\u0236\u0238"+
		"\u0003\u00dam\u0000\u0237\u0239\u0007\u0003\u0000\u0000\u0238\u0237\u0001"+
		"\u0000\u0000\u0000\u0238\u0239\u0001\u0000\u0000\u0000\u0239M\u0001\u0000"+
		"\u0000\u0000\u023a\u023b\u0005I\u0000\u0000\u023b\u0240\u0007\u0004\u0000"+
		"\u0000\u023c\u0241\u0003\u00dam\u0000\u023d\u023e\u0003\u00dcn\u0000\u023e"+
		"\u023f\u0005\u0005\u0000\u0000\u023f\u0241\u0001\u0000\u0000\u0000\u0240"+
		"\u023c\u0001\u0000\u0000\u0000\u0240\u023d\u0001\u0000\u0000\u0000\u0241"+
		"\u0242\u0001\u0000\u0000\u0000\u0242\u0246\u0007\u0003\u0000\u0000\u0243"+
		"\u0247\u0005\u008d\u0000\u0000\u0244\u0245\u0005\u00c4\u0000\u0000\u0245"+
		"\u0247\u0005\u00ac\u0000\u0000\u0246\u0243\u0001\u0000\u0000\u0000\u0246"+
		"\u0244\u0001\u0000\u0000\u0000\u0247O\u0001\u0000\u0000\u0000\u0248\u0249"+
		"\u0003\u0006\u0003\u0000\u0249Q\u0001\u0000\u0000\u0000\u024a\u024c\u0005"+
		"\u00a5\u0000\u0000\u024b\u024d\u00058\u0000\u0000\u024c\u024b\u0001\u0000"+
		"\u0000\u0000\u024c\u024d\u0001\u0000\u0000\u0000\u024d\u024e\u0001\u0000"+
		"\u0000\u0000\u024e\u024f\u0003T*\u0000\u024fS\u0001\u0000\u0000\u0000"+
		"\u0250\u0255\u0003V+\u0000\u0251\u0252\u0005\u0001\u0000\u0000\u0252\u0254"+
		"\u0003V+\u0000\u0253\u0251\u0001\u0000\u0000\u0000\u0254\u0257\u0001\u0000"+
		"\u0000\u0000\u0255\u0253\u0001\u0000\u0000\u0000\u0255\u0256\u0001\u0000"+
		"\u0000\u0000\u0256U\u0001\u0000\u0000\u0000\u0257\u0255\u0001\u0000\u0000"+
		"\u0000\u0258\u025a\u0003X,\u0000\u0259\u025b\u0003\u00deo\u0000\u025a"+
		"\u0259\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000\u0000\u0000\u025b"+
		"W\u0001\u0000\u0000\u0000\u025c\u0261\u0003<\u001e\u0000\u025d\u0261\u0003"+
		"Z-\u0000\u025e\u0261\u0003\\.\u0000\u025f\u0261\u0003\u00c2a\u0000\u0260"+
		"\u025c\u0001\u0000\u0000\u0000\u0260\u025d\u0001\u0000\u0000\u0000\u0260"+
		"\u025e\u0001\u0000\u0000\u0000\u0260\u025f\u0001\u0000\u0000\u0000\u0261"+
		"Y\u0001\u0000\u0000\u0000\u0262\u0263\u0005>\u0000\u0000\u0263\u0264\u0005"+
		"\u0002\u0000\u0000\u0264\u0265\u0003\u0084B\u0000\u0265\u0266\u0005\u0003"+
		"\u0000\u0000\u0266[\u0001\u0000\u0000\u0000\u0267\u0268\u0005\u0088\u0000"+
		"\u0000\u0268\u0269\u0005\u0002\u0000\u0000\u0269\u026a\u0003\u00e4r\u0000"+
		"\u026a\u026b\u0005\u0003\u0000\u0000\u026b]\u0001\u0000\u0000\u0000\u026c"+
		"\u026d\u0005\u00c3\u0000\u0000\u026d\u0272\u0003\u00c0`\u0000\u026e\u026f"+
		"\u0005\u0001\u0000\u0000\u026f\u0271\u0003\u00c0`\u0000\u0270\u026e\u0001"+
		"\u0000\u0000\u0000\u0271\u0274\u0001\u0000\u0000\u0000\u0272\u0270\u0001"+
		"\u0000\u0000\u0000\u0272\u0273\u0001\u0000\u0000\u0000\u0273_\u0001\u0000"+
		"\u0000\u0000\u0274\u0272\u0001\u0000\u0000\u0000\u0275\u0277\u0005^\u0000"+
		"\u0000\u0276\u0275\u0001\u0000\u0000\u0000\u0276\u0277\u0001\u0000\u0000"+
		"\u0000\u0277\u0280\u0001\u0000\u0000\u0000\u0278\u027a\u0007\u0005\u0000"+
		"\u0000\u0279\u0278\u0001\u0000\u0000\u0000\u0279\u027a\u0001\u0000\u0000"+
		"\u0000\u027a\u027c\u0001\u0000\u0000\u0000\u027b\u027d\u0005\u0091\u0000"+
		"\u0000\u027c\u027b\u0001\u0000\u0000\u0000\u027c\u027d\u0001\u0000\u0000"+
		"\u0000\u027d\u0280\u0001\u0000\u0000\u0000\u027e\u0280\u0005)\u0000\u0000"+
		"\u027f\u0276\u0001\u0000\u0000\u0000\u027f\u0279\u0001\u0000\u0000\u0000"+
		"\u027f\u027e\u0001\u0000\u0000\u0000\u0280a\u0001\u0000\u0000\u0000\u0281"+
		"\u0282\u0005)\u0000\u0000\u0282\u0283\u0005d\u0000\u0000\u0283\u0285\u0003"+
		"\u00e2q\u0000\u0284\u0286\u0003\u00deo\u0000\u0285\u0284\u0001\u0000\u0000"+
		"\u0000\u0285\u0286\u0001\u0000\u0000\u0000\u0286c\u0001\u0000\u0000\u0000"+
		"\u0287\u0288\u0007\u0006\u0000\u0000\u0288\u0289\u0003\u00c0`\u0000\u0289"+
		"e\u0001\u0000\u0000\u0000\u028a\u028b\u0005\u0001\u0000\u0000\u028b\u028c"+
		"\u0005[\u0000\u0000\u028c\u028d\u0005\u0002\u0000\u0000\u028d\u028e\u0003"+
		"\u0084B\u0000\u028e\u0290\u0005\u0003\u0000\u0000\u028f\u0291\u0003\u00de"+
		"o\u0000\u0290\u028f\u0001\u0000\u0000\u0000\u0290\u0291\u0001\u0000\u0000"+
		"\u0000\u0291g\u0001\u0000\u0000\u0000\u0292\u0293\u0005T\u0000\u0000\u0293"+
		"\u0294\u0005#\u0000\u0000\u0294\u0299\u0003@ \u0000\u0295\u0296\u0005"+
		"\u0001\u0000\u0000\u0296\u0298\u0003@ \u0000\u0297\u0295\u0001\u0000\u0000"+
		"\u0000\u0298\u029b\u0001\u0000\u0000\u0000\u0299\u0297\u0001\u0000\u0000"+
		"\u0000\u0299\u029a\u0001\u0000\u0000\u0000\u029ai\u0001\u0000\u0000\u0000"+
		"\u029b\u0299\u0001\u0000\u0000\u0000\u029c\u029d\u0005\u008f\u0000\u0000"+
		"\u029d\u029e\u0005#\u0000\u0000\u029e\u02a3\u0003B!\u0000\u029f\u02a0"+
		"\u0005\u0001\u0000\u0000\u02a0\u02a2\u0003B!\u0000\u02a1\u029f\u0001\u0000"+
		"\u0000\u0000\u02a2\u02a5\u0001\u0000\u0000\u0000\u02a3\u02a1\u0001\u0000"+
		"\u0000\u0000\u02a3\u02a4\u0001\u0000\u0000\u0000\u02a4k\u0001\u0000\u0000"+
		"\u0000\u02a5\u02a3\u0001\u0000\u0000\u0000\u02a6\u02a7\u0005V\u0000\u0000"+
		"\u02a7\u02ac\u0003\u00c0`\u0000\u02a8\u02a9\u0005\u0001\u0000\u0000\u02a9"+
		"\u02ab\u0003\u00c0`\u0000\u02aa\u02a8\u0001\u0000\u0000\u0000\u02ab\u02ae"+
		"\u0001\u0000\u0000\u0000\u02ac\u02aa\u0001\u0000\u0000\u0000\u02ac\u02ad"+
		"\u0001\u0000\u0000\u0000\u02adm\u0001\u0000\u0000\u0000\u02ae\u02ac\u0001"+
		"\u0000\u0000\u0000\u02af\u02b1\u0005\u00ba\u0000\u0000\u02b0\u02b2\u0005"+
		"\u0019\u0000\u0000\u02b1\u02b0\u0001\u0000\u0000\u0000\u02b1\u02b2\u0001"+
		"\u0000\u0000\u0000\u02b2\u02bc\u0001\u0000\u0000\u0000\u02b3\u02b5\u0005"+
		"a\u0000\u0000\u02b4\u02b6\u0005\u0019\u0000\u0000\u02b5\u02b4\u0001\u0000"+
		"\u0000\u0000\u02b5\u02b6\u0001\u0000\u0000\u0000\u02b6\u02bc\u0001\u0000"+
		"\u0000\u0000\u02b7\u02b9\u0005C\u0000\u0000\u02b8\u02ba\u0005\u0019\u0000"+
		"\u0000\u02b9\u02b8\u0001\u0000\u0000\u0000\u02b9\u02ba\u0001\u0000\u0000"+
		"\u0000\u02ba\u02bc\u0001\u0000\u0000\u0000\u02bb\u02af\u0001\u0000\u0000"+
		"\u0000\u02bb\u02b3\u0001\u0000\u0000\u0000\u02bb\u02b7\u0001\u0000\u0000"+
		"\u0000\u02bco\u0001\u0000\u0000\u0000\u02bd\u02c4\u0005\u0086\u0000\u0000"+
		"\u02be\u02c4\u0003r9\u0000\u02bf\u02c4\u0003t:\u0000\u02c0\u02c4\u0003"+
		"v;\u0000\u02c1\u02c4\u0003x<\u0000\u02c2\u02c4\u0003|>\u0000\u02c3\u02bd"+
		"\u0001\u0000\u0000\u0000\u02c3\u02be\u0001\u0000\u0000\u0000\u02c3\u02bf"+
		"\u0001\u0000\u0000\u0000\u02c3\u02c0\u0001\u0000\u0000\u0000\u02c3\u02c1"+
		"\u0001\u0000\u0000\u0000\u02c3\u02c2\u0001\u0000\u0000\u0000\u02c4q\u0001"+
		"\u0000\u0000\u0000\u02c5\u02c6\u0007\u0007\u0000\u0000\u02c6s\u0001\u0000"+
		"\u0000\u0000\u02c7\u02c8\u0007\b\u0000\u0000\u02c8u\u0001\u0000\u0000"+
		"\u0000\u02c9\u02ca\u0007\t\u0000\u0000\u02caw\u0001\u0000\u0000\u0000"+
		"\u02cb\u02da\u0005p\u0000\u0000\u02cc\u02da\u0005r\u0000\u0000\u02cd\u02da"+
		"\u0005q\u0000\u0000\u02ce\u02da\u0005,\u0000\u0000\u02cf\u02da\u0005."+
		"\u0000\u0000\u02d0\u02da\u0005/\u0000\u0000\u02d1\u02da\u0005\u008b\u0000"+
		"\u0000\u02d2\u02d3\u0007\n\u0000\u0000\u02d3\u02da\u00051\u0000\u0000"+
		"\u02d4\u02d5\u0007\n\u0000\u0000\u02d5\u02da\u0005\u00ad\u0000\u0000\u02d6"+
		"\u02d7\u0007\u000b\u0000\u0000\u02d7\u02da\u00052\u0000\u0000\u02d8\u02da"+
		"\u0005`\u0000\u0000\u02d9\u02cb\u0001\u0000\u0000\u0000\u02d9\u02cc\u0001"+
		"\u0000\u0000\u0000\u02d9\u02cd\u0001\u0000\u0000\u0000\u02d9\u02ce\u0001"+
		"\u0000\u0000\u0000\u02d9\u02cf\u0001\u0000\u0000\u0000\u02d9\u02d0\u0001"+
		"\u0000\u0000\u0000\u02d9\u02d1\u0001\u0000\u0000\u0000\u02d9\u02d2\u0001"+
		"\u0000\u0000\u0000\u02d9\u02d4\u0001\u0000\u0000\u0000\u02d9\u02d6\u0001"+
		"\u0000\u0000\u0000\u02d9\u02d8\u0001\u0000\u0000\u0000\u02day\u0001\u0000"+
		"\u0000\u0000\u02db\u02dc\u0007\f\u0000\u0000\u02dc{\u0001\u0000\u0000"+
		"\u0000\u02dd\u02e9\u0005\u00ce\u0000\u0000\u02de\u02df\u0005\u0006\u0000"+
		"\u0000\u02df\u02e4\u0005\u00cd\u0000\u0000\u02e0\u02e1\u0005\u0001\u0000"+
		"\u0000\u02e1\u02e3\u0005\u00cd\u0000\u0000\u02e2\u02e0\u0001\u0000\u0000"+
		"\u0000\u02e3\u02e6\u0001\u0000\u0000\u0000\u02e4\u02e2\u0001\u0000\u0000"+
		"\u0000\u02e4\u02e5\u0001\u0000\u0000\u0000\u02e5\u02e7\u0001\u0000\u0000"+
		"\u0000\u02e6\u02e4\u0001\u0000\u0000\u0000\u02e7\u02e9\u0005\u0007\u0000"+
		"\u0000\u02e8\u02dd\u0001\u0000\u0000\u0000\u02e8\u02de\u0001\u0000\u0000"+
		"\u0000\u02e9}\u0001\u0000\u0000\u0000\u02ea\u02eb\u0006?\uffff\uffff\u0000"+
		"\u02eb\u02ec\u0005\u0002\u0000\u0000\u02ec\u02ed\u0003~?\u0000\u02ed\u02ee"+
		"\u0005\u0003\u0000\u0000\u02ee\u030c\u0001\u0000\u0000\u0000\u02ef\u02f0"+
		"\u0005\u0002\u0000\u0000\u02f0\u02f3\u0003\u00c2a\u0000\u02f1\u02f2\u0005"+
		"\u0001\u0000\u0000\u02f2\u02f4\u0003\u00c2a\u0000\u02f3\u02f1\u0001\u0000"+
		"\u0000\u0000\u02f4\u02f5\u0001\u0000\u0000\u0000\u02f5\u02f3\u0001\u0000"+
		"\u0000\u0000\u02f5\u02f6\u0001\u0000\u0000\u0000\u02f6\u02f7\u0001\u0000"+
		"\u0000\u0000\u02f7\u02f8\u0005\u0003\u0000\u0000\u02f8\u030c\u0001\u0000"+
		"\u0000\u0000\u02f9\u02fa\u0005\u0002\u0000\u0000\u02fa\u02fb\u0003P(\u0000"+
		"\u02fb\u02fc\u0005\u0003\u0000\u0000\u02fc\u030c\u0001\u0000\u0000\u0000"+
		"\u02fd\u030c\u0003\u0080@\u0000\u02fe\u02ff\u0007\r\u0000\u0000\u02ff"+
		"\u030c\u0003v;\u0000\u0300\u0301\u0007\r\u0000\u0000\u0301\u030c\u0003"+
		"~?\t\u0302\u0303\u00053\u0000\u0000\u0303\u0304\u0005\u0089\u0000\u0000"+
		"\u0304\u030c\u0005\u00c1\u0000\u0000\u0305\u0306\u00053\u0000\u0000\u0306"+
		"\u0307\u0005\u0089\u0000\u0000\u0307\u030c\u0005\u007f\u0000\u0000\u0308"+
		"\u0309\u0005\u00c1\u0000\u0000\u0309\u030a\u0005\u0089\u0000\u0000\u030a"+
		"\u030c\u0005\u00c7\u0000\u0000\u030b\u02ea\u0001\u0000\u0000\u0000\u030b"+
		"\u02ef\u0001\u0000\u0000\u0000\u030b\u02f9\u0001\u0000\u0000\u0000\u030b"+
		"\u02fd\u0001\u0000\u0000\u0000\u030b\u02fe\u0001\u0000\u0000\u0000\u030b"+
		"\u0300\u0001\u0000\u0000\u0000\u030b\u0302\u0001\u0000\u0000\u0000\u030b"+
		"\u0305\u0001\u0000\u0000\u0000\u030b\u0308\u0001\u0000\u0000\u0000\u030c"+
		"\u031d\u0001\u0000\u0000\u0000\u030d\u030e\n\u0006\u0000\u0000\u030e\u030f"+
		"\u0007\u000e\u0000\u0000\u030f\u031c\u0003~?\u0007\u0310\u0311\n\u0005"+
		"\u0000\u0000\u0311\u0312\u0007\r\u0000\u0000\u0312\u031c\u0003~?\u0006"+
		"\u0313\u0314\n\u0004\u0000\u0000\u0314\u0315\u0005\u000b\u0000\u0000\u0315"+
		"\u031c\u0003~?\u0005\u0316\u0317\n\b\u0000\u0000\u0317\u031c\u0003z=\u0000"+
		"\u0318\u0319\n\u0007\u0000\u0000\u0319\u031a\u0005#\u0000\u0000\u031a"+
		"\u031c\u0003z=\u0000\u031b\u030d\u0001\u0000\u0000\u0000\u031b\u0310\u0001"+
		"\u0000\u0000\u0000\u031b\u0313\u0001\u0000\u0000\u0000\u031b\u0316\u0001"+
		"\u0000\u0000\u0000\u031b\u0318\u0001\u0000\u0000\u0000\u031c\u031f\u0001"+
		"\u0000\u0000\u0000\u031d\u031b\u0001\u0000\u0000\u0000\u031d\u031e\u0001"+
		"\u0000\u0000\u0000\u031e\u007f\u0001\u0000\u0000\u0000\u031f\u031d\u0001"+
		"\u0000\u0000\u0000\u0320\u0326\u0003\u008eG\u0000\u0321\u0326\u0003p8"+
		"\u0000\u0322\u0326\u0003\u00e0p\u0000\u0323\u0326\u0003\u0098L\u0000\u0324"+
		"\u0326\u0003\u0086C\u0000\u0325\u0320\u0001\u0000\u0000\u0000\u0325\u0321"+
		"\u0001\u0000\u0000\u0000\u0325\u0322\u0001\u0000\u0000\u0000\u0325\u0323"+
		"\u0001\u0000\u0000\u0000\u0325\u0324\u0001\u0000\u0000\u0000\u0326\u0081"+
		"\u0001\u0000\u0000\u0000\u0327\u032a\u0003\u00e4r\u0000\u0328\u032a\u0003"+
		"\u008aE\u0000\u0329\u0327\u0001\u0000\u0000\u0000\u0329\u0328\u0001\u0000"+
		"\u0000\u0000\u032a\u0083\u0001\u0000\u0000\u0000\u032b\u032d\u0003\u00bc"+
		"^\u0000\u032c\u032e\u0003\u00be_\u0000\u032d\u032c\u0001\u0000\u0000\u0000"+
		"\u032d\u032e\u0001\u0000\u0000\u0000\u032e\u0331\u0001\u0000\u0000\u0000"+
		"\u032f\u0331\u0003\u0086C\u0000\u0330\u032b\u0001\u0000\u0000\u0000\u0330"+
		"\u032f\u0001\u0000\u0000\u0000\u0331\u0085\u0001\u0000\u0000\u0000\u0332"+
		"\u0334\u0003\u008aE\u0000\u0333\u0335\u0003\u0088D\u0000\u0334\u0333\u0001"+
		"\u0000\u0000\u0000\u0334\u0335\u0001\u0000\u0000\u0000\u0335\u0087\u0001"+
		"\u0000\u0000\u0000\u0336\u0337\u0005\f\u0000\u0000\u0337\u0338\u0003~"+
		"?\u0000\u0338\u033b\u0005\r\u0000\u0000\u0339\u033a\u0005\u000e\u0000"+
		"\u0000\u033a\u033c\u0003\u0086C\u0000\u033b\u0339\u0001\u0000\u0000\u0000"+
		"\u033b\u033c\u0001\u0000\u0000\u0000\u033c\u0089\u0001\u0000\u0000\u0000"+
		"\u033d\u0341\u0003\u00e4r\u0000\u033e\u0340\u0003\u008cF\u0000\u033f\u033e"+
		"\u0001\u0000\u0000\u0000\u0340\u0343\u0001\u0000\u0000\u0000\u0341\u033f"+
		"\u0001\u0000\u0000\u0000\u0341\u0342\u0001\u0000\u0000\u0000\u0342\u008b"+
		"\u0001\u0000\u0000\u0000\u0343\u0341\u0001\u0000\u0000\u0000\u0344\u0345"+
		"\u0005\u000e\u0000\u0000\u0345\u0346\u0003\u00e4r\u0000\u0346\u008d\u0001"+
		"\u0000\u0000\u0000\u0347\u034a\u0003\u0090H\u0000\u0348\u034a\u0003\u0092"+
		"I\u0000\u0349\u0347\u0001\u0000\u0000\u0000\u0349\u0348\u0001\u0000\u0000"+
		"\u0000\u034a\u008f\u0001\u0000\u0000\u0000\u034b\u034c\u0005$\u0000\u0000"+
		"\u034c\u034e\u0003\u00c2a\u0000\u034d\u034f\u0003\u0094J\u0000\u034e\u034d"+
		"\u0001\u0000\u0000\u0000\u034f\u0350\u0001\u0000\u0000\u0000\u0350\u034e"+
		"\u0001\u0000\u0000\u0000\u0350\u0351\u0001\u0000\u0000\u0000\u0351\u0354"+
		"\u0001\u0000\u0000\u0000\u0352\u0353\u0005;\u0000\u0000\u0353\u0355\u0003"+
		"\u00c2a\u0000\u0354\u0352\u0001\u0000\u0000\u0000\u0354\u0355\u0001\u0000"+
		"\u0000\u0000\u0355\u0356\u0001\u0000\u0000\u0000\u0356\u0357\u0005=\u0000"+
		"\u0000\u0357\u0091\u0001\u0000\u0000\u0000\u0358\u035a\u0005$\u0000\u0000"+
		"\u0359\u035b\u0003\u0096K\u0000\u035a\u0359\u0001\u0000\u0000\u0000\u035b"+
		"\u035c\u0001\u0000\u0000\u0000\u035c\u035a\u0001\u0000\u0000\u0000\u035c"+
		"\u035d\u0001\u0000\u0000\u0000\u035d\u0360\u0001\u0000\u0000\u0000\u035e"+
		"\u035f\u0005;\u0000\u0000\u035f\u0361\u0003\u00c2a\u0000\u0360\u035e\u0001"+
		"\u0000\u0000\u0000\u0360\u0361\u0001\u0000\u0000\u0000\u0361\u0362\u0001"+
		"\u0000\u0000\u0000\u0362\u0363\u0005=\u0000\u0000\u0363\u0093\u0001\u0000"+
		"\u0000\u0000\u0364\u0365\u0005\u00c2\u0000\u0000\u0365\u0366\u0003~?\u0000"+
		"\u0366\u0367\u0005\u00ab\u0000\u0000\u0367\u0368\u0003\u00c2a\u0000\u0368"+
		"\u0095\u0001\u0000\u0000\u0000\u0369\u036a\u0005\u00c2\u0000\u0000\u036a"+
		"\u036b\u0003\u00c0`\u0000\u036b\u036c\u0005\u00ab\u0000\u0000\u036c\u036d"+
		"\u0003\u00c2a\u0000\u036d\u0097\u0001\u0000\u0000\u0000\u036e\u036f\u0003"+
		"\u00e8t\u0000\u036f\u0372\u0005\u0002\u0000\u0000\u0370\u0373\u0003\u009a"+
		"M\u0000\u0371\u0373\u0005\u001e\u0000\u0000\u0372\u0370\u0001\u0000\u0000"+
		"\u0000\u0372\u0371\u0001\u0000\u0000\u0000\u0372\u0373\u0001\u0000\u0000"+
		"\u0000\u0373\u0374\u0001\u0000\u0000\u0000\u0374\u0376\u0005\u0003\u0000"+
		"\u0000\u0375\u0377\u0003\u00be_\u0000\u0376\u0375\u0001\u0000\u0000\u0000"+
		"\u0376\u0377\u0001\u0000\u0000\u0000\u0377\u0379\u0001\u0000\u0000\u0000"+
		"\u0378\u037a\u0003\u009cN\u0000\u0379\u0378\u0001\u0000\u0000\u0000\u0379"+
		"\u037a\u0001\u0000\u0000\u0000\u037a\u037c\u0001\u0000\u0000\u0000\u037b"+
		"\u037d\u0003\u009eO\u0000\u037c\u037b\u0001\u0000\u0000\u0000\u037c\u037d"+
		"\u0001\u0000\u0000\u0000\u037d\u037f\u0001\u0000\u0000\u0000\u037e\u0380"+
		"\u0003\u00a0P\u0000\u037f\u037e\u0001\u0000\u0000\u0000\u037f\u0380\u0001"+
		"\u0000\u0000\u0000\u0380\u038d\u0001\u0000\u0000\u0000\u0381\u0382\u0003"+
		"\u00e8t\u0000\u0382\u0383\u0005\u0002\u0000\u0000\u0383\u0384\u0003P("+
		"\u0000\u0384\u0385\u0005\u0003\u0000\u0000\u0385\u038d\u0001\u0000\u0000"+
		"\u0000\u0386\u038d\u0003\u00acV\u0000\u0387\u038d\u0003\u00b2Y\u0000\u0388"+
		"\u038d\u0003\u00b4Z\u0000\u0389\u038d\u0003\u00b8\\\u0000\u038a\u038d"+
		"\u0003\u00ba]\u0000\u038b\u038d\u0003\u00bc^\u0000\u038c\u036e\u0001\u0000"+
		"\u0000\u0000\u038c\u0381\u0001\u0000\u0000\u0000\u038c\u0386\u0001\u0000"+
		"\u0000\u0000\u038c\u0387\u0001\u0000\u0000\u0000\u038c\u0388\u0001\u0000"+
		"\u0000\u0000\u038c\u0389\u0001\u0000\u0000\u0000\u038c\u038a\u0001\u0000"+
		"\u0000\u0000\u038c\u038b\u0001\u0000\u0000\u0000\u038d\u0099\u0001\u0000"+
		"\u0000\u0000\u038e\u0390\u00058\u0000\u0000\u038f\u038e\u0001\u0000\u0000"+
		"\u0000\u038f\u0390\u0001\u0000\u0000\u0000\u0390\u0391\u0001\u0000\u0000"+
		"\u0000\u0391\u0396\u0003\u00c2a\u0000\u0392\u0393\u0005\u0001\u0000\u0000"+
		"\u0393\u0395\u0003\u00c2a\u0000\u0394\u0392\u0001\u0000\u0000\u0000\u0395"+
		"\u0398\u0001\u0000\u0000\u0000\u0396\u0394\u0001\u0000\u0000\u0000\u0396"+
		"\u0397\u0001\u0000\u0000\u0000\u0397\u009b\u0001\u0000\u0000\u0000\u0398"+
		"\u0396\u0001\u0000\u0000\u0000\u0399\u039a\u0005J\u0000\u0000\u039a\u039b"+
		"\u0005\u0002\u0000\u0000\u039b\u039c\u0003^/\u0000\u039c\u039d\u0005\u0003"+
		"\u0000\u0000\u039d\u009d\u0001\u0000\u0000\u0000\u039e\u039f\u0005\u00c5"+
		"\u0000\u0000\u039f\u03a0\u0005T\u0000\u0000\u03a0\u03a1\u0005\u0002\u0000"+
		"\u0000\u03a1\u03a2\u0003j5\u0000\u03a2\u03a3\u0005\u0003\u0000\u0000\u03a3"+
		"\u009f\u0001\u0000\u0000\u0000\u03a4\u03a5\u0005\u0092\u0000\u0000\u03a5"+
		"\u03a7\u0005\u0002\u0000\u0000\u03a6\u03a8\u0003\u00a2Q\u0000\u03a7\u03a6"+
		"\u0001\u0000\u0000\u0000\u03a7\u03a8\u0001\u0000\u0000\u0000\u03a8\u03aa"+
		"\u0001\u0000\u0000\u0000\u03a9\u03ab\u0003j5\u0000\u03aa\u03a9\u0001\u0000"+
		"\u0000\u0000\u03aa\u03ab\u0001\u0000\u0000\u0000\u03ab\u03ad\u0001\u0000"+
		"\u0000\u0000\u03ac\u03ae\u0003\u00a4R\u0000\u03ad\u03ac\u0001\u0000\u0000"+
		"\u0000\u03ad\u03ae\u0001\u0000\u0000\u0000\u03ae\u03af\u0001\u0000\u0000"+
		"\u0000\u03af\u03b0\u0005\u0003\u0000\u0000\u03b0\u00a1\u0001\u0000\u0000"+
		"\u0000\u03b1\u03b2\u0005\u0096\u0000\u0000\u03b2\u03b3\u0005#\u0000\u0000"+
		"\u03b3\u03b8\u0003~?\u0000\u03b4\u03b5\u0005\u0001\u0000\u0000\u03b5\u03b7"+
		"\u0003~?\u0000\u03b6\u03b4\u0001\u0000\u0000\u0000\u03b7\u03ba\u0001\u0000"+
		"\u0000\u0000\u03b8\u03b6\u0001\u0000\u0000\u0000\u03b8\u03b9\u0001\u0000"+
		"\u0000\u0000\u03b9\u00a3\u0001\u0000\u0000\u0000\u03ba\u03b8\u0001\u0000"+
		"\u0000\u0000\u03bb\u03bc\u0007\u000f\u0000\u0000\u03bc\u03be\u0003\u00a6"+
		"S\u0000\u03bd\u03bf\u0003\u00a8T\u0000\u03be\u03bd\u0001\u0000\u0000\u0000"+
		"\u03be\u03bf\u0001\u0000\u0000\u0000\u03bf\u03c9\u0001\u0000\u0000\u0000"+
		"\u03c0\u03c1\u0007\u000f\u0000\u0000\u03c1\u03c2\u0005 \u0000\u0000\u03c2"+
		"\u03c3\u0003\u00a6S\u0000\u03c3\u03c4\u0005\u001a\u0000\u0000\u03c4\u03c6"+
		"\u0003\u00aaU\u0000\u03c5\u03c7\u0003\u00a8T\u0000\u03c6\u03c5\u0001\u0000"+
		"\u0000\u0000\u03c6\u03c7\u0001\u0000\u0000\u0000\u03c7\u03c9\u0001\u0000"+
		"\u0000\u0000\u03c8\u03bb\u0001\u0000\u0000\u0000\u03c8\u03c0\u0001\u0000"+
		"\u0000\u0000\u03c9\u00a5\u0001\u0000\u0000\u0000\u03ca\u03cb\u0005\u00b9"+
		"\u0000\u0000\u03cb\u03d5\u0005\u009b\u0000\u0000\u03cc\u03cd\u0003~?\u0000"+
		"\u03cd\u03ce\u0005\u009b\u0000\u0000\u03ce\u03d5\u0001\u0000\u0000\u0000"+
		"\u03cf\u03d0\u0005+\u0000\u0000\u03d0\u03d5\u0005\u00a1\u0000\u0000\u03d1"+
		"\u03d2\u0003~?\u0000\u03d2\u03d3\u0005N\u0000\u0000\u03d3\u03d5\u0001"+
		"\u0000\u0000\u0000\u03d4\u03ca\u0001\u0000\u0000\u0000\u03d4\u03cc\u0001"+
		"\u0000\u0000\u0000\u03d4\u03cf\u0001\u0000\u0000\u0000\u03d4\u03d1\u0001"+
		"\u0000\u0000\u0000\u03d5\u00a7\u0001\u0000\u0000\u0000\u03d6\u03d7\u0005"+
		"D\u0000\u0000\u03d7\u03d8\u0005+\u0000\u0000\u03d8\u03e1\u0005\u00a1\u0000"+
		"\u0000\u03d9\u03da\u0005D\u0000\u0000\u03da\u03e1\u0005T\u0000\u0000\u03db"+
		"\u03dc\u0005D\u0000\u0000\u03dc\u03e1\u0005\u00ac\u0000\u0000\u03dd\u03de"+
		"\u0005D\u0000\u0000\u03de\u03df\u0005\u0084\u0000\u0000\u03df\u03e1\u0005"+
		"\u0090\u0000\u0000\u03e0\u03d6\u0001\u0000\u0000\u0000\u03e0\u03d9\u0001"+
		"\u0000\u0000\u0000\u03e0\u03db\u0001\u0000\u0000\u0000\u03e0\u03dd\u0001"+
		"\u0000\u0000\u0000\u03e1\u00a9\u0001\u0000\u0000\u0000\u03e2\u03e3\u0003"+
		"~?\u0000\u03e3\u03e4\u0005\u009b\u0000\u0000\u03e4\u03ed\u0001\u0000\u0000"+
		"\u0000\u03e5\u03e6\u0005+\u0000\u0000\u03e6\u03ed\u0005\u00a1\u0000\u0000"+
		"\u03e7\u03e8\u0003~?\u0000\u03e8\u03e9\u0005N\u0000\u0000\u03e9\u03ed"+
		"\u0001\u0000\u0000\u0000\u03ea\u03eb\u0005\u00b9\u0000\u0000\u03eb\u03ed"+
		"\u0005N\u0000\u0000\u03ec\u03e2\u0001\u0000\u0000\u0000\u03ec\u03e5\u0001"+
		"\u0000\u0000\u0000\u03ec\u03e7\u0001\u0000\u0000\u0000\u03ec\u03ea\u0001"+
		"\u0000\u0000\u0000\u03ed\u00ab\u0001\u0000\u0000\u0000\u03ee\u03ef\u0005"+
		"%\u0000\u0000\u03ef\u03f0\u0005\u0002\u0000\u0000\u03f0\u03f1\u0003~?"+
		"\u0000\u03f1\u03f2\u0005\u001c\u0000\u0000\u03f2\u03f3\u0003\u00aeW\u0000"+
		"\u03f3\u03f4\u0005\u0003\u0000\u0000\u03f4\u00ad\u0001\u0000\u0000\u0000"+
		"\u03f5\u03fd\u0003\u00b0X\u0000\u03f6\u03f7\u0005\u0002\u0000\u0000\u03f7"+
		"\u03fa\u0005\u00cb\u0000\u0000\u03f8\u03f9\u0005\u0001\u0000\u0000\u03f9"+
		"\u03fb\u0005\u00cb\u0000\u0000\u03fa\u03f8\u0001\u0000\u0000\u0000\u03fa"+
		"\u03fb\u0001\u0000\u0000\u0000\u03fb\u03fc\u0001\u0000\u0000\u0000\u03fc"+
		"\u03fe\u0005\u0003\u0000\u0000\u03fd\u03f6\u0001\u0000\u0000\u0000\u03fd"+
		"\u03fe\u0001\u0000\u0000\u0000\u03fe\u00af\u0001\u0000\u0000\u0000\u03ff"+
		"\u0400\u0003\u00e4r\u0000\u0400\u0401\u0006X\uffff\uffff\u0000\u0401\u0408"+
		"\u0001\u0000\u0000\u0000\u0402\u0403\u0005\u000e\u0000\u0000\u0403\u0404"+
		"\u0003\u00e4r\u0000\u0404\u0405\u0006X\uffff\uffff\u0000\u0405\u0407\u0001"+
		"\u0000\u0000\u0000\u0406\u0402\u0001\u0000\u0000\u0000\u0407\u040a\u0001"+
		"\u0000\u0000\u0000\u0408\u0406\u0001\u0000\u0000\u0000\u0408\u0409\u0001"+
		"\u0000\u0000\u0000\u0409\u00b1\u0001\u0000\u0000\u0000\u040a\u0408\u0001"+
		"\u0000\u0000\u0000\u040b\u040c\u0005G\u0000\u0000\u040c\u040d\u0005\u0002"+
		"\u0000\u0000\u040d\u040e\u0003~?\u0000\u040e\u040f\u0005Q\u0000\u0000"+
		"\u040f\u0410\u0003~?\u0000\u0410\u0411\u0005\u0003\u0000\u0000\u0411\u0418"+
		"\u0001\u0000\u0000\u0000\u0412\u0413\u0003\u00b6[\u0000\u0413\u0414\u0005"+
		"\u0002\u0000\u0000\u0414\u0415\u0003~?\u0000\u0415\u0416\u0005\u0003\u0000"+
		"\u0000\u0416\u0418\u0001\u0000\u0000\u0000\u0417\u040b\u0001\u0000\u0000"+
		"\u0000\u0417\u0412\u0001\u0000\u0000\u0000\u0418\u00b3\u0001\u0000\u0000"+
		"\u0000\u0419\u041a\u0005\u00b4\u0000\u0000\u041a\u041c\u0005\u0002\u0000"+
		"\u0000\u041b\u041d\u0007\u0010\u0000\u0000\u041c\u041b\u0001\u0000\u0000"+
		"\u0000\u041c\u041d\u0001\u0000\u0000\u0000\u041d\u041f\u0001\u0000\u0000"+
		"\u0000\u041e\u0420\u0003t:\u0000\u041f\u041e\u0001\u0000\u0000\u0000\u041f"+
		"\u0420\u0001\u0000\u0000\u0000\u0420\u0422\u0001\u0000\u0000\u0000\u0421"+
		"\u0423\u0005Q\u0000\u0000\u0422\u0421\u0001\u0000\u0000\u0000\u0422\u0423"+
		"\u0001\u0000\u0000\u0000\u0423\u0424\u0001\u0000\u0000\u0000\u0424\u0425"+
		"\u0003~?\u0000\u0425\u0426\u0005\u0003\u0000\u0000\u0426\u00b5\u0001\u0000"+
		"\u0000\u0000\u0427\u0428\u0007\f\u0000\u0000\u0428\u00b7\u0001\u0000\u0000"+
		"\u0000\u0429\u042a\u0007\u0011\u0000\u0000\u042a\u042b\u0005\u0002\u0000"+
		"\u0000\u042b\u042c\u0003\u00c0`\u0000\u042c\u042d\u0005\u0003\u0000\u0000"+
		"\u042d\u043a\u0001\u0000\u0000\u0000\u042e\u042f\u0007\u0011\u0000\u0000"+
		"\u042f\u0430\u0005\u0002\u0000\u0000\u0430\u0431\u0003P(\u0000\u0431\u0432"+
		"\u0005\u0003\u0000\u0000\u0432\u043a\u0001\u0000\u0000\u0000\u0433\u0434"+
		"\u0007\u0011\u0000\u0000\u0434\u0435\u0007\u0012\u0000\u0000\u0435\u0436"+
		"\u0005\u0002\u0000\u0000\u0436\u0437\u0003\u008aE\u0000\u0437\u0438\u0005"+
		"\u0003\u0000\u0000\u0438\u043a\u0001\u0000\u0000\u0000\u0439\u0429\u0001"+
		"\u0000\u0000\u0000\u0439\u042e\u0001\u0000\u0000\u0000\u0439\u0433\u0001"+
		"\u0000\u0000\u0000\u043a\u00b9\u0001\u0000\u0000\u0000\u043b\u043c\u0007"+
		"\u0013\u0000\u0000\u043c\u043d\u0005\u0002\u0000\u0000\u043d\u043e\u0003"+
		"\u00c0`\u0000\u043e\u043f\u0005\u0003\u0000\u0000\u043f\u044c\u0001\u0000"+
		"\u0000\u0000\u0440\u0441\u0007\u0013\u0000\u0000\u0441\u0442\u0005\u0002"+
		"\u0000\u0000\u0442\u0443\u0003P(\u0000\u0443\u0444\u0005\u0003\u0000\u0000"+
		"\u0444\u044c\u0001\u0000\u0000\u0000\u0445\u0446\u0007\u0013\u0000\u0000"+
		"\u0446\u0447\u0007\u0012\u0000\u0000\u0447\u0448\u0005\u0002\u0000\u0000"+
		"\u0448\u0449\u0003\u008aE\u0000\u0449\u044a\u0005\u0003\u0000\u0000\u044a"+
		"\u044c\u0001\u0000\u0000\u0000\u044b\u043b\u0001\u0000\u0000\u0000\u044b"+
		"\u0440\u0001\u0000\u0000\u0000\u044b\u0445\u0001\u0000\u0000\u0000\u044c"+
		"\u00bb\u0001\u0000\u0000\u0000\u044d\u044e\u0005\u00b3\u0000\u0000\u044e"+
		"\u044f\u0005\u0002\u0000\u0000\u044f\u0450\u0003\u0084B\u0000\u0450\u0451"+
		"\u0005\u001c\u0000\u0000\u0451\u0452\u0003\u008aE\u0000\u0452\u0454\u0005"+
		"\u0003\u0000\u0000\u0453\u0455\u0003\u00be_\u0000\u0454\u0453\u0001\u0000"+
		"\u0000\u0000\u0454\u0455\u0001\u0000\u0000\u0000\u0455\u00bd\u0001\u0000"+
		"\u0000\u0000\u0456\u0457\u0005\u000e\u0000\u0000\u0457\u0458\u0003\u008a"+
		"E\u0000\u0458\u00bf\u0001\u0000\u0000\u0000\u0459\u045a\u0006`\uffff\uffff"+
		"\u0000\u045a\u045b\u0005\u0002\u0000\u0000\u045b\u045c\u0003\u00c0`\u0000"+
		"\u045c\u045d\u0005\u0003\u0000\u0000\u045d\u0469\u0001\u0000\u0000\u0000"+
		"\u045e\u0469\u0003\u00c8d\u0000\u045f\u0469\u0003\u00ccf\u0000\u0460\u0469"+
		"\u0003\u00c6c\u0000\u0461\u0469\u0003\u00c4b\u0000\u0462\u0469\u0003\u00ca"+
		"e\u0000\u0463\u0469\u0003\u00d0h\u0000\u0464\u0469\u0003\u00d2i\u0000"+
		"\u0465\u0466\u0005\u0085\u0000\u0000\u0466\u0469\u0003\u00c0`\u0004\u0467"+
		"\u0469\u0003~?\u0000\u0468\u0459\u0001\u0000\u0000\u0000\u0468\u045e\u0001"+
		"\u0000\u0000\u0000\u0468\u045f\u0001\u0000\u0000\u0000\u0468\u0460\u0001"+
		"\u0000\u0000\u0000\u0468\u0461\u0001\u0000\u0000\u0000\u0468\u0462\u0001"+
		"\u0000\u0000\u0000\u0468\u0463\u0001\u0000\u0000\u0000\u0468\u0464\u0001"+
		"\u0000\u0000\u0000\u0468\u0465\u0001\u0000\u0000\u0000\u0468\u0467\u0001"+
		"\u0000\u0000\u0000\u0469\u0472\u0001\u0000\u0000\u0000\u046a\u046b\n\u0003"+
		"\u0000\u0000\u046b\u046c\u0005\u001a\u0000\u0000\u046c\u0471\u0003\u00c0"+
		"`\u0004\u046d\u046e\n\u0002\u0000\u0000\u046e\u046f\u0005\u008e\u0000"+
		"\u0000\u046f\u0471\u0003\u00c0`\u0003\u0470\u046a\u0001\u0000\u0000\u0000"+
		"\u0470\u046d\u0001\u0000\u0000\u0000\u0471\u0474\u0001\u0000\u0000\u0000"+
		"\u0472\u0470\u0001\u0000\u0000\u0000\u0472\u0473\u0001\u0000\u0000\u0000"+
		"\u0473\u00c1\u0001\u0000\u0000\u0000\u0474\u0472\u0001\u0000\u0000\u0000"+
		"\u0475\u0478\u0003~?\u0000\u0476\u0478\u0003\u00c0`\u0000\u0477\u0475"+
		"\u0001\u0000\u0000\u0000\u0477\u0476\u0001\u0000\u0000\u0000\u0478\u00c3"+
		"\u0001\u0000\u0000\u0000\u0479\u047a\u0003~?\u0000\u047a\u047b\u0007\u0014"+
		"\u0000\u0000\u047b\u047c\u0003~?\u0000\u047c\u00c5\u0001\u0000\u0000\u0000"+
		"\u047d\u047f\u0003~?\u0000\u047e\u0480\u0005\u0085\u0000\u0000\u047f\u047e"+
		"\u0001\u0000\u0000\u0000\u047f\u0480\u0001\u0000\u0000\u0000\u0480\u0481"+
		"\u0001\u0000\u0000\u0000\u0481\u0482\u0005 \u0000\u0000\u0482\u0483\u0003"+
		"~?\u0000\u0483\u0484\u0005\u001a\u0000\u0000\u0484\u0485\u0003~?\u0000"+
		"\u0485\u00c7\u0001\u0000\u0000\u0000\u0486\u0487\u0003~?\u0000\u0487\u0489"+
		"\u0005c\u0000\u0000\u0488\u048a\u0005\u0085\u0000\u0000\u0489\u0488\u0001"+
		"\u0000\u0000\u0000\u0489\u048a\u0001\u0000\u0000\u0000\u048a\u048b\u0001"+
		"\u0000\u0000\u0000\u048b\u048c\u0005\u0086\u0000\u0000\u048c\u0497\u0001"+
		"\u0000\u0000\u0000\u048d\u048e\u0003~?\u0000\u048e\u0490\u0005c\u0000"+
		"\u0000\u048f\u0491\u0005\u0085\u0000\u0000\u0490\u048f\u0001\u0000\u0000"+
		"\u0000\u0490\u0491\u0001\u0000\u0000\u0000\u0491\u0492\u0001\u0000\u0000"+
		"\u0000\u0492\u0493\u00058\u0000\u0000\u0493\u0494\u0005Q\u0000\u0000\u0494"+
		"\u0495\u0003~?\u0000\u0495\u0497\u0001\u0000\u0000\u0000\u0496\u0486\u0001"+
		"\u0000\u0000\u0000\u0496\u048d\u0001\u0000\u0000\u0000\u0497\u00c9\u0001"+
		"\u0000\u0000\u0000\u0498\u049a\u0003~?\u0000\u0499\u049b\u0005\u0085\u0000"+
		"\u0000\u049a\u0499\u0001\u0000\u0000\u0000\u049a\u049b\u0001\u0000\u0000"+
		"\u0000\u049b\u049c\u0001\u0000\u0000\u0000\u049c\u049e\u0007\u0015\u0000"+
		"\u0000\u049d\u049f\u0005\u0005\u0000\u0000\u049e\u049d\u0001\u0000\u0000"+
		"\u0000\u049e\u049f\u0001\u0000\u0000\u0000\u049f\u04a0\u0001\u0000\u0000"+
		"\u0000\u04a0\u04a2\u0003~?\u0000\u04a1\u04a3\u0005\u0005\u0000\u0000\u04a2"+
		"\u04a1\u0001\u0000\u0000\u0000\u04a2\u04a3\u0001\u0000\u0000\u0000\u04a3"+
		"\u04a9\u0001\u0000\u0000\u0000\u04a4\u04a7\u0005A\u0000\u0000\u04a5\u04a8"+
		"\u0003t:\u0000\u04a6\u04a8\u0003\u00e0p\u0000\u04a7\u04a5\u0001\u0000"+
		"\u0000\u0000\u04a7\u04a6\u0001\u0000\u0000\u0000\u04a8\u04aa\u0001\u0000"+
		"\u0000\u0000\u04a9\u04a4\u0001\u0000\u0000\u0000\u04a9\u04aa\u0001\u0000"+
		"\u0000\u0000\u04aa\u00cb\u0001\u0000\u0000\u0000\u04ab\u04ad\u0003~?\u0000"+
		"\u04ac\u04ae\u0005\u0085\u0000\u0000\u04ad\u04ac\u0001\u0000\u0000\u0000"+
		"\u04ad\u04ae\u0001\u0000\u0000\u0000\u04ae\u04af\u0001\u0000\u0000\u0000"+
		"\u04af\u04b0\u0005[\u0000\u0000\u04b0\u04b1\u0003\u00ceg\u0000\u04b1\u00cd"+
		"\u0001\u0000\u0000\u0000\u04b2\u04b3\u0007\u0012\u0000\u0000\u04b3\u04b4"+
		"\u0005\u0002\u0000\u0000\u04b4\u04b5\u0003\u008aE\u0000\u04b5\u04b6\u0005"+
		"\u0003\u0000\u0000\u04b6\u04c9\u0001\u0000\u0000\u0000\u04b7\u04b8\u0005"+
		"\u0002\u0000\u0000\u04b8\u04b9\u0003P(\u0000\u04b9\u04ba\u0005\u0003\u0000"+
		"\u0000\u04ba\u04c9\u0001\u0000\u0000\u0000\u04bb\u04c9\u0003\u00e0p\u0000"+
		"\u04bc\u04c5\u0005\u0002\u0000\u0000\u04bd\u04c2\u0003\u00c2a\u0000\u04be"+
		"\u04bf\u0005\u0001\u0000\u0000\u04bf\u04c1\u0003\u00c2a\u0000\u04c0\u04be"+
		"\u0001\u0000\u0000\u0000\u04c1\u04c4\u0001\u0000\u0000\u0000\u04c2\u04c0"+
		"\u0001\u0000\u0000\u0000\u04c2\u04c3\u0001\u0000\u0000\u0000\u04c3\u04c6"+
		"\u0001\u0000\u0000\u0000\u04c4\u04c2\u0001\u0000\u0000\u0000\u04c5\u04bd"+
		"\u0001\u0000\u0000\u0000\u04c5\u04c6\u0001\u0000\u0000\u0000\u04c6\u04c7"+
		"\u0001\u0000\u0000\u0000\u04c7\u04c9\u0005\u0003\u0000\u0000\u04c8\u04b2"+
		"\u0001\u0000\u0000\u0000\u04c8\u04b7\u0001\u0000\u0000\u0000\u04c8\u04bb"+
		"\u0001\u0000\u0000\u0000\u04c8\u04bc\u0001\u0000\u0000\u0000\u04c9\u00cf"+
		"\u0001\u0000\u0000\u0000\u04ca\u04cb\u0005E\u0000\u0000\u04cb\u04cc\u0007"+
		"\u0012\u0000\u0000\u04cc\u04cd\u0005\u0002\u0000\u0000\u04cd\u04ce\u0003"+
		"\u008aE\u0000\u04ce\u04cf\u0005\u0003\u0000\u0000\u04cf\u04d3\u0001\u0000"+
		"\u0000\u0000\u04d0\u04d1\u0005E\u0000\u0000\u04d1\u04d3\u0003~?\u0000"+
		"\u04d2\u04ca\u0001\u0000\u0000\u0000\u04d2\u04d0\u0001\u0000\u0000\u0000"+
		"\u04d3\u00d1\u0001\u0000\u0000\u0000\u04d4\u04d5\u0003~?\u0000\u04d5\u04d7"+
		"\u0005c\u0000\u0000\u04d6\u04d8\u0005\u0085\u0000\u0000\u04d7\u04d6\u0001"+
		"\u0000\u0000\u0000\u04d7\u04d8\u0001\u0000\u0000\u0000\u04d8\u04d9\u0001"+
		"\u0000\u0000\u0000\u04d9\u04da\u0005<\u0000\u0000\u04da\u04e4\u0001\u0000"+
		"\u0000\u0000\u04db\u04dd\u0003~?\u0000\u04dc\u04de\u0005\u0085\u0000\u0000"+
		"\u04dd\u04dc\u0001\u0000\u0000\u0000\u04dd\u04de\u0001\u0000\u0000\u0000"+
		"\u04de\u04df\u0001\u0000\u0000\u0000\u04df\u04e0\u0005x\u0000\u0000\u04e0"+
		"\u04e1\u0005\u0089\u0000\u0000\u04e1\u04e2\u0003\u0084B\u0000\u04e2\u04e4"+
		"\u0001\u0000\u0000\u0000\u04e3\u04d4\u0001\u0000\u0000\u0000\u04e3\u04db"+
		"\u0001\u0000\u0000\u0000\u04e4\u00d3\u0001\u0000\u0000\u0000\u04e5\u04e9"+
		"\u0005l\u0000\u0000\u04e6\u04e9\u0005s\u0000\u0000\u04e7\u04e9\u0003\u008a"+
		"E\u0000\u04e8\u04e5\u0001\u0000\u0000\u0000\u04e8\u04e6\u0001\u0000\u0000"+
		"\u0000\u04e8\u04e7\u0001\u0000\u0000\u0000\u04e9\u00d5\u0001\u0000\u0000"+
		"\u0000\u04ea\u04ef\u0003\u00d8l\u0000\u04eb\u04ec\u0005\u0001\u0000\u0000"+
		"\u04ec\u04ee\u0003\u00d8l\u0000\u04ed\u04eb\u0001\u0000\u0000\u0000\u04ee"+
		"\u04f1\u0001\u0000\u0000\u0000\u04ef\u04ed\u0001\u0000\u0000\u0000\u04ef"+
		"\u04f0\u0001\u0000\u0000\u0000\u04f0\u00d7\u0001\u0000\u0000\u0000\u04f1"+
		"\u04ef\u0001\u0000\u0000\u0000\u04f2\u04f5\u0003\u00c2a\u0000\u04f3\u04f5"+
		"\u0003<\u001e\u0000\u04f4\u04f2\u0001\u0000\u0000\u0000\u04f4\u04f3\u0001"+
		"\u0000\u0000\u0000\u04f5\u04f7\u0001\u0000\u0000\u0000\u04f6\u04f8\u0003"+
		"\u00deo\u0000\u04f7\u04f6\u0001\u0000\u0000\u0000\u04f7\u04f8\u0001\u0000"+
		"\u0000\u0000\u04f8\u00d9\u0001\u0000\u0000\u0000\u04f9\u04fc\u0003\u00e0"+
		"p\u0000\u04fa\u04fc\u0005\u00cb\u0000\u0000\u04fb\u04f9\u0001\u0000\u0000"+
		"\u0000\u04fb\u04fa\u0001\u0000\u0000\u0000\u04fc\u00db\u0001\u0000\u0000"+
		"\u0000\u04fd\u0500\u0003\u00e0p\u0000\u04fe\u0500\u0003v;\u0000\u04ff"+
		"\u04fd\u0001\u0000\u0000\u0000\u04ff\u04fe\u0001\u0000\u0000\u0000\u0500"+
		"\u00dd\u0001\u0000\u0000\u0000\u0501\u0502\u0005\u001c\u0000\u0000\u0502"+
		"\u0505\u0003\u00e4r\u0000\u0503\u0505\u0003\u00eau\u0000\u0504\u0501\u0001"+
		"\u0000\u0000\u0000\u0504\u0503\u0001\u0000\u0000\u0000\u0505\u00df\u0001"+
		"\u0000\u0000\u0000\u0506\u0507\u0005\u0016\u0000\u0000\u0507\u0511\u0005"+
		"\u00d0\u0000\u0000\u0508\u0509\u0005\u0016\u0000\u0000\u0509\u0511\u0003"+
		"\u00e4r\u0000\u050a\u050b\u0005\u0017\u0000\u0000\u050b\u0511\u0005\u00d0"+
		"\u0000\u0000\u050c\u050e\u0005\u0017\u0000\u0000\u050d\u050f\u0005\u00cb"+
		"\u0000\u0000\u050e\u050d\u0001\u0000\u0000\u0000\u050e\u050f\u0001\u0000"+
		"\u0000\u0000\u050f\u0511\u0001\u0000\u0000\u0000\u0510\u0506\u0001\u0000"+
		"\u0000\u0000\u0510\u0508\u0001\u0000\u0000\u0000\u0510\u050a\u0001\u0000"+
		"\u0000\u0000\u0510\u050c\u0001\u0000\u0000\u0000\u0511\u00e1\u0001\u0000"+
		"\u0000\u0000\u0512\u0517\u0003\u00e4r\u0000\u0513\u0514\u0005\u000e\u0000"+
		"\u0000\u0514\u0516\u0003\u00e4r\u0000\u0515\u0513\u0001\u0000\u0000\u0000"+
		"\u0516\u0519\u0001\u0000\u0000\u0000\u0517\u0515\u0001\u0000\u0000\u0000"+
		"\u0517\u0518\u0001\u0000\u0000\u0000\u0518\u051c\u0001\u0000\u0000\u0000"+
		"\u0519\u0517\u0001\u0000\u0000\u0000\u051a\u051c\u0005\u00d0\u0000\u0000"+
		"\u051b\u0512\u0001\u0000\u0000\u0000\u051b\u051a\u0001\u0000\u0000\u0000"+
		"\u051c\u00e3\u0001\u0000\u0000\u0000\u051d\u051e\u0003\u00eau\u0000\u051e"+
		"\u00e5\u0001\u0000\u0000\u0000\u051f\u0520\u0005\u00c8\u0000\u0000\u0520"+
		"\u00e7\u0001\u0000\u0000\u0000\u0521\u0526\u0003\u00eau\u0000\u0522\u0523"+
		"\u0005\u000e\u0000\u0000\u0523\u0525\u0003\u00eau\u0000\u0524\u0522\u0001"+
		"\u0000\u0000\u0000\u0525\u0528\u0001\u0000\u0000\u0000\u0526\u0524\u0001"+
		"\u0000\u0000\u0000\u0526\u0527\u0001\u0000\u0000\u0000\u0527\u00e9\u0001"+
		"\u0000\u0000\u0000\u0528\u0526\u0001\u0000\u0000\u0000\u0529\u052c\u0005"+
		"\u00cf\u0000\u0000\u052a\u052c\u0007\u0016\u0000\u0000\u052b\u0529\u0001"+
		"\u0000\u0000\u0000\u052b\u052a\u0001\u0000\u0000\u0000\u052c\u00eb\u0001"+
		"\u0000\u0000\u0000\u00a3\u00f3\u00f8\u0100\u0109\u010f\u0112\u0118\u011b"+
		"\u012a\u012f\u0132\u013d\u0141\u0148\u0150\u0153\u0157\u015a\u015e\u0160"+
		"\u0164\u0168\u016a\u016d\u016f\u0173\u0176\u0179\u0181\u0188\u018e\u0192"+
		"\u0196\u0199\u019f\u01a1\u01a6\u01aa\u01ae\u01b2\u01b5\u01bb\u01bd\u01c2"+
		"\u01c8\u01cd\u01d1\u01d9\u01e2\u01e6\u01ea\u01f0\u01f8\u0203\u020c\u0218"+
		"\u021f\u0223\u0226\u022b\u0238\u0240\u0246\u024c\u0255\u025a\u0260\u0272"+
		"\u0276\u0279\u027c\u027f\u0285\u0290\u0299\u02a3\u02ac\u02b1\u02b5\u02b9"+
		"\u02bb\u02c3\u02d9\u02e4\u02e8\u02f5\u030b\u031b\u031d\u0325\u0329\u032d"+
		"\u0330\u0334\u033b\u0341\u0349\u0350\u0354\u035c\u0360\u0372\u0376\u0379"+
		"\u037c\u037f\u038c\u038f\u0396\u03a7\u03aa\u03ad\u03b8\u03be\u03c6\u03c8"+
		"\u03d4\u03e0\u03ec\u03fa\u03fd\u0408\u0417\u041c\u041f\u0422\u0439\u044b"+
		"\u0454\u0468\u0470\u0472\u0477\u047f\u0489\u0490\u0496\u049a\u049e\u04a2"+
		"\u04a7\u04a9\u04ad\u04c2\u04c5\u04c8\u04d2\u04d7\u04dd\u04e3\u04e8\u04ef"+
		"\u04f4\u04f7\u04fb\u04ff\u0504\u050e\u0510\u0517\u051b\u0526\u052b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}