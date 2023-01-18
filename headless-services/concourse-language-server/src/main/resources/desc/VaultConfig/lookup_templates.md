*Optional.* Default `["/{{.Team}}/{{.Pipeline}}/{{.Secret}}", "/{{.Team}}/{{.Secret}}"]`.

A list of path templates to be expanded in a team and pipeline context subject to the `path_prefix` and `namespace`.

See [Changing the path templates](https://concourse-ci.org/vault-credential-manager.html#vault-lookup-templates) for more information.