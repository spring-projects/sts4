You can use the `path` attribute to tell Cloud Foundry where to find your application. This is generally not necessary when you run `cf push` from the directory where an application is located.

```
---
  ...
  path: path_to_application_bits
```

The command line option that overrides this attribute is `-p`.