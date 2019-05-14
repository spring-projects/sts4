#!/bin/bash
set -e

workdir=`pwd`

# Download yaml TextMate grammar
curl https://raw.githubusercontent.com/textmate/yaml.tmbundle/master/Syntaxes/YAML.tmLanguage > yaml-support/yaml.tmLanguage

# Preinstall commons-vscode package
(cd ../commons-vscode ; npm install ; npm pack)
npm install ../commons-vscode/*-commons-vscode-*.tgz

# Use maven to build fat jar of the language server
cd ../../headless-services/manifest-yaml-language-server
./build.sh

#Clean old LS folder
rm -fr ${workdir}/language-server
mkdir -p ${workdir}/language-server

# Explode LS JAR
cd ${workdir}/language-server
server_jar_file=$(find ${workdir}/../../headless-services/manifest-yaml-language-server/target -name '*-exec.jar');
jar -xvf ${server_jar_file}


