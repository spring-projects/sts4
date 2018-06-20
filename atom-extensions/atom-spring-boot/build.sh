#!/bin/bash
set -e
set -x
workdir=`pwd`

# Cleanup old jars
rm -rf server
mkdir server

# Build fat jar and copy it over into
cd ../../headless-services/spring-boot-language-server
./build.sh
cp -f target/spring-boot-language-server-*.jar $workdir/server/spring-boot-language-server.jar

# Installs atom-commons from source rather than npm registry
# cd ../atom-commons
# npm install

cd $workdir
npm install
npm run build
