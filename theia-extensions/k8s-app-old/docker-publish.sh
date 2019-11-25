#!/bin/bash

# Script to build and publish theia-spring-boot docker image
# to docker-hub.

# The published images makes it slightly easier to run theia+boot support
# deployments in different contexts.

set -e 

docker_user=kdvolder
docker build -t ${docker_user}/theia-spring-boot .
docker push ${docker_user}/theia-spring-boot