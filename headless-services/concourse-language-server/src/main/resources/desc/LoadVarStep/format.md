*Optional*. The format of the file's content.

If unset, Concourse will try to detect the format from the file extension. If the file format cannot be determined, Concourse will fallback to `trim`.

If set to `json`, `yaml`, or `yml`, the file content will be parsed accordingly and the resulting structure will be the value of the var.

If set to `trim`, the var will be set to the content of the file with any trailing and leading whitespace removed.

If set to `raw`, the var will be set to the content of the file without modification (i.e. with any existing whitespace).

**Example**: Loading a var with multiple fields.

Let's say we have a task, `generate-creds`, which produces a `generated-user` output containing a `user.json` file like so:

```
{
  "username": "some-user",
  "password": "some-password"
}
```

We could pass these credentials to subsequent steps by loading it into a var with `load_var`, which will detect that it is in JSON format based on the file extension:

```
plan:
- task: generate-creds
- load_var: user
  file: generated-user/user.json
- task: use-creds
  params:
    USERNAME: ((.:user.username))
    PASSWORD: ((.:user.password))
```
    
If the `use-creds` task were to print these values, they would be automatically redacted unless `reveal: true` is set.