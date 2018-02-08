#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl boot-language-server -am clean install
