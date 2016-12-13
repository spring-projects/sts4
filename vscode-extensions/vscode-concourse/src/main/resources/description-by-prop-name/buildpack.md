If your application requires a custom buildpack, you can use the `buildpack` attribute to specify its URL or name:

```
---
  ...
  buildpack: buildpack_URL
```

**Note**: The `cf buildpacks` command lists the buildpacks that you can refer to by name in a manifest or a command line option.

The command line option that overrides this attribute is `-b`.
