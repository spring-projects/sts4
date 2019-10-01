#!/bin/bash
set -e
set -v
workdir=`pwd`
output=$workdir/out

cd sts4
origin_url="$(git config --get remote.origin.url)"

cd $output

echo "Syncing original repo $origin_url' and mirror repo '$mirror_repo'"

mkdir original-repo
# enable hidden files for * matcher
shopt -s dotglob
cp -a $workdir/sts4/* original-repo
# disable hidden files for * matcher
shopt -u dotglob

cd original-repo
git config user.email "aboyko@pivotal.io"
git config user.name "Alex Boyko"
git remote add sync $mirror_repo
git push sync --all
