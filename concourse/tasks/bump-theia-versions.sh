#!/bin/bash
set -e
set -v
workdir=`pwd`

theia_sources=$workdir/sts4/theia-extensions

theia_version=`cat theia-version/version`
echo "theia-version=$theia_version"

# theia extensions
cd $theia_sources
yarn global add lerna
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

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git commit \
    -m "Bump Theia version to ${theia_version}"

git clone $workdir/sts4 $workdir/out
