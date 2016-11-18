Use the `random-route` attribute to create a URL that includes the app name and random words. Use this attribute to avoid URL collision when pushing the same app to multiple spaces, or to avoid managing app URLs.

The command line option that corresponds to this attribute is `--random-route`.

```
---
  ...
  random-route: true
```