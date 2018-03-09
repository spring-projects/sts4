*Optional.* The base image of the container. This style of specifying the base image has the same effect as `image:` but uses Concourse resources to download the image. The contents of this field should be the same as a resource configuration in your pipeline (without the name).

The following example configures the task to use the `golang:1.6` Docker image:

	image_resource:
	  type: docker-image
	  source: {repository: golang, tag: "1.6"}

...and the following example uses an insecure private Docker registry with a username and password:

	image_resource:
	  type: docker-image
	  source:
	    repository: my.local.registry:8080/my/image
	    insecure_registries: ["my.local.registry:8080"]
	    username: myuser
	    password: mypass
	    email: x@x.com

You can use any resource that returns a filesystem in the correct format (a `/rootfs` directory and a `metadata.json` file in the top level) but normally this will be the [Docker Image resource](https://github.com/concourse/docker-image-resource). If you'd like to make a resource of your own that supports this please use that as a reference implementation for now.

If you want to use an artifact source within the plan containing an image, you must set the [image](https://concourse-ci.org/task-step.html#task-image) in the plan step instead.
