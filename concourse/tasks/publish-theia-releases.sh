#!/bin/bash
set -e
workdir=`pwd`

theia_tgz_files=`ls ${workdir}/s3-*-theia-*/theia-*.tgz`

for theia_tgz_file in $theia_tgz_files
do
    echo "****************************************************************"
    echo "*** Publishing : ${theia_tgz_file}"
    echo "****************************************************************"
    echo ""
    echo "We are running the following command:"
    echo ""
    echo "     yarn publish ${theia_tgz_file} --access public"
    echo ""

    yarn publish ${theia_tgz_file} --access public

done
