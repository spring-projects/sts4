By default, if you do not provide a hostname, the URL for the app takes the form of `APP-NAME.DOMAIN`. If you want to override this and map the root domain to this app then you can set no-hostname as true.

```
---
  ...
  no-hostname: true
```

The command line option that corresponds to this attribute is `--no-hostname`.