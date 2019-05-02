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
cp target/*-exec.jar ${workdir}/jars

cd ${workdir}/../../../headless-services/jdt-ls-extension
cp org.springframework.tooling.jdt.ls.extension/target/*.jar ${workdir}/jars/jdt-ls-extension.jar
cp org.springframework.tooling.jdt.ls.commons/target/*.jar ${workdir}/jars/jdt-ls-commons.jar

# Copy Reactor dependency bundles
cp org.springframework.tooling.jdt.ls.commons/target/dependencies/io.projectreactor.reactor-core.jar ${workdir}/jars/
cp org.springframework.tooling.jdt.ls.commons/target/dependencies/org.reactivestreams.reactive-streams.jar ${workdir}/jars/

cd ${workdir}/..
yarn
