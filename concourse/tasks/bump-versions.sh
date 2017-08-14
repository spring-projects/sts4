#!/bin/bash
set -e
set -v
workdir=`pwd`

sources=$workdir/sts4/vscode-extensions

version=`cat version/version`
echo "version=$version"

cd $sources
for extension_id in $(ls -d vscode-*)
do
    cd $sources/$extension_id
    echo "Should update version of $extension_id to $version"
    npm version $version
    git add package.json
    echo ""
done

cd $workdir/sts4/headless-services
$workdir/sts4/concourse/tasks/update-pom-versions.sh $version

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Bump version to ${version}"

git clone $workdir/sts4 $workdir/out
