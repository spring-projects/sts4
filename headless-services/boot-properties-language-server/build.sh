#!/bin/bash
set -e
../mvnw -U -f ../pom.xml -pl boot-properties-language-server -am clean install
