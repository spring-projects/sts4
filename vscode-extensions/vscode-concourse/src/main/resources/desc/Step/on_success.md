Any step can have `on_success` tacked onto it, whose value is a second step to execute only if the parent step succeeds.

	on_success: step

The step to execute when the parent step succeeds. If the attached step fails, the outer step is considered to have failed.

The following will perform the second task only if the first one succeeds:

	plan:
	- get: foo
	- task: unit
	  file: foo/unit.yml
	  on_success:
	    task: alert
	    file: foo/alert.yml

Note that this is semantically equivalent to the following:

	plan:
	- get: foo
	- task: unit
	  file: foo/unit.yml
	- task: alert
	  file: foo/alert.yml

...however it is provided mainly for cases where there is an equivalent `on_failure`, and having them next to each other is more clear.