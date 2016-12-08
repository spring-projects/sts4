#!/bin/bash
set -e
workdir=`pwd`

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
mv sts4/vscode-extensions/vscode-manifest-yaml/target/*.vsix vsix-files/
mv sts4/vscode-extensions/vscode-boot-properties/target/*.vsix vsix-files/
