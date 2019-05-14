#!/bin/bash
set -e

workdir=`pwd`/cf-manifest-yaml

cd ${workdir}

# Clear LS folder
rm -fr ${workdir}/server
mkdir -p ${workdir}/server/cf-manifest-yaml-language-server

# Use maven to build fat jar of the language server
cd ../../../headless-services/manifest-yaml-language-server
./build.sh
cd ${workdir}/server/cf-manifest-yaml-language-server
server_jar_file=$(find ${workdir}/../../../headless-services/manifest-yaml-language-server/target -name '*-exec.jar');
jar -xvf ${server_jar_file}

cd ${workdir}/..
yarn
