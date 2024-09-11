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
 
/**
 * Based on the answer to this Stack Overflow question:
 * https://stackoverflow.com/questions/6132529/antlr-parsing-java-properties 
 */
 
grammar PropertyPlaceHolder;

start
  :  line* EOF
  ;

line
  :  Space* keyDefaultValuePair Space* EOF
  ;
  
keyDefaultValuePair
  :  key
  |  key defaultValue
  ;
  
defaultValue
  : Colon value
  ;

key
  :  identifier (Dot identifier)*
  ;
  
identifier
  : Identifier+
  ;

value
  :  (Identifier | Exclamation | Number | Space | Backslash Backslash | Backslash LineBreak | Equals | Colon | Dot)*
  ;

Backslash : '\\';
Colon     : ':';
Equals    : '=';
Exclamation: '!';
Number    : '#';
Dot       : '.';

LineBreak
  :  '\r'? '\n'
  |  '\r'
  ;

Space
  :  ' ' 
  |  '\t' 
  |  '\f'
  |  LineBreak
  ;

Identifier
  : IdentifierChar+
  ;
  
fragment IdentifierChar
  : ~(' ' | ':' | '=' | '\r' | '\n' | '.')
  |  Backslash (Colon | Equals)
  ;
