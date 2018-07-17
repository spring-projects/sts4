Any step can have `on_abort` tacked onto it, whose value is a second step to execute only if the parent step aborts.
If the attached step succeeds, the entire step is still `aborted`.

##Example: Cleanup on Abort

The following will perform the cleanup task only if the build is aborted while the unit task was running:

	plan:
	- get: foo
	- task: unit
	  file: foo/unit.yml
	  on_abort:
	    task: cleanup
	    file: foo/cleanup.yml
