#!/bin/bash
docker build -t kdvolder/k8s-theia .
docker run -it -p 3000:3000 kdvolder/k8s-theia