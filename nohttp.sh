#!/bin/bash
set -e
VERSION=0.0.2.RELEASE
if [ ! -z ${bamboo_JDK8_HOME} ]; then
    # put java 8 on the path when running on bamboo
    echo "Running on bamboo. Put JDK on path: ${bamboo_JDK8_HOME}/bin"
    export PATH=${bamboo_JDK8_HOME}/bin/:${PATH}
fi
if [ ! -f nohttp-cli-${VERSION}.jar ]; then
    curl -s -O https://repo.maven.apache.org/maven2/io/spring/nohttp/nohttp-cli/${VERSION}/nohttp-cli-${VERSION}.jar
fi
java -jar nohttp-cli-${VERSION}.jar -w=nohttp-whitelist.txt -F nohttp-whitelist.txt \
    -F README-dev-env.md
