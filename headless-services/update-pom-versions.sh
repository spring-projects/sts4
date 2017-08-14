#!/bin/bash
#poms=`find . -name pom.xml -not -path "*/target/*"`
poms=commons/pom.xml
for i in $poms; do
    ./mvnw -f $i versions:set -DnewVersion=0.0.9-SNAPSHOT
done
