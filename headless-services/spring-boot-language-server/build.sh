#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl spring-boot-language-server -am clean install
