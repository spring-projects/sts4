*Optional.* The cached directories shared between task runs.

On the task's first run, all cache directories will be empty. It is the responsibility of the task to populate these directories with any artifacts to be cached. On subsequent runs, the cached directories will contain those artifacts.

Caches are scoped to the worker the task is run on, so you will not get a cache hit when subsequent builds run on different workers. This also means that caching is not intended to share state between workers, and your task should be able to run whether or not the cache is warmed.

Caches are also scoped to a particular task name inside of a pipeline's job. As a consequence, if the job name, step name or cache path are changed, the cache will not be used. This also means that caches do not exist for one-off builds.

For example, the following task and script define a node project that takes advantage of task caches for its node modules:

	---
	platform: linux
	
	image_resource: # ...
	
	inputs:
	- name: project-src
	
	caches:
	- path: project-src/node_modules
	
	run:
	  path: project-src/ci/build
	
...assuming project-src/ci/build looks something like:

	#!/bin/bash
	
	set -e -u -x
	
	cd project-src
	npm install
	
	# ...

...this task would cache the contents of project-src/node_modules between runs of this task on the same worker.