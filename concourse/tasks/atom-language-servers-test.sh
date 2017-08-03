#!/bin/bash
set -e
set -v
workdir=`pwd`
sources=$workdir/sts4/headless-services
output=$workdir/out

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

cd ${sources}
./mvnw package -DargLine="-Dlsp.lazy.completions.disable=true -Dlsp.completions.indentation.enable=true -Dlsp.yaml.completions.errors.disable=true"

timestamp=`date -u +%Y%m%d%H%M`
for i in `ls *-language-server/target/*.jar`; do
    basename=$basename($i)
    cp $i $output/${basename/SNAPSHOT/$timestamp}
done

find . -name "*-language-server"