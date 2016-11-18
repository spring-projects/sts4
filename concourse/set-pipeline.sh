#!/bin/bash
fly -t tools set-pipeline --load-vars-from credentials.yml -p sts4 -c pipeline.yml
