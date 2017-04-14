#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl boot-java-language-server -am clean install
