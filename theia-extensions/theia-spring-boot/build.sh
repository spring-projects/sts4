#!/bin/bash
set -e

workdir=`pwd`/spring-boot

cd ${workdir}

# Use maven to build fat jar of the language server
cd ../../../headless-services/spring-boot-language-server
./build.sh

rm -fr ${workdir}/jars
mkdir -p ${workdir}/jars
cp target/*.jar ${workdir}/jars

cd ${workdir}/..
yarn
