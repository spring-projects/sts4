#!/bin/bash
set -e

workdir=$(pwd)

cd client
npm install && npm run build

client_dir=$(pwd)

cd $workdir

static_dir=${workdir}/src/main/resources/static



rm -fr $static_dir
mkdir -p $static_dir
echo "cp ${client_dir}/bundle* $static_dir"
cp ${client_dir}/bundle* $static_dir
cp ${client_dir}/index.html $static_dir
cp ${client_dir}/eclipse.html $static_dir
cp -R ${client_dir}/css $static_dir/css
cp -R ${client_dir}/lib $static_dir/lib
cp -R ${client_dir}/src $static_dir/src

../mvnw \
    -f ../pom.xml \
    -pl sprotty-si-view \
    -am \
    clean install \
    -DskipTests
