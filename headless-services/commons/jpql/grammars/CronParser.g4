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
parser grammar CronParser;
options { tokenVocab=CronLexer; }

cronExpression
   : WS* secondsElement WS+ minutesElement WS+ hoursElement WS+ daysElement WS+ monthsElement WS+ daysOfWeekElement WS*
   | WS* MACRO WS*
   ;
   
secondsElement
    : cronElement
    ;

minutesElement
    : cronElement
    ;

hoursElement
    : cronElement
    ;

daysElement
    : nearestWeekDayToDayOfMonthElement
    | lastDayOfMonthElement
    | cronElement
    ;

monthsElement
    : cronElement
    ;

daysOfWeekElement
    : nthDayOfWeekElement
    | lastDayOfWeekElement
    | cronElement
    ;
    
cronElement
   : rangeCronList
   | periodicCronElement
   ;

rangeCronElement
   : terminalCronElement (DASH (INT | weekdayLiteral | monthLiteral))*
   ;

terminalCronElement
   : ( INT | weekdayLiteral | monthLiteral | STAR | QUESTION )
   ;

periodicCronElement
   : terminalCronElement SLASH rangeCronList
   ;

rangeCronList
   : rangeCronElement (COMMA rangeCronElement)*
   ;
   
nthDayOfWeekElement
   : INT TAG INT
   | weekdayLiteral TAG INT
   ;
   
lastDayOfWeekElement
   : INT L
   | weekdayLiteral L
   ;
   
nearestWeekDayToDayOfMonthElement
   : INT W
   ;
   
lastDayOfMonthElement
    : L (DASH INT)?
    | LW
    ;

weekdayLiteral
    : MON
    | TUE
    | WED
    | THU
    | FRI
    | SAT
    | SUN
    ;

monthLiteral
    : JAN
    | FEB
    | MAR
    | APR
    | MAY
    | JUN
    | JUL
    | AUG
    | SEP
    | OCT
    | NOV
    | DEC
    ;
