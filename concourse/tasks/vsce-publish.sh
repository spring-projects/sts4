#!/bin/bash
set -e
workdir=`pwd`

vsix_file=`ls ${workdir}/vsix-resource/*.vsix`

echo "vsix_file=$vsix_file"
echo "extension_id=$extension_id"

sources=$workdir/sts4/vscode-extensions/$extension_id

release_version=`jq -r .version ${sources}/package.json`

echo "release_version=$release_version"

vsce publish -p $vsce_token --packagePath "$vsix_file"

echo "****************************************************************"
echo "****************************************************************"
echo "****************************************************************"
echo "*** $extension_id $version published to vscode marketplace *****"
echo "****************************************************************"
echo "****************************************************************"
echo "****************************************************************"

cd $sources

echo "Tagging release"
git tag -f ${extension_id}-${release_version}-RELEASE
git clone $workdir/sts4 $workdir/sts4-out
