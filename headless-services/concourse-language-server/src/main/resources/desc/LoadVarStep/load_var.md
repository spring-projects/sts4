Note: The `load_var` step was introduced in Concourse v6.0.0. It is considered an experimental feature until its associated [RFC](https://github.com/concourse/rfcs/pull/27) is resolved.

*Required* Load the value for a var at runtime, making it available to subsequent steps as a build-local var named after the given identifier.

The following build plan uses a version produced by the semver resource as a tag:

```
plan:
- get: version
- load_var: version-tag
  file: version/version
- put: image
  params: {tag: ((.:version-tag))}
```