#!/bin/bash
set -e

# Build an rc .tar.gz file, ready to be published to NPM.
#
# An RC is built in a similar way as a snaphsot, except that
# we package it as a tar.gz file
# This is so that, if this candidate is aproved for publication,
# then the tar.gz can be published to NPM (i.e.
# without requiring another rebuild/repackaging to change it)

workdir=`pwd`
sources=$workdir/sts4/theia-extensions/theia-$extension_id
ext_sources=$workdir/sts4/theia-extensions/theia-$extension_id/$extension_id

version=`cat theia-version/theia-version`
echo "extension_id=${extension_id}"
echo "version=${version}"

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   exit -1
fi

cd ${ext_sources}
npm version "${version%-*}" || true
echo "Version set to " `npm version`

cd ${sources}
./build.sh

cd ${ext_sources}
yarn pack

#Because the RC was with a unqualified version...
#The RC version qualifier isn't automaticlaly present in the file name.
#So we must explicitly rename the file to include the RC version qualifier.
cp *.tgz $workdir/out/theia-$extension_id-${version}.tgz
