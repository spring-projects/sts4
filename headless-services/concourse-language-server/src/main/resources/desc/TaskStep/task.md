Executes a [Task](https://concourse-ci.org/concepts.html#tasks), either from a file fetched via the preceding steps, or with inlined configuration.

	task: string

Required. A freeform name for the task that's being executed. Common examples would be `unit` or `integration`.

If any task in the build plan fails, the build will complete with failure. By default, any subsequent steps will not be performed. You can perform additional steps after failure by adding a `on_failure` or `ensure` step.

For example, the following plan fetches a single repository and executes multiple tasks, using the `aggregate` step, in a build matrix style configuration:

	plan:
	- get: my-repo
	- aggregate:
	  - task: go-1.3
	    file: my-repo/go-1.3.yml
	  - task: go-1.4
	    file: my-repo/ci/go-1.4.yml

Only if both tasks succeed will the build go green.

When a task completes, the files in its declared outputs will be made available to subsequent steps. This allows those subsequent steps to process the result of a task. For example, the following plan pulls down a repo, makes a commit to it, and pushes the commit to another repo (the task must have an output called `repo-with-commit`):

	plan:
	- get: my-repo
	- task: commit
	  file: my-repo/commit.yml
	- put: other-repo
	  params:
	    repository: repo-with-commit

