#!/bin/bash

if [ ! -d "../vscode-extensions/$extension_id" ]; then
    echo "ERROR: ../vscode-extensions/$extension_id is not a directory"
fi

branch=`git rev-parse --abbrev-ref HEAD`

fly -t tools set-pipeline \
    --var "branch=${branch}" \
    --load-vars-from ${HOME}/.sts4-concourse-credentials.yml \
    -p "sts4-release-${branch}" \
    -c release-pipeline.yml
