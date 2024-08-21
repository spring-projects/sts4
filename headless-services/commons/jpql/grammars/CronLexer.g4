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

lexer grammar CronLexer;

WS
	: [ \t\r\n]+
	;

STAR        : '*';
QUESTION    : '?';
DASH        : '-';
SLASH       : '/';
COMMA       : ',';
L           : 'L';
W           : 'W';
LW          : 'LW';
TAG         : '#';

INT
    : DIGIT+
    ;
    
MON: 'MON';
TUE: 'TUE';
WED: 'WED';
THU: 'THU';
FRI: 'FRI';
SAT: 'SAT';
SUN: 'SUN';
    
JAN: 'JAN';
FEB: 'FEB';
MAR: 'MAR';
APR: 'APR';
MAY: 'MAY';
JUN: 'JUN';
JUL: 'JUL';
AUG: 'AUG';
SEP: 'SEP';
OCT: 'OCT';
NOV: 'NOV';
DEC: 'DEC';
    
IDENTIFIER
    : (ALPHABETIC | '_' )* (ALPHABETIC_NO_L | '_')
    ;
    
MACRO
    : '@' (ALPHABETIC | '_') (ALPHABETIC | DIGIT | '_' | '$')*
    ;
    
fragment ALPHABETIC
    : [a-zA-Z]
    ;

fragment ALPHABETIC_NO_L
    : [a-zA-KM-Z]
    ;

fragment DIGIT
    : [0-9]
    ;