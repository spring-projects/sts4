#!/bin/bash
set -e
../mvnw -U -f ../pom.xml -pl boot-java-language-server -am clean install
