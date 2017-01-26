*Optional.* The artifacts produced by the task.

Each output configures a directory to make available to later steps in the [build plan](https://concourse.ci/build-plans.html). The directory will be automatically created before the task runs, and the task should place any artifacts it wants to export in the directory.

Each output has the following attributes:

	name: string

*Required.* The logical name of the output. The contents under `path` will be made available to the rest of the plan under this name.

	path: string

*Optional.* The path to a directory where the output will be taken from. If not specified, the output's `name` is used.

Note that this value must not overlap with any other inputs or outputs. Each output results in a new empty directory that your task should place artifacts in; if the path overlaps it'll clobber whatever files used to be there.

For example, the following task and script would be used to propagate a built binary to later steps:

	---
	platform: linux
	
	image_resource: # ...
	
	inputs:
	- name: project-src
	
	outputs:
	- name: built-project
	
	run:
	  path: project-src/ci/build
	  
...assuming `project-src/ci/build` looks something like:

	#!/bin/bash
	set -e -u -x
	export GOPATH=$PWD/project-src
	go build -o built-project/my-project github.com/concourse/my-project
	
...this task could then be used in a build plan like so:

	plan:
	- get: project-src
	- task: build-bin
	  file: project-src/ci/build.yml
	- put: project-bin
	  params: file: built-project/my-project
