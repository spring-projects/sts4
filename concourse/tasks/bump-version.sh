#!/bin/bash
set -e
workdir=`pwd`

echo "extension_id=$extension_id"

sources=$workdir/sts4/vscode-extensions/$extension_id
release_version=`jq -r .version ${sources}/package.json`

echo "release_version=$release_version"

cd $sources

echo "Bumping version of ${extension_id}"
echo "release_version=${release_version}"
npm version patch

new_version=`jq -r .version ${sources}/package.json`
echo "new_version=${new_version}"

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git commit \
    -m "Bump version of ${extension_id} to ${new_version}" \

git clone $workdir/sts4 $workdir/sts4-out
