#!/bin/bash
set -e
workdir=$(pwd)

echo "workdir=${workdir}"

cp -R ${workdir}/sts4/concourse/theia-docker-image/* ${workdir}/output
for i in ${workdir}/*-vsix-*/*.vsix ; do
    echo "Adding plugin: $i"
    cp "$i" ${workdir}/output/plugins/
done

ls -Rl ${workdir}/output
