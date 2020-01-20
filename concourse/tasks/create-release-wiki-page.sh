#!/bin/bash
set -e
workdir=`pwd`
vsix_files=$(find *-vsix -name "*.vsix")
page=$workdir/sts4-wiki/${wiki_page_file_name}.md

echo "" > $page
for vsix_file in ${vsix_files}
do
    echo "Processing $vsix_file"
    fname=$(basename $vsix_file)

    if [[ $page == *"Candidate"* ]]; then
       # Example url for snapshot or RC:
       #  https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/snapshots/vscode-spring-boot-1.15.0-RC.1.vsix
       url=https://s3-us-west-1.amazonaws.com/s3-test.spring.io/sts4/vscode-extensions/snapshots/$fname
    else
       url=https://dist.springsource.com/release/STS4/vscode/$fname
    fi
    echo "- [$fname]($url)" >> $page
done

echo "Vsix Wiki Page Generated:"
echo "----------------------------------------------"
cat $page
echo "----------------------------------------------"

cd $workdir/sts4-wiki
git config user.email "kdevolder@pivotal.io"
git config user.name "Kris De Volder"

git add .

git commit \
    -m "Update vscode latest downloads wiki page"

git clone $workdir/sts4-wiki $workdir/sts4-wiki-out
