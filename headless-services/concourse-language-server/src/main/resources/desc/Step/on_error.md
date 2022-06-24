A hook step to execute after the parent step if the parent step terminates abnormally 
in any way other than those handled by the [on_abort](https://concourse-ci.org/on-abort-hook.html#schema.on_abort) 
or [on_failure](https://concourse-ci.org/on-failure-hook.html#schema.on_failure). 
This covers scenarios as broad as configuration mistakes, temporary network issues with the workers, 
or running longer than a timeout.

##Example: Send a notification

```
plan:
- do:
  - get: ci
  - task: unit
    file: ci/unit.yml
  on_error:
    put: slack
```