#!/bin/bash
set -ev
mkdir /home/project
git clone https://github.com/spring-projects/spring-petclinic.git /home/project
cd /home/project
./mvnw clean package
rm -fr target
rm -fr /tmp/clone-and-build.sh