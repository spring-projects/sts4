#!/bin/bash
set -e
modules=spring-boot-language-server,:org.springframework.tooling.jdt.ls.extension,:org.springframework.tooling.jdt.ls.commons.test
cd ../jdt-ls-extension
if command -v xvfb-run ; then
    echo "Using xvfb to run in headless environment..."
    xvfb-run ../mvnw \
        -f ../pom.xml \
        -pl $modules \
        -am \
        clean install
else
    ../mvnw \
        -f ../pom.xml \
        -pl $modules \
        -am \
        clean install
fi
