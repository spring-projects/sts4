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

parser grammar SpelParser;

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

options {
	tokenVocab = SpelLexer;
}

script
	: spelExpr EOF
	;

spelExpr
	: (PLUS | MINUS | NOT | INC | DEC) spelExpr 	// ~unaryspelExpr
	| spelExpr (INC | DEC)						// ~incspelExpr
	| spelExpr POWER spelExpr				// ~powerExpr
	| spelExpr (STAR | DIV | MOD) spelExpr	// ~productspelExpr
	| spelExpr (PLUS | MINUS) spelExpr		// ~sumspelExpr
	| spelExpr (GT | LT | LE | GE | EQ | NE | GT_KEYWORD | LT_KEYWORD | LE_KEYWORD | GE_KEYWORD | EQ_KEYWORD | NE_KEYWORD) spelExpr // ~relationalspelExpr
	| spelExpr (AND | SYMBOLIC_AND) spelExpr // ~logicalAndspelExpr
	| spelExpr (OR | SYMBOLIC_OR) spelExpr // ~logicalOrspelExpr
	| spelExpr MATCHES spelExpr // ~regexExpr
	// spelExpr
	| spelExpr ASSIGN spelExpr
	| spelExpr ELVIS spelExpr
	| spelExpr QMARK spelExpr COLON spelExpr
	// primaryspelExpr
	| startNode node*
	;


node
	: dottedNode
	| nonDottedNode
	;

nonDottedNode
	: (LSQUARE spelExpr RSQUARE)
	| inputParameter
	| propertyPlaceHolder
	;

dottedNode
	: (DOT | SAFE_NAVI)	methodOrProperty
	| functionOrVar
	| projection
	| selection
	;

functionOrVar
	: HASH IDENTIFIER
	| HASH IDENTIFIER methodArgs
	;

methodArgs
	: LPAREN args RPAREN
	;

args
	: spelExpr? (COMMA spelExpr)*
	;

methodOrProperty
	: IDENTIFIER
	| IDENTIFIER methodArgs
	;

projection
	: PROJECT spelExpr RSQUARE
	;

selection
	: (SELECT | SELECT_FIRST | SELECT_LAST) spelExpr RSQUARE
	;

startNode
	: literal
	| parenspelExpr
	| typeReference
	| nullReference
	| constructorReference
	| methodOrProperty
	| functionOrVar
	| beanReference
	| projection
	| selection
	| inlineListOrMap
	| inputParameter
	| propertyPlaceHolder
	;


literal
	: numericLiteral
	| STRING_LITERAL
	| TRUE
	| FALSE
	;

numericLiteral
    : INTEGER_LITERAL
    | REAL_LITERAL
    ;

parenspelExpr
	: LPAREN spelExpr RPAREN
	;

typeReference
	: T LPAREN possiblyQualifiedId (LSQUARE RSQUARE)* RPAREN
	;

possiblyQualifiedId
	: IDENTIFIER (DOT IDENTIFIER)*
	;

nullReference
	: NULL
	;

constructorReference
	: NEW possiblyQualifiedId (LSQUARE spelExpr? RSQUARE)+ inlineListOrMap?
	| NEW possiblyQualifiedId constructorArgs
	;

constructorArgs
	: LPAREN args RPAREN
	;

inlineListOrMap
	: LCURLY RCURLY
	| LCURLY COLON RCURLY
	| LCURLY listBindings RCURLY
	| LCURLY mapBindings RCURLY
	;

listBindings
	: bindings+=listBinding (COMMA bindings+=listBinding)*
	;

listBinding
	: spelExpr
	;

mapBindings
	: bindings+=mapBinding (COMMA bindings+=mapBinding)*
	;

mapBinding
	: key=spelExpr COLON value=spelExpr
	;

beanReference
	: (BEAN_REF | FACTORY_BEAN_REF) (IDENTIFIER | STRING_LITERAL)
	;
	
inputParameter
	: (LSQUARE INTEGER_LITERAL RSQUARE)
	;
	
propertyPlaceHolder
	: PROPERTY_PLACE_HOLDER
	;
	
