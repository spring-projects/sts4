*Optional*. Defaults to `latest`. The version of the resource to fetch.

If set to `latest`, scheduling will just find the latest available version of a resource and use it, allowing versions to be skipped. This is usually what you want, e.g. if someone pushes 100 git commits.

If set to `every`, builds will walk through all available versions of the resource. Note that if `passed` is also configured, it will only step through the versions satisfying the constraints.

If set to a specific version (e.g. `{ref: abcdef123}`), only that version will be used. Note that the version must be available and detected by the resource, otherwise the input will never be satisfied. You may want to use [check-resource](https://concourse-ci.org/fly-check-resource.html) to force detection of resource versions, if you need to use an older one that was never detected (as all newly configured resources start from the latest version).