#!/bin/bash
set -e

# Build an rc .vsix file, ready to be uploaded to vscode marketplace.
#
# An RC is built in a similar way as a snaphsot, except that
# we build it with a unqualified version (i.e. no pre-release qualifier)
# This is so that, if this candidate is aproved for publication,
# then the vsix can be uploaded as is (i.e.
# without requiring another rebuild/repackaging to change its version)

workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id

version=`cat version/version`
echo "extension_id=${extension_id}"
echo "version=${version}"

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   exit -1
fi

cd "sts4/vscode-extensions/${extension_id}"
npm version "${version%-*}" || true
echo "Version set to " `npm version`
echo -e "\n\n*Version: ${version}*" >> README.md

# Build commons vscode
cd ${sources}/../commons-vscode
npm install

cd "$sources"
npm install ../commons-vscode
npm install
npm run vsce-package

#Because the RC was with a unqualified version...
#The RC version qualifier isn't automaticlaly present in the file name.
#So we must explicitly rename the file to include the RC version qualifier.
cp *.vsix $workdir/out/$extension_id-${version}.vsix
