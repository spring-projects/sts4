#!/bin/bash
set -e

workdir=`pwd`

# Download yaml TextMate grammar
curl https://raw.githubusercontent.com/textmate/yaml.tmbundle/master/Syntaxes/YAML.tmLanguage > yaml-support/yaml.tmLanguage

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install)
npm install ../commons-vscode

# Use maven to build fat jar of the language server
cd ../../headless-services/manifest-yaml-language-server
./build.sh

mkdir -p ${workdir}/jars
cp target/*.jar ${workdir}/jars/language-server.jar

