*Optional.* A map of task parameters to set, overriding those configured in `config` or `file`. This is useful for passing in credentials or other configuration to the task from the pipeline.

For example:

	plan:
	- get: my-repo
	- task: integration
	  file: my-repo/ci/integration.yml
	  params:
	    REMOTE_SERVER: 10.20.30.40:8080
	    USERNAME: my-user
	    PASSWORD: my-pass

This is often used in combination with `{{parameters}}` in the pipeline.