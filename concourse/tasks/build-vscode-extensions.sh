#!/bin/bash
set -e
workdir=`pwd`

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
cp `find sts4/vscode-extensions -name "*.vsix"` vsix-files

