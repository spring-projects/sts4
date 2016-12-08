#!/bin/bash
set -e
workdir=`pwd`

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
mv sts4/vscode-extensions/vscode-*/target/*.vsix vsix-files/
