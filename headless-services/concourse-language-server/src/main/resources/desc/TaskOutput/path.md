*Optional.* The path to a directory where the output will be taken from. If not specified, the output's `name` is used.

Note that this value must not overlap with any other inputs or outputs. Each output results in a new empty directory that your task should place artifacts in; if the path overlaps it'll clobber whatever files used to be there.

