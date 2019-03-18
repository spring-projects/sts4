#!/bin/bash
set -e
workdir=`pwd`

envsubst > ~/.npmrc << XXXXXX
//registry.npmjs.org/:_authToken=${npm_token}
XXXXXX

theia_tgz_files=`ls ${workdir}/s3-*-theia-*/theia-*.tgz`

for theia_tgz_file in $theia_tgz_files
do
    echo "****************************************************************"
    echo "*** Publishing : ${theia_tgz_file}"
    echo "****************************************************************"
    echo ""
    echo "We are running the following command:"
    echo ""
    echo "     npm publish ${theia_tgz_file} --access public"
    echo ""

    npm publish ${theia_tgz_file} --access public

done
