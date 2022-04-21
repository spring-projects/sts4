#!/bin/bash
set -e
cd sts4/vscode-extensions/vscode-concourse/
echo "pat=$vsce_token"
vsce verify-pat -p "${vsce_token}" Pivotal
