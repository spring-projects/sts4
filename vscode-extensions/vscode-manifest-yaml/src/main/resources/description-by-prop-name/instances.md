Use the `instances` attribute to specify the number of app instances that you want to start upon push:

```
---
  ...
  instances: 2
```

We recommend that you run at least two instances of any apps for which fault tolerance matters.

The command line option that overrides this attribute is `-i`.