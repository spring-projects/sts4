#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl bosh-language-server -am clean install
