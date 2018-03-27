#!/bin/bash
set -e

workdir=`pwd`

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install ; npm pack)
npm install ../commons-vscode/commons-vscode-*.tgz

# Copy grammar files for .properties and .yml format
curl https://raw.githubusercontent.com/textmate/yaml.tmbundle/master/Syntaxes/YAML.tmLanguage > yaml-support/yaml.tmLanguage
curl https://raw.githubusercontent.com/textmate/java.tmbundle/master/Syntaxes/JavaProperties.plist > properties-support/java-properties.tmLanguage

# Clean old jars
rm -fr ${workdir}/jars
mkdir -p ${workdir}/jars

# Use maven to build jdt ls extension
cd ../../headless-services/org.springframework.tooling.jdt.ls.extension
../mvnw \
    clean package
cp target/*.jar ${workdir}/jars/jdt-ls-extension.jar

# Use maven to build fat jar of the language server
cd ../../headless-services/spring-boot-language-server
./build.sh
cp target/*.jar ${workdir}/jars

