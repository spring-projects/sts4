#!/bin/bash
set -e
workdir=$(pwd)
cp -R ${workdir}/sts4/concourse/theia-docker-image/* ${workdir}/output
for i in ${workdir}/*-vsix-*/*.vsix ; do
    cp "$i" ${workdir}/output/plugins/
done

ls -Rl ${workdir}/output

