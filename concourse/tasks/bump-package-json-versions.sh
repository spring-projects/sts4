#!/bin/bash
set -e
workdir=`pwd`

sources=$workdir/sts4/vscode-extensions

version=`cat version/version`
echo "version=$version"

for extension_id in (vscode-manifest-yaml vscode-concourse commons-vscode)
do
    echo "Should update version of $extension_id to $version"
    echo "But this isn't implemented yet!"
    echo ""
done

exit 99

release_version=`jq -r .version ${sources}/package.json`

echo "release_version=$release_version"

cd $sources

echo "Bumping version of ${extension_id}"
echo "release_version=${release_version}"
npm version patch
git add .

new_version=`jq -r .version ${sources}/package.json`
echo "new_version=${new_version}"

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git commit \
    -m "Bump version of ${extension_id} to ${new_version}"

git clone $workdir/sts4 $workdir/sts4-out
