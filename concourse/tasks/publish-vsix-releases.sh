#!/bin/bash
set -e
workdir=`pwd`
vsix_files=`ls ${workdir}/s3-*/*.vsix`

for vsix_file in $vsix_files
do
    echo vsix_file=${vsix_file}
    echo "We should be runing the following command!"
    echo "But this is just a practice run..."
    echo "vsce publish -p $vsce_token --packagePath $vsix_file"
done

exit 99

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
