#!/bin/bash
#poms=`find . -name pom.xml -not -path "*/target/*"`
set -e
version=$1
poms=commons/pom.xml
for i in $poms; do
    ./mvnw -f $i versions:set -DnewVersion=${version}-SNAPSHOT
done
