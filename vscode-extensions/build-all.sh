#!/bin/bash
set -e

base_dir=`pwd`
cd ${base_dir}/commons-vscode
npm install

cd $base_dir
for i in vscode-* ; do
    cd ${base_dir}/${i}
    npm install ../commons-vscode
    npm install
    npm run vsce-package
done

