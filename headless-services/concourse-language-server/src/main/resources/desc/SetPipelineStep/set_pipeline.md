Note: The `set_pipeline` step was introduced in Concourse v5.8.0. It is considered an experimental feature until its associated [RFC](https://github.com/concourse/rfcs/pull/31) is resolved.

*Required* The identifier specifies the name of the pipeline to configure. It will be configured within the current team and be created unpaused.

This is a way to ensure a pipeline stays up to date with its definition in a source code repository, eliminating the need to manually run fly set-pipeline

```
resources:
- name: booklit
  type: git
  source: {uri: https://github.com/vito/booklit}
jobs:
- name: reconfigure
  plan:
  - get: booklit
    trigger: true
  - set_pipeline: booklit
    file: booklit/ci/pipeline.yml
```
