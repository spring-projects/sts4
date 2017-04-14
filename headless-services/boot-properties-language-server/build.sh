#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl boot-properties-language-server -am clean install
