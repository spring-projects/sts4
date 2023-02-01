#!/bin/bash
set -e
./scripts/preinstall.sh
npm install
if [ "$1" = "release" ]
then
  npm run vsce-release-package
else
  npm run vsce-pre-release-package
fi
rm -fr ~/.vscode/extensions/pivotal.vscode-concourse*
rm -fr ~/.vscode/extensions/.obsolete
code --install-extension vscode-concourse-*.vsix
