*Required.* The path to a directory to be cached.

Paths are relative to the working directory of the task. Absolute paths are not respected.

Note that this value must not overlap with any other caches in the same task. Each cache results in a new empty directory that your task can place artifacts in; if the path overlaps it'll clobber whatever files used to be there.