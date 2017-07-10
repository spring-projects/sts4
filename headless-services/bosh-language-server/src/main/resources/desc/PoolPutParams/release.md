If set, we will release the lock by moving it from claimed to
unclaimed. The value is the path of the lock to release (a directory
containing `name` and `metadata`), which typically is just the step that
provided the lock (either a `get` to pass one along or a `put` to acquire).

Note: the lock must be available in your job before you can release it. In
other words, a `get` step to fetch metadata about the lock is necessary
before a `put` step can release the lock.