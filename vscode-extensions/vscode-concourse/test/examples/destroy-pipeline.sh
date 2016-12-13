#!/bin/bash
branch=`git rev-parse --abbrev-ref HEAD`
fly -t tools destroy-pipeline -p sts4-${branch}

