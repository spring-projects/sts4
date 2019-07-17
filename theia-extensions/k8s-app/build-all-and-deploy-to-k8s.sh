#!/bin/bash
set -e

# Create k8s cluster using 'kind'
kind create cluster
export KUBECONFIG="$(kind get kubeconfig-path --name="kind")"
kubectl cluster-info

cd docker-boot-dev

# Build the 'theia-spring-boot' package
./create-theia-boot-pkg.sh

# Then build it into a docker image
docker build -t theia-spring-boot .

# If using a 'kind' cluster, we must upload the image to the cluster
kind load docker-image theia-spring-boot

cd ..

# Deploy to the cluster
./deploy_theia_k8s.sh

# Expose the app on localhost
echo "Exposing theia-spring-boot deployment on http://localhost:3000"
until kubectl port-forward deployment/theia-spring-boot 3000:3000; do
  echo "Problem forwarding port, still waiting for container to start?"
  sleep 10
  echo "Retrying"
done