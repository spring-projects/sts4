#!/bin/bash
set -e

workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id

version= `cat version/version`
echo "extension_id=${extension_id}"
echo "version=${version}"

cd "sts4/vscode-extensions/${extension_id}"
npm version "${version}"
echo "Version set to " `npm version`
echo -e "\n\n*Version: ${version}*" >> README.md

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   exit -1
fi

# Build commons vscode
cd ${sources}/../commons-vscode
npm install

cd "$sources"
npm install ../commons-vscode
npm install
npm run vsce-package

cp *.vsix $workdir/out
