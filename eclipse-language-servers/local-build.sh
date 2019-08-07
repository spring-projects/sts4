#!/bin/bash
set -e
workdir=`pwd`
cd ../headless-services
./mvnw clean install -Dmaven.test.skip=true

cd $workdir
./mvnw -Pe412 clean install -Dmaven.test.skip=true
