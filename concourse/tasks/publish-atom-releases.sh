#!/bin/bash
set -e
workdir=`pwd`

atom_packages=`ls ${workdir}/atom-*`

for atom_package in $atom_packages
do
    echo "****************************************************************"
    echo "*** Publishing : ${atom_package}"
    echo "****************************************************************"
    echo ""
    echo "We are runing the following command:"
    echo ""
    echo "     apm publish -p vsce_token --packagePath $vsix_file"
    echo ""
    tag=v$(cat package.json | jq -r ".version")
    apm login --token $atom_token
    apm publish --tag $tag
done
