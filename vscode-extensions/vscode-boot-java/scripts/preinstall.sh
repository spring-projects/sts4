#!/bin/bash
set -e

workdir=`pwd`

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install ; npm pack)
npm install ../commons-vscode/commons-vscode-*.tgz

# Use maven to build fat jar of the language server
cd ../../headless-services/boot-java-language-server
./build.sh

# mkdir -p ${workdir}/jars
cp target/*.jar ${workdir}/jars/language-server.jar

