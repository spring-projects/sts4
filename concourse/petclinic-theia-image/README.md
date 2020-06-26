Theia + Spring Petclinic layer
=============================

To build this docker image:

```
docker build -t kdvolder/theia-petclinic .
```

To run this

```
docker run -it --init  -p 3000:3000 -p 8080:8080 kdvolder/theia-petclinic:latest
```