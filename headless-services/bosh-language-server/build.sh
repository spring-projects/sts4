#!/bin/bash
set -e
../mvnw -B -f ../pom.xml -pl bosh-language-server -am clean install
