#!/bin/bash
set -e

workdir=`pwd`/spring-boot

cd ${workdir}

# Clean old jars
rm -fr ${workdir}/jars
mkdir -p ${workdir}/jars


# Use maven to build fat jar of the language server
cd ${workdir}/../../../headless-services/spring-boot-language-server
./build.sh
cp target/*.jar ${workdir}/jars

cd ${workdir}/../../../headless-services/jdt-ls-extension
cp org.springframework.tooling.jdt.ls.extension/target/*.jar ${workdir}/jars/jdt-ls-extension.jar
cp org.springframework.tooling.jdt.ls.commons/target/*.jar ${workdir}/jars/jdt-ls-commons.jar

cd ${workdir}/..
yarn
