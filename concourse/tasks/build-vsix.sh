#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id

cd ${sources}/../commons-vscode
npm install

cd "$sources"

npm install ../commons-vscode

timestamp=`date -u +%Y%m%d%H%M`

if [ "$dist_type" != release ]; then
    # for snapshot build, work the timestamp into package.json version qualifier
    base_version=`jq -r .version package.json`
    qualified_version=${base_version}-${timestamp}
    npm version ${qualified_version}
    echo -e "\n*Version: ${qualified_version}*" >> README.md
fi

npm install
npm run vsce-package

# for release build we don't don't add version-qualifier to package.json
# So we must instead rename the file ourself to add a qualifier
if [ "$dist_type" == release ]; then
    vsix_file=`ls *.vsix`
    release_name=`git describe --tags`
    echo "release_name=$release_name"
    if [ -z "$release_name" ]; then
        echo "Release Candidates must be tagged" >&2
        exit 1
    else
        mv $vsix_file ${release_name}.vsix
    fi
fi

cp *.vsix $workdir/out
