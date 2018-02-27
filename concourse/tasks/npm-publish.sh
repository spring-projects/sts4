#!/bin/bash
set -e
set -x
workdir=`pwd`
sources=$workdir/sources_repo/${sources_dir}
cd $sources

envsubst > ~/.npmrc << XXXXXX
unsafe-perm=true
//registry.npmjs.org/:_authToken=${npm_token}
XXXXXX

npm install --unsafe-perm
npm publish --unsafe-perm
