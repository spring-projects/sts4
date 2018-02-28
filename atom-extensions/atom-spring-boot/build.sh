#!/bin/bash
set -e
set -x
workdir=`pwd`
cd ../atom-commons
npm install
cd $workdir
npm install
apm link .
