#!/bin/bash
set -e
../mvnw -f ../pom.xml -pl concourse-language-server -Pnative -am clean install
