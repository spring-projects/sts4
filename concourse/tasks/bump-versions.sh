#!/bin/bash
set -e
set -v
workdir=`pwd`

vscode_sources=$workdir/sts4/vscode-extensions
atom_sources=$workdir/sts4/atom-extensions
theia_sources=$workdir/sts4/theia-extensions

version=`cat version/version`
echo "version=$version"

theia_version=`cat theia-version/version`
echo "theia-version=$theia_version"

# vscode extensions
cd $vscode_sources
for extension_id in $(ls -d vscode-*)
do
    cd $vscode_sources/$extension_id
    echo "Should update version of $extension_id to $version"
    npm version $version
    git add package.json
    echo ""
done

# atom extensions
cd $atom_sources
for extension_id in $(ls -d atom-*)
do
    if [ $extension_id != "atom-commons" ]; then
        cd $atom_sources/$extension_id
        echo "Should update version of $extension_id to $version"
        npm version $version
        git add package.json
        echo ""
    fi
done

# theia extensions
cd $theia_sources
yarn install lerna -g
for extension_id in $(ls -d theia-*)
do
    if [ $extension_id != "theia-commons" ]; then
        cd $theia_sources/$extension_id
        echo "Should update version of $extension_id to $theia_version"
        lerna version $theia_version --exact --no-git-tag-version --no-push --yes
        git add ./
        echo ""
    fi
done

cd $workdir/sts4/headless-services
$workdir/sts4/concourse/tasks/update-pom-versions.sh $version

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Bump version to ${version}"

git clone $workdir/sts4 $workdir/out
