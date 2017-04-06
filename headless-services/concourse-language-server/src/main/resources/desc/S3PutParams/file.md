*Required.* Path to the file to upload, provided by an output of a task.
If multiple files are matched by the glob, an error is raised. The file which
matches will be placed into the directory structure on S3 as defined in `regexp`
in the resource definition. The matching syntax is bash glob expansion, so
no capture groups, etc.