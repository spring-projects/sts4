#!/bin/bash
set -e
workdir=$(pwd)
vscode_java_url="https://marketplace.visualstudio.com/_apis/public/gallery/publishers/redhat/vsextensions/java/0.53.1/vspackage"

echo "workdir=${workdir}"

cp -R ${workdir}/sts4/concourse/theia-docker-image/* ${workdir}/output
for i in ${workdir}/*-vsix-*/*.vsix ; do
    echo "Adding plugin: $i"
    cp "$i" ${workdir}/output/plugins/
done

# Download vscode-java 0.53.1. Higher versions require vscode-client 0.40 or higher not available in Theia
curl ${vscode_java_url} > ${workdir}/output/plugins/vscode-java.vsix

ls -Rl ${workdir}/output

