Fetches a resource, making it available to subsequent steps via the given name.

For example, the following plan fetches a version number via the `semver` resource, bumps it to the next release candidate, and `put`s it back.

```
plan:
- get: version
  params:
    bump: minor
    rc: true
- put: version
  params:
    version: version/number
```

```
get: string
```

*Required.* The logical name of the resource being fetched. This name satisfies logical inputs to a [Task](https://concourse-ci.org/concepts.html#tasks), and may be referenced within the plan itself (e.g. in the `file` attribute of a `task` step).

