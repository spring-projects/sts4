*Optional*. A map of template variables to pass to an external task. Only works with external tasks defined in [`file`](https://concourse-ci.org/task-step.html#task-step-file).

This is often used in combination with [`((vars))`](https://concourse-ci.org/setting-pipelines.html#pipeline-vars) in the pipeline.

For example:

```
plan:
- get: my-repo
- task: integration
  file: my-repo/ci/task.yml
  vars:
    text: ((text))
```

And `task.yml`:

```
---
platform: linux

image_resource:
  type: docker-image
  source:
    repository: my.local.registry:8080/my/image
    username: ((myuser))
    password: ((mypass))

run:
  path: echo
  args: ["((text))"]
```

This will resolve `"((text))"` to `"Hello World!"`, while `((myuser))` and `((mypass))` will be resolved at runtime via a [credential manager](https://concourse-ci.org/creds.html), if it has been configured.
