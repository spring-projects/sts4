#!/bin/bash
set -e
workdir=`pwd`

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
cp sts4/vscode-extensions/vscode-manifest-yaml/*.vsix vsix-files/
cp sts4/vscode-extensions/vscode-boot-properties/*.vsix vsix-files/
