#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: ./trigger-rc-build.sh ${RC_TAG}"
    echo "Where RC_TAG is one of RC1, RC2, etc."
fi

rc_tag=$1
workdir=`pwd`
extension_id=$(basename "$workdir")
version=`jq -r .version package.json`
tag=${extension_id}-${version}-${rc_tag}

echo "Tagging head as tag=$tag"
git tag $tag
echo "Pushing tag..."
git push origin $tag
