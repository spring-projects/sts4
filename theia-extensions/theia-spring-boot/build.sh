#!/bin/bash
set -e

workdir=`pwd`/spring-boot

cd ${workdir}

# Clean old jars
rm -fr ${workdir}/jars
rm -fr ${workdir}/language-server
mkdir -p ${workdir}/jars
mkdir -p ${workdir}/language-server

# Use maven to build fat jar of the language server
cd ${workdir}/../../../headless-services/spring-boot-language-server
./build.sh

cd ${workdir}/language-server
server_jar_file=$(find ${workdir}/../../../headless-services/spring-boot-language-server/target -name '*-exec.jar');
jar -xvf ${server_jar_file}

cd ${workdir}/../../../headless-services/jdt-ls-extension
cp org.springframework.tooling.jdt.ls.extension/target/*.jar ${workdir}/jars/jdt-ls-extension.jar
cp org.springframework.tooling.jdt.ls.commons/target/*.jar ${workdir}/jars/jdt-ls-commons.jar

# Copy Reactor dependency bundles
cp org.springframework.tooling.jdt.ls.commons/target/dependencies/io.projectreactor.reactor-core.jar ${workdir}/jars/
cp org.springframework.tooling.jdt.ls.commons/target/dependencies/org.reactivestreams.reactive-streams.jar ${workdir}/jars/

cd ${workdir}/..
yarn
