*Required.* The expected set of inputs for the task.

This determines which artifacts will propagate into the task, as the [build plan](https://concourse-ci.org/build-plans.html) executes. If any specified inputs are not present, the task will end with an error, without running.
