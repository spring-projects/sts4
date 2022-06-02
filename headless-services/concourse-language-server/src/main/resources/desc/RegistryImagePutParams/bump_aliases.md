*Optional. Default `false`.* When set to `true` and `version` is specified
automatically bump alias tags for the version. For example, when pushing version
`1.2.3`, push the same image to the following tags:
- `1.2`, if 1.2.3 is the latest version of 1.2.x.
- `1`, if 1.2.3 is the latest version of 1.x.
- `latest`, if 1.2.3 is the latest version overall.

If `variant` is configured as `foo`, push the same image to the following tags:
- `1.2-foo`, if 1.2.3 is the latest version of 1.2.x with `foo`.
- `1-foo`, if 1.2.3 is the latest version of 1.x with `foo`.
- `foo`, if 1.2.3 is the latest version overall for `foo`