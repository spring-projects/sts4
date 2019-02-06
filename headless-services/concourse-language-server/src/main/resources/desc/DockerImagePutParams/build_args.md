*Optional.*  A map of Docker build-time variables. These will be available as environment variables during the Docker build.

While not stored in the image layers, they are stored in image metadata and so it is recommend to avoid using these to pass secrets into the build context. In multi-stage builds ARGs in earlier stages will not be copied to the later stages, or in the metadata of the final stage.

The [build metadata](https://concourse-ci.org/implementing-resources.html#resource-metadata) environment variables provided by Concourse will be expanded in the values (the syntax is `$SOME_ENVVAR` or `${SOME_ENVVAR}`).

Example:

```
build_args:
  DO_THING: true
  HOW_MANY_THINGS: 2
  EMAIL: me@yopmail.com
  CI_BUILD_ID: concourse-$BUILD_ID
```
