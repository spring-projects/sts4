#!/bin/bash
set -e
set -v
workdir=`pwd`
sources=$workdir/sts4/headless-services
output=$workdir/out
maven_out=$workdir/maven-out

if [ -d "maven-cache" ]; then
    echo "Prepopulating maven cache"
    tar xzf maven-cache/*.tar.gz -C ${HOME}
else
   echo "!!!No stored maven cache found!!! "
   echo "!!!This may slow down the build!!!"
fi

cd ${sources}
xvfb-run ./mvnw clean install -DargLine="-Dlsp.completions.indentation.enable=true -Dlsp.yaml.completions.errors.disable=true"

# Copy fatjars to `out` directory
timestamp=`date -u +%Y%m%d%H%M`
for i in `ls *-language-server/target/*-exec.jar`; do
    basename=$(basename $i)
    cp $i $output/${basename/SNAPSHOT/$timestamp}
done

ls -la $output

# Copy installed artefacts from local maven cache to `maven-out`

mkdir -p ${maven_out}/org/springframework
cp -R ~/.m2/repository/org/springframework/ide ${maven_out}/org/springframework

timestamp=`date +%s`
tarfile=${output}/headless-maven-out-${timestamp}.tar.gz
tar -czvf ${tarfile} -C ${maven_out} .