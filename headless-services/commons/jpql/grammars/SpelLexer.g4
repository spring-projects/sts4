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

lexer grammar SpelLexer;

@header {
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
}

@members {
    int nesting = 0;
}

SEMICOLON: ';';

fragment NEWLINE
   : '\r\n'
   | [\r\n\u2028\u2029]
   ;

WS
	: [ \t\r\n]+ -> channel(HIDDEN)
	;

// ========================== S P E L ====================================================

INC: '++';
PLUS: '+';
DEC: '--';
MINUS: '-';
COLON: ':';
DOT: '.';
COMMA: ',';
STAR: '*';
DIV: '/';
MOD: '%';
LPAREN: '(';
RPAREN: ')';
LSQUARE: '[';
RSQUARE: ']';
HASH: '#';
BEAN_REF: '@';
SELECT_FIRST: '^[';
POWER: '^';
NE: '!=';
PROJECT: '![';
NOT: '!';
EQ: '==';
ASSIGN: '=';
SYMBOLIC_AND: '&&';
FACTORY_BEAN_REF: '&';
SYMBOLIC_OR: '||';
SELECT: '?[';
ELVIS: '?:';
SAFE_NAVI: '?.';
QMARK: '?';
SELECT_LAST: '$[';
GE: '>=';
GT: '>';
LE: '<=';
LT: '<';

// Special treatment to support template literals
LCURLY: '{' { nesting++; } -> pushMode(DEFAULT_MODE);

RCURLY: '}' {
	if (nesting > 0) {
		nesting--;
	}
} -> popMode;

BACKTICK
	: '`' -> pushMode(IN_TEMPLATE_STRING)
	;






OR: 'or';
AND: 'and';

TRUE: 'true';
FALSE: 'false';
NEW: 'new';
NULL: 'null';
T: 'T';
MATCHES: 'matches';
GT_KEYWORD: 'gt';
GE_KEYWORD: 'ge';
LE_KEYWORD: 'le';
LT_KEYWORD: 'lt';
EQ_KEYWORD: 'eq';
NE_KEYWORD: 'ne';

// IDENTIFIER appearing AFTER tokens like OR and AND make those lex as special tokens,
// like SpEL's lexIdentifier() does
IDENTIFIER
    : (ALPHABETIC | '_') (ALPHABETIC | DIGIT | '_' | '$')*
    ;

REAL_LITERAL
    : '.' DECIMAL_DIGIT+ EXPONENT_PART? REAL_TYPE_SUFFIX?
    | DECIMAL_DIGIT+ '.' DECIMAL_DIGIT+ EXPONENT_PART? REAL_TYPE_SUFFIX?
    | DECIMAL_DIGIT+ EXPONENT_PART REAL_TYPE_SUFFIX?
    | DECIMAL_DIGIT+ REAL_TYPE_SUFFIX
    ;

INTEGER_LITERAL
	 : DECIMAL_DIGIT+ INTEGER_TYPE_SUFFIX?
	 ;

STRING_LITERAL
	: SINGLE_QUOTED_STRING
	| DOUBLE_QUOTED_STRING
	;

SINGLE_QUOTED_STRING
    : '\'' ( '\'\'' | ~['\n] )* '\''
    ;

DOUBLE_QUOTED_STRING
    : '"' ( '""' | ~["\n] )* '"'
    ;

fragment INTEGER_TYPE_SUFFIX : ( 'L' | 'l' );
fragment HEX_DIGIT : [0-9A-Fa-f];
fragment DECIMAL_DIGIT: [0-9];
fragment EXPONENT_PART
    : 'e' (SIGN)* (DECIMAL_DIGIT)+
    | 'E' (SIGN)* (DECIMAL_DIGIT)+
    ;

fragment SIGN : '+' | '-' ;
fragment REAL_TYPE_SUFFIX : 'F' | 'f' | 'D' | 'd';



fragment ALPHABETIC
// See Character.isLetter()
// and https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md#lexer-rule-elements
// and https://github.com/spring-projects/spring-framework/commit/c8c8f5722bc58452894742534954c0935653771f
    : [\p{Lu}] // UPPERCASE_LETTER
    | [\p{Ll}] // LOWERCASE_LETTER
    | [\p{Lt}] // TITLECASE_LETTER
    | [\p{Lm}] // MODIFIER_LETTER
    | [\p{Lo}] // OTHER_LETTER
    ;

fragment DIGIT
    : [0-9]
    ;


mode IN_TEMPLATE_STRING;

ESCAPED_BACKTICK
	: '``'
	;

SPEL_IN_TEMPLATE_STRING_OPEN: '#{' { nesting++; } -> pushMode(DEFAULT_MODE);

TEMPLATE_TEXT
	: ~[`\n]
	;

BACKTICK_IN_TEMPLATE
	: '`' -> type(BACKTICK), popMode
	;