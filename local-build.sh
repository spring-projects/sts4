#!/bin/bash
#export JAVA_HOME=/home/kdvolder/Applications/jdk-11.0.8+10
set -e
workdir=$(pwd)
cd $workdir/eclipse-language-servers
./local-build.sh
cd $workdir/eclipse-distribution
./local-build.sh
