#!/bin/bash
set -e
set -v
workdir=`pwd`

url=`cat fatjar/url`

echo "The url of the fatjar is ${url}"

ls -la