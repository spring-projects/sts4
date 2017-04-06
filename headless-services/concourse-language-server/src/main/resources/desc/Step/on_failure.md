Any step can have `on_failure` tacked onto it, whose value is a second step to execute only if the parent step fails.

	on_failure: step

The step to execute when the parent step fails. If the attached step succeeds, the entire step is still failed.

The following will perform the attached task only if the first one fails:

	plan:
	- get: foo
	- task: unit
	  file: foo/unit.yml
	  on_failure:
	    task: alert
	    file: foo/alert.yml