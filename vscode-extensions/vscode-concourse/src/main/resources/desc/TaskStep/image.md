*Optional.* Names an artifact source within the plan containing an image to use for the task. This overrides any `image` or `image_resource` configuration present in the task configuration.

This is very useful when part of your pipeline involves building an image, possibly with dependencies pre-baked. You can then propagate that image through the rest of your pipeline, guaranteeing that the correct version (and thus a consistent set of dependencies) is used throughout your pipeline.

For example, here's a pipeline building an image in one job and propagating it to the next:

	resources:
	- name: my-project
	  type: git
	  source: {uri: https://github.com/my-user/my-project}
	
	- name: my-task-image
	  type: docker-image
	  source: {repository: my-user/my-repo}
	
	jobs:
	- name: build-task-image
	  plan:
	  - get: my-project
	  - put: my-task-image
	    params: {build: my-project/ci/images/my-task}
	
	- name: use-task-image
	  plan:
	  - get: my-task-image
	    passed: [build-task-image]
	  - get: my-project
	    passed: [build-task-image]
	  - task: use-task-image
	    image: my-task-image
	    file: my-project/ci/tasks/my-task.yml

This can also be used in the simpler case of explicitly keeping track of dependent images, in which case you just wouldn't have a job building it (`build-task-image` in the above example).