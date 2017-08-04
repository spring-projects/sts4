*Required*. This specifies instance update properties. These properties control how BOSH updates instances during the deployment.

See job [lifecycle](https://bosh.io/docs/job-lifecycle.html) for more details on startup/shutdown procedure within each VM.

Example:

	update:
	  canaries: 1
	  max_in_flight: 10
	  canary_watch_time: 1000-30000
	  update_watch_time: 1000-30000
