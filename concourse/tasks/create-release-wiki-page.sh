#!/bin/bash
#######################################
## !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
## This script is not working yet, 
## Not tested, incomplete
## WIP
##########################################

set -e
set -v
workdir=`pwd`

vsix_files=$(find "$workdir" *-vsix -name "*.vsix") 
# version=`cat version/version`
# echo "version=$version"


page=$workdir/sts4-wiki/VSCode-Extensions-Downloads-Latest.md

echo "Current Vscode Extensions Downloads" > $page
echo "===================================" >> $page
echo "" >> $page
for vsix_file in ${vsix_files}
do
    fname=$(basename $vsix_files)
    echo "- [$fname](https://dist.springsource.com/release/STS4/vscode/$fname)" >> $page
done

echo "Vsix Downloads Page Generated:"
echo "----------------------------------------------"
cat $page
echo "----------------------------------------------"

git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Update vscode latest downloads wiki page"

git clone $workdir/sts4-wiki $workdir/sts4-wiki-out
