Perform a step on certain workers.

Any step can be directed at a pool of workers for a given set of tags, by adding the `tags` attribute to it.

	tags: [string]

*Optional. Default* `[]`. The tags by which to match workers.

For example, if `[a, b]` is specified, only workers advertising the `a` and `b` tags (or any others) will be used for running the step.