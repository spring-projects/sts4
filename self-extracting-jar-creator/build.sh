#!/bin/bash
mvn clean package
cp target/*.jar ../eclipse-distribution/common/self-extracting-jar-creator.jar