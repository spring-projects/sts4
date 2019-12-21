#!/bin/bash
#######################################
## !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
## This script is not working yet, 
## Not tested, incomplete
## WIP
##########################################

set -e
set -v
workdir=`pwd`

vsix_files=$(find *-vsix -name "*.vsix") 
# version=`cat version/version`
# echo "version=$version"

# vscode extensions
wikidir=${workdir}/sts4
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

cd $workdir/sts4/headless-services
$workdir/sts4/concourse/tasks/update-pom-versions.sh $version

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Update vscode release wiki page"

git clone $workdir/sts4-wiki $workdir/sts4-wiki-out
