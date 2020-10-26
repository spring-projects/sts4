#!/bin/bash
set -e
version_override=99.99.99-SNAPSHOT
export JAVA_HOME=/home/kdvolder/Applications/jdk1.8.0_271
cd ~/git/kdvolder/docker-java
./mvnw versions:set -DnewVersion=${version_override}
./mvnw clean package -Dmaven.test.skip=true
for f in \
   docker-java-transport/target/docker-java-transport-${version_override}.jar \
   docker-java-transport-zerodep/target/docker-java-transport-zerodep-${version_override}.jar \
   docker-java-core/target/docker-java-core-${version_override}.jar \
   docker-java-api/target/docker-java-api-${version_override}.jar
do
  cp $f ~/git/sts4/eclipse-extensions/org.springframework.ide.eclipse.docker.client/dependency/
done
