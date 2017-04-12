Use the `health-check-type` attribute to set the `health_check_type` flag to either `port`, `process` or `http`. 
If you do not provide a `health-check-type` attribute, it defaults to `port`.

```
---
  ...
  health-check-type: port
```

The command line option that overrides this attribute is -u.

The value of `none` is deprecated in favor of `process`