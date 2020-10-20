#!/bin/bash
set -e
cd sts4/vscode-extensions/boot-dev-pack
echo "Invoking ovsx publish in dir $(pwd)"
ovsx publish -p $ovsx_token