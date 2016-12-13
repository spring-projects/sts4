#!/bin/bash
branch=`git rev-parse --abbrev-ref HEAD`
fly -t tools set-pipeline --var "branch=${branch}" --load-vars-from ${HOME}/.sts4-concourse-credentials.yml -p sts4-${branch} -c pipeline.yml
