#!/bin/bash
set -e
workdir=`pwd`

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzvf maven-cache/*.tar.gz -C ${HOME}
else
    echo "!!!No stored maven cache found!!! "
    echo "!!!This may slow down the build!!!"
fi

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
cp `find sts4/vscode-extensions -name "*.vsix"` vsix-files

