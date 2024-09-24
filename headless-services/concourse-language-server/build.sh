#!/bin/bash
set -e
../mvnw -B -f ../pom.xml -pl concourse-language-server -am clean install
