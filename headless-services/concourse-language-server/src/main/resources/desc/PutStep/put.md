Pushes to the given [Resource](https://concourse-ci.org/concepts.html#resources). 
All artifacts collected during the plan's execution will be available in the working directory.

For example, the following plan fetches a repo using [get](https://concourse-ci.org/get-step.html) and pushes it to another repo (assuming `repo-develop` and `repo-master` are defined as `git` resources):

```
plan:
- get: repo-develop
- put: repo-master
  params:
    repository: repo-develop
```

When the `put` succeeds, the produced version of the resource will be immediately fetched via an implicit `get` step. This is so that later steps in your plan can use the artifact that was produced. The source will be available under whatever name `put` specifies, just like as with `get`.

So, if the logical name (whatever put specifies) differs from the concrete resource, you would specify resource as well, like so:

```
plan:
- put: resource-image
  resource: docker-image-resource
```

Additionally, you can control the settings of the implicit `get` step by setting get_params. For example, if you did not want a put step utilizing the `docker-image` resource type to download the image, you would implement your `put` step as such:

```
plan:
- put: docker-build
  params: build: git-resource
  get_params: skip_download: true
```

```
put: string
```

Required. The logical name of the resource being pushed. The pushed resource will be available under this name after the push succeeds.
