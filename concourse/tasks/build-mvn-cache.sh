#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/vscode-extensions/$extension_id

cd sts4/vscode-extensions
./mvnw -DskipTests package

cd ${workdir}/sts4/headless-services
./mvnw -DskipTests package

timestamp=`date +%s`
tarfile=${workdir}/out/sts4-mvn-cache-${timestamp}.tar.gz
tar -czvf ${tarfile} -C ${HOME} .m2/repository
