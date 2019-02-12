#!/bin/bash
set -e

workdir=`pwd`/spring-boot

cd ${workdir}

# Clean old jars
rm -fr ${workdir}/jars
mkdir -p ${workdir}/jars

# Use maven to build common-lsp-extensions - jdt extension depend on that jar
cd ../../../headless-services/
./mvnw -pl :commons-parent clean install -DskipTests
./mvnw -pl :commons-lsp-extensions clean install
cd ${workdir}

# Use maven to build jdt ls extension
cd ../../../headless-services/jdt-ls-extension
if command -v xvfb-run ; then
    echo "Using xvfb to run in headless environment..."
    xvfb-run ../mvnw clean integration-test
else
    ../mvnw clean integration-test
fi
cp org.springframework.tooling.jdt.ls.extension/target/*.jar ${workdir}/jars/jdt-ls-extension.jar
cp org.springframework.tooling.jdt.ls.commons/target/*.jar ${workdir}/jars/jdt-ls-commons.jar

cd ${workdir}

# Use maven to build fat jar of the language server
cd ../../../headless-services/spring-boot-language-server
./build.sh
cp target/*.jar ${workdir}/jars

cd ${workdir}/..
yarn
