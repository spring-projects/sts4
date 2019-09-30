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
cp ${client_dir}/*.html $static_dir
cp -R ${client_dir}/samples $static_dir/samples
cp -R ${client_dir}/css $static_dir/css
cp -R ${client_dir}/lib $static_dir/lib
cp -R ${client_dir}/src $static_dir/src
cp -R ${client_dir}/icon $static_dir/icon

../mvnw \
    -f ../pom.xml \
    -pl sprotty-si-view \
    -am \
    clean install \
    -DskipTests
