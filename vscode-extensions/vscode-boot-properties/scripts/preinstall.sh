#!/bin/bash
set -e

workdir=`pwd`

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install)
npm install ../commons-vscode

# Copy grammar files for .properties and .yml format
curl https://raw.githubusercontent.com/textmate/yaml.tmbundle/master/Syntaxes/YAML.tmLanguage > yaml-support/yaml.tmLanguage
curl https://raw.githubusercontent.com/textmate/java.tmbundle/master/Syntaxes/JavaProperties.plist > properties-support/java-properties.tmLanguage

# Use maven to build fat jar of the language server
cd ../../headless-services/boot-properties-language-server
./build.sh

mkdir -p ${workdir}/jars
cp target/*.jar ${workdir}/jars/language-server.jar

