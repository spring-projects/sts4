#!/bin/bash

update_package_json() {
    cd $1
    tmp=$(mktemp)
    jq_cmd=".dependencies[\"@pivotal-tools/theia-${2}\"] = \"${3}\""
    jq "${jq_cmd}" package.json > "$tmp" && mv "$tmp" package.json
    npm version $3
}

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

cd "$ext_folder"

timestamp=`date -u +%Y%m%d%H%M`

base_version=`jq -r .version package.json`

cd "$sources"
./build.sh

# for snapshot build, work the timestamp into package.json version qualifier
qualified_version=${base_version}-${timestamp}
echo "Version: ${qualified_version}"
lerna version ${qualified_version} --exact --no-git-tag-version --no-push --yes

cd "$ext_sources"
echo -e "\n\n*Version: ${qualified_version}*" >> README.md

cd "$ext_folder"
tar_file=$extension_id-v$qualified_version.tgz
yarn pack --filename $tar_file

cp $tar_file $workdir/out

