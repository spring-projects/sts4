#!/bin/bash
timestamp=`date -u +%Y%m%d%H%M`

base_dir=`pwd`

cd sts4/vscode-extensions/${extension_id}
base_version=`jq -r .version package.json`

echo ${extension_id}-${base_version}-RC${timestamp} > ${base_dir}/out/tag