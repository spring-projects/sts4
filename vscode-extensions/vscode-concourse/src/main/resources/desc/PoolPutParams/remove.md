If set, we will remove the given lock from the pool. The value is
the same as `release`. This can be used for e.g. tearing down an environment,
or moving a lock between pools by using `add` with a different pool in a
second step.