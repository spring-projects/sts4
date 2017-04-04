#!/bin/bash
set -e

base_dir=`pwd`

timestamp=`date -u +%Y%m%d%H%M`

cd ${base_dir}/commons-vscode
npm install

cd $base_dir
for i in vscode-boot-properties vscode-manifest-yaml vscode-boot-java; do
    cd ${base_dir}/${i}
    echo "***************************************************************************************"
    echo "***************************************************************************************"
    echo "***************************************************************************************"
    echo "***** BUILDING: " $i
    echo "***************************************************************************************"
    echo "***************************************************************************************"
    echo "***************************************************************************************"
    
    npm install ../commons-vscode

    base_version=`jq -r .version package.json`
    qualified_version=${base_version}-${timestamp}
    echo -e "\n\n*Version: ${qualified_version}*" >> README.md
    npm version ${qualified_version}
    npm install
    npm run vsce-package
done

