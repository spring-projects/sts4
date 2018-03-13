*Optional.* A list of glob patterns. The inverse of `paths`; changes 
to the specified files are ignored.

Note that if you want to push commits that change these files via a `put`,
the commit will still be "detected", as [`check` and `put` both introduce
versions](https://concourse-ci.org/pipeline-mechanics.html#collecting-versions).
To avoid this you should define a second resource that you use for commits
that change files that you don't want to feed back into your pipeline - think
of one as read-only (with `ignore_paths`) and one as write-only (which
shouldn't need it).
