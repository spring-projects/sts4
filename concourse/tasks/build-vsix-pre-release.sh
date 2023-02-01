#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id
server_id=${extension_id#vscode-}

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

#cd ${sources}/../commons-vscode
#npm install

cd "$sources"

#npm install ../commons-vscode

timestamp=`date -u +%Y%m%d%H%M`

version=`jq -r .version package.json`
echo -e "\n\n*Version: ${version}-PRE-RELEASE*" >> README.md

./scripts/preinstall.sh
npm install
#npm audit
npm run vsce-pre-release-package

cp *.vsix $workdir/out
server_jar=$workdir/sts4/headless-services/${server_id}-language-server/target/*-exec.jar
if [ -f $server_jar ]; then
    cp $server_jar $workdir/out/${server_id}-language-server-${version}-${timestamp}.jar
fi
