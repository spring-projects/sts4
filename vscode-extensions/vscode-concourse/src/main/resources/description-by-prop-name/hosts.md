Use the `hosts` attribute to provide multiple hostnames, or subdomains. Each hostname generates a unique route for the app. `hosts` can be used in conjunction with `host`. If you define both attributes, Cloud Foundry creates routes for hostnames defined in both `host` and `hosts`.

```
---
  ...
  hosts:
  - app_host1
  - app_host2
```

The command line option that overrides this attribute is `-n`.