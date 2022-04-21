#!/bin/bash
set -e
cd sts4/vscode-extensions/vscode-concourse/
vsce verify-pat -p "${vsce_token}" Pivotal
