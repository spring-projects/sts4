#!/bin/bash
set -e
./scripts/preinstall.sh
npm install
if [ "$1" = "pre-release" ]
then
  npm run vsce-pre-release-package
else
  npm run vsce-package
fi
rm -fr ~/.vscode/extensions/pivotal.vscode-manifest-yaml*
rm -fr ~/.vscode/extensions/.obsolete
code --install-extension vscode-*.vsix
