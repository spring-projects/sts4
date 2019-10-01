#!/bin/bash
set -e
set -v
workdir=`pwd`
output=$workdir/out

cd sts4
origin_url="$(git config --get remote.origin.url)"

cd $output

echo "Syncing original repo $origin_url' and mirror repo '$mirror_repo'"

git clone --mirror $origin_url original-repo
cd original-repo
git config user.email "aboyko@pivotal.io"
git config user.name "Alex Boyko"
git remote add sync $mirror_repo
git push sync --all
