*One of `config` or `file` attributes is required.*

`file` points at a `.yml` file containing the task config, which allows this to be tracked with your resources.

The first segment in the path should refer to another source from the plan, and the rest of the path is relative to that source.

For example, if in your plan you have the following `get` step:

	- get: something

And the `something` resource provided a `unit.yml` file, you would set 

	file: something/unit.yml.
