Run steps in series.

	do: [step]

Simply performs the given steps serially, with the same semantics as if they were at the top level step listing.

This can be used to perform multiple steps serially in the branch of an `aggregate` step:

	plan:
	- aggregate:
	  - task: unit
	  - do:
	    - get: something-else
	    - task: something-else-unit