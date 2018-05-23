#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/theia-extensions/$extension_id
server_id=${extension_id#theia-}
ext_folder=$sources/$server_id

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

cd "$sxt_folder"

timestamp=`date -u +%Y%m%d%H%M`

base_version=`jq -r .version package.json`
if [ "$dist_type" != release ]; then
    # for snapshot build, work the timestamp into package.json version qualifier
    qualified_version=${base_version}-${timestamp}
    npm version ${qualified_version}
    echo -e "\n\n*Version: ${qualified_version}*" >> README.md
else
    echo -e "\n\n*Version: ${base_version}-RELEASE*" >> README.md
fi

cd "$sources"
./build.sh
cd "$ext_folder"
yarn pack

# for release build we don't don't add version-qualifier to package.json
# So we must instead rename the file ourself to add a qualifier
if [ "$dist_type" == release ]; then
    tar_file=`ls *.tar`
    release_name=`git tag --points-at HEAD | grep ${extension_id}`
    echo "release_name=$release_name"
    if [ -z "$release_name" ]; then
        echo "Release Candidates must be tagged" >&2
        exit 1
    else
        mv $tar_file ${release_name}.tar
    fi
fi

cp *.tar $workdir/out
