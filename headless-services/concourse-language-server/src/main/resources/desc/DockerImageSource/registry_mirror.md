*Optional.* A URL pointing to a docker registry mirror service.

Note: `registry_mirror` is ignored if `repository` contains an explicitly-declared
registry-hostname-prefixed value, such as `my-registry.com/foo/bar`, in which case
the registry cited in the `repository` value is used instead of the `registry_mirror`.