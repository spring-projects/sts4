#!/bin/bash
JAVA_PROJECT_DIR=/home/kdvolder/git/kdvolder/chatter

docker build -t kdvolder/k8s-theia .
docker run -it --init  -p 3000:3000 -v "${JAVA_PROJECT_DIR}:/home/project:cached" kdvolder/k8s-theia
