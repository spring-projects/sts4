Running Locally with Docker
===========================

See script `docker-build-and-run.sh`. Make sure to edit the `JAVA_PROJECT_DIR`
variable set in that script to something that exists on your machine.

Running via a deployment in K8s
===============================

1. Create our setup cluster and login kubectl

For example with `kind` you can do:

```
$ kind create cluster
Creating cluster "kind" ...
 ✓ Ensuring node image (kindest/node:v1.15.3) 🖼
 ✓ Preparing nodes 📦 
 ✓ Creating kubeadm config 📜 
 ✓ Starting control-plane 🕹️ 
 ✓ Installing CNI 🔌 
 ✓ Installing StorageClass 💾 
Cluster creation complete. You can now use the cluster with:

export KUBECONFIG="$(kind get kubeconfig-path --name="kind")"
kubectl cluster-info
```

2. Create docker image

See above under "Running Locally with Docker"

3. Make the image accessible to nodes on the cluster. E.g. push image to 
docker hub is one way.

```
docker push kdvolder/k8s-theia
```

4. Create deployment

```
kubectl create -f k8s-yaml/deployment.yml
```

5. Make deployment accessible from localhost

```
kubectl port-forward deployment/theia-deployment 3000:3000
```

6. Open theia-app on http://localhost:3000