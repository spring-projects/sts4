# Theia Spring Boot Extension on K8s

### Remote Debug Boot LS Running in K8s
Spring Boot LS Java application may need to be debugged while running in K8s pod environment. In order to facilitate remote debugging of Spring Boot LS process the following setup is required:
1. Add or uncomment Boot LS launch JVM arguments enabling remote debugging. Open `theia-spring-boot/spring-boot/node/spring-boot-ls-contribution.ts` file, find `jvmArguments` property which is an array of `string` JVM arguments. Add `-Xdebug` and `-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=7999` arguments.
2. Build `theia-spring-boot` docker image using scripts in `k8s-app/docker-boot-dev` folder (i.e. build `theia-spring-boot` NPM package TGZ file then create docker image using `Dockefile` in the folder)
3. Deploy the created docker image on K8s using `k8s-app/deploy.yml` file or `k8s-app/deploy_theia_k8s.sh` script. Ensure that `k8s-app/deploy.yml` has the following in Service block `ports`:
```
  ports:
  - name: theia
    port: 80
    targetPort: 3000
  - name: debug
    nodePort: 30031
    port: 7999
    protocol: TCP
    targetPort: 7999

``` 
Also it may need a `containerPort` under `theia-spring-boot` container:
```
          ports:
          - containerPort: 3000
          - containerPort: 7999

```

Note that K8s container will have remote debugging connection available at port `7999` which will be exposed to the outside world on the `30031` port. Therefore, when attempting to attch remote debugger from local IDE port `30031` should be used.