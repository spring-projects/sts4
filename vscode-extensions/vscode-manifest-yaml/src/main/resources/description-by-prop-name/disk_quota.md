Use the `disk_quota` attribute to allocate the disk space for your app instance. This attribute requires a unit of measurement: `M`, `MB`, `G`, or `GB`, in upper case or lower case.

```
---
  ...
  disk_quota: 1024M
```

The command line option that overrides this attribute is `-k`.