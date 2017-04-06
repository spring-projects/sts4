#!/bin/bash
set -e
../mvnw -U -f ../pom.xml -pl concourse-language-server -am clean install
