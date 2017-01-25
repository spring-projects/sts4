#!/bin/bash
set -e
# Download yaml TextMate grammar
curl https://raw.githubusercontent.com/textmate/yaml.tmbundle/master/Syntaxes/YAML.tmLanguage > yaml-support/yaml.tmLanguage

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install)
npm install ../commons-vscode

# Use maven to build fat jar of the language server
../mvnw -U -f ../pom.xml -pl vscode-concourse -am clean install
