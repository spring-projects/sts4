Every `cf push` deploys applications to one particular Cloud Foundry instance. Every Cloud Foundry instance may have a shared domain set by an admin. Unless you specify a domain, Cloud Foundry incorporates that shared domain in the route to your application.

You can use the `domain` attribute when you want your application to be served from a domain other than the default shared domain.

```
---
  ...
  domain: unique-example.com
```

The command line option that overrides this attribute is `-d`.

### The domains attribute

Use the `domains` attribute to provide multiple domains. If you define both `domain` and `domains` attributes, Cloud Foundry creates routes for domains defined in both of these fields.

```
---
  ...
  domains:
  - domain-example1.com
  - domain-example2.org
```

The command line option that overrides this attribute is `-d`.