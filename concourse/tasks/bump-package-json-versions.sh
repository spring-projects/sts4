#!/bin/bash
set -e
workdir=`pwd`

sources=$workdir/sts4/vscode-extensions

version=`cat version/version`
echo "version=$version"

for extension_id in vscode-manifest-yaml vscode-concourse commons-vscode
do
    cd $sources/$extension_id
    echo "Should update version of $extension_id to $version"
    npm version $version
    git add package.json
    echo ""
done

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git commit \
    -m "Bump version of vscode extensions to ${version}"

git clone $workdir/sts4 $workdir/out
