#!/bin/bash
set -e
workdir=`pwd`

cd ${workdir}/sts4/headless-services
./mvnw -Dtycho.disableP2Mirrors=true -DskipTests package

timestamp=`date +%s`
tarfile=${workdir}/out/sts4-mvn-cache-${timestamp}.tar.gz
tar -czvf ${tarfile} -C ${HOME} .m2/repository
