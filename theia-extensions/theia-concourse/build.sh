#!/bin/bash
set -e

workdir=`pwd`/concourse

cd ${workdir}

# Use maven to build fat jar of the language server
cd ../../../headless-services/concourse-language-server
./build.sh

rm -fr ${workdir}/jars
mkdir -p ${workdir}/jars
cp target/*-exec.jar ${workdir}/jars

cd ${workdir}/..
yarn
