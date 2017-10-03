#!/bin/bash
set -e
workdir=`pwd`
cd ../headless-services
mvn clean install -Dmaven.test.skip=true

cd $workdir
mvn -Pe47 clean install