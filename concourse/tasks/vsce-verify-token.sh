#!/bin/bash
set -e
cd sts4/vscode-extensions/vscode-concourse/
echo "publisher=$vsce_publisher"
vsce verify-pat -p "${vsce_token}" "$vsce_publisher"
