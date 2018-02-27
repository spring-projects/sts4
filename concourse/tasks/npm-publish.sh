#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sources_repo/${sources_dir}
cd $sources

envsubst > ~/.npmrc << XXXXXX
//registry.npmjs.org/:_authToken=${npm_token}
XXXXXX

npm install
npm publish
