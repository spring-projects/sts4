#!/bin/bash
set -e
#if [ ! -d "node_modules/commons-vscode" ]; then
    ./scripts/preinstall.sh
#fi
npm install
npm run vsce-package