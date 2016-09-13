#!/bin/bash

## run this script to package up this extension using vsce tool.
## This assumes you have vsce tool installed.
## You can install it via npm

set -e # fail at the first sign of trouble

mvn clean package ## build language server fat jar
npm install
vsce package


