*Optional.* Default `false`. When the `build` parameter is set,
first pull `image:tag` from the Docker registry (so as to use cached
intermediate images when building). This will cause the resource to fail
if it is set to `true` and the image does not exist yet.
Note: Since docker 1.10 docker images [do not contain all necessary metadata to
restore the build cache](https://github.com/docker/docker/issues/20316).
Additional metadata needs to be saved and re-applied after a docker pull to have 
subsequent builds skip identical intermediate layers. This additional
metadata is stored as a very small separate image (`image:${cache_tag}-buildcache`)
in the repository of this resource.