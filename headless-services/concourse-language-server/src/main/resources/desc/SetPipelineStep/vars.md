*Optional* A map of template variables to pass to the pipeline config.

Note that variables set with this field will not propagate to tasks configured via task step file. If you want those variables to be determined at the time the pipeline is set, use task step vars as well.

*Example* Configuring static variables

A var may be statically passed like so:

```
plan:
- get: my-repo
- set_pipeline: configure-the-pipeline
  file: my-repo/ci/pipeline.yml
  vars:
    text: "Hello World!"
```

Any [Vars](https://concourse-ci.org/vars.html) in the pipeline config will be filled in statically using this field.

For example, if my-repo/ci/pipeline.yml looks like...:

```
resources:
- name: task-image
    type: docker-image
    source:
      repository: my.local.registry:8080/my/image
      username: ((myuser))
      password: ((mypass))
jobs:
- name: job
  plan:
  - get: task-image
  - task: do-stuff
    image: task-image
    config:
      platform: linux
      run:
        path: echo
        args: ["((text))"]
```

...this will resolve "((text))" to "Hello World!", while ((myuser)) and ((mypass)) will be left in the pipeline to be [fetched at runtime](https://concourse-ci.org/vars.html#dynamic-vars).