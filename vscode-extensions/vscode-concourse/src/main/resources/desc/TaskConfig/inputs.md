*Required.* The expected set of inputs for the task.

This determines which artifacts will propagate into the task, as the [build plan](https://concourse.ci/build-plans.html) executes. If any specified inputs are not present, the task will end with an error, without running.

Each input has the following attributes:

	name: string

Required. The logical name of the input.

	path: string

Optional. The path where the input will be placed. If not specified, the input's name is used.