*Required.* The command to execute in the container.

Note that this is *not* provided as a script blob, but explicit `path` and `args` values; this allows `fly` to forward arguments to the script, and forces your config `.yml` to stay fairly small.