#!/bin/bash
set -e
ANTLR_VERSION=4.13.1
if [ ! -f antlr-${ANTLR_VERSION}.jar ]; then
    curl https://www.antlr.org/download/antlr-${ANTLR_VERSION}-complete.jar --output antlr-${ANTLR_VERSION}.jar
fi
workdir=$(pwd)
cd grammars
java -jar ${workdir}/antlr-${ANTLR_VERSION}.jar Jpql.g4 -o ${workdir}/src/main/java/org/springframework/ide/vscode/parser/jpql -package org.springframework.ide.vscode.parser.jpql
java -jar ${workdir}/antlr-${ANTLR_VERSION}.jar Hql.g4 -o ${workdir}/src/main/java/org/springframework/ide/vscode//parser/hql -package org.springframework.ide.vscode.parser.hql

