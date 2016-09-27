#!/bin/bash

## run this script to package up this extension using vsce tool.
## This assumes you have vsce tool installed.
## You can install it via npm

set -e # fail at the first sign of trouble

#Ensure commons are built and uptodate in local maven cache
mvn -f ../commons/pom.xml clean install
mvn clean package
npm install
vsce package


