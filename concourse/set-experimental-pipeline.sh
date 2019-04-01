#!/bin/bash

branch=`git rev-parse --abbrev-ref HEAD`

fly -t tools set-pipeline \
    --var "branch=${branch}" \
    -p "sts4-experimental-${branch}" \
    -c experimental-pipeline.yml
