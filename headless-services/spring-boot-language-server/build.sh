#!/bin/bash
set -e
../mvnw \
    -Dmaven.test.skip=true \
    -f ../pom.xml \
    -pl spring-boot-language-server \
    -am \
    clean install
