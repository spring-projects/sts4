If your app is contained in a Docker image, then you may use the `docker` attribute to specify it and an optional Docker `username`.

This attribute is a combination of `push` options that include `--docker-image` and `--docker-username`.

```
---
  ...
  docker:
    image: docker-image-repository/docker-image-name
    username: docker-user-name
```

The command line option `--docker-image` or `-o` overrides `docker.image`. The command line option `--docker-username` overrides `docker.username`.

The manifest attribute `docker.username` is optional. If it is used, then the password must be provided in the environment variable `CF_DOCKER_PASSWORD`. Additionally, if a Docker username is specified, then a Docker image must also be specified.

