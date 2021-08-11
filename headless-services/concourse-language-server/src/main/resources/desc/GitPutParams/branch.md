*Optional.* The branch to push commits.

Note that the version produced by the `put` step will be picked up by subsequent `get` steps
even if the `branch` differs from the `branch` specified in the source.
To avoid this, you should use two resources of read-only and write-only.
