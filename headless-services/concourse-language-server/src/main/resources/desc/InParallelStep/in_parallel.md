Performs the given steps in parallel.

If any sub-steps in an `in_parallel` result in an error, the `in_parallel` step
as a whole is considered to have errored.

Similarly, when aggregating `task` steps, if any *fail*, the 
`in_parallel` step will fail. This is useful for build matrices:

```
plan:
- get: some-repo
- in_parallel:
  - task: unit-windows
    file: some-repo/ci/windows.yml
  - task: unit-linux
    file: some-repo/ci/linux.yml
  - task: unit-darwin
    file: some-repo/ci/darwin.yml
```

The `in_parallel` step is also useful for performing arbitrary steps in parallel, for the sake of speeding up the build. It is often used to fetch all dependent resources together:

```
plan:
- in_parallel:
  - get: component-a
  - get: component-b
  - get: integration-suite
- task: integration
  file: integration-suite/task.yml
```