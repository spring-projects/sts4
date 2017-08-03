#!/bin/bash
set -e
workdir=`pwd`
sources=$workdir/sts4/headless-services

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

cd sources
./mvnw test -DargLine="-Dlsp.lazy.completions.disable=true -Dlsp.completions.indentation.enable=true -Dlsp.yaml.completions.errors.disable=true"