#!/bin/bash
set -e

workdir=`pwd`

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install)
npm install ../commons-vscode

# Use maven to build fat jar of the language server
cd ../../headless-services/boot-properties-language-server
./build.sh

mkdir -p ${workdir}/jars
cp target/*.jar ${workdir}/jars/language-server.jar

