*Optional.* The location of the resource. This varies by resource type, and is a black box to Concourse; it is blindly passed to the resource at runtime.

To use `git` as an example, the source may contain the repo URI, the branch of the repo to track, and a private key to use when pushing/pulling.

By convention, documentation for each resource type's configuration is in each implementation's `README`.

You can find the source for the resource types provided with Concourse at the [Concourse GitHub organization](https://github.com/concourse?query=-resource).