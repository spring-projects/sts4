#!/bin/bash
set -e
workdir=`pwd`

cd sts4/vscode-extensions
./build-all.sh

cd $workdir
vsix_files=`find sts4 -name "*.visx"`

for f in ${vsix_files}; do
    new_name = $(basename "$f" .vsix)-`date -u +%Y%m%d%H%M`
    mv "$f" "vsix_files/$new_name"
done
