Use the `domains` attribute to provide multiple domains. If you define both `domain` and `domains` attributes, Cloud Foundry creates routes for domains defined in both of these fields.

```
---
  ...
  domains:
  - domain-example1.com
  - domain-example2.org
```

The command line option that overrides this attribute is `-d`.