/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
 
/**
 * Based on the answer to this Stack Overflow question:
 * https://stackoverflow.com/questions/6132529/antlr-parsing-java-properties 
 */
 
grammar JavaProperties;

parse
  :  line* EOF
  ;

line
  :  propertyLine
  |  commentLine
  |  emptyLine
  ;
  
propertyLine
  : Space* keyValuePair
  ;

commentLine
  : Space* (Exclamation | Number) ~(LineBreak)* (LineBreak | EOF)
  ;
  
emptyLine
  : Space* LineBreak
  ;
  
keyValuePair
  :  key separatorAndValue (LineBreak | EOF)
  ;

key
  :  keyChar+
  ;

keyChar
  :  IdentifierChar 
  |  Backslash (Colon | Equals)
  ;
  
separatorAndValue
  :  (Space | Colon | Equals) valueChar*
  ;

valueChar
  :  IdentifierChar
  |  Exclamation
  |  Number
  |  Space
  |  Backslash anyChar
  |  Equals
  |  Colon
  ;

anyChar
  : LineBreak
  | IdentifierChar
  | Backslash
  | Colon
  | Equals
  | Space
  | Exclamation
  | Number
  ;

Backslash : '\\';
Colon     : ':';
Equals    : '=';
Exclamation: '!';
Number    : '#';

LineBreak
  :  '\r'? '\n'
  |  '\r'
  ;

Space
  :  ' ' 
  |  '\t' 
  |  '\f'
  ;

IdentifierChar
  : ~(' ' | ':' | '=' | '\r' | '\n' )
  ;
