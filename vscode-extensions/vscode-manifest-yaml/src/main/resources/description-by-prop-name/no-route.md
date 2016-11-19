By default, `cf push` assigns a route to every application. But some applications process data while running in the background, and should not be assigned routes.

You can use the `no-route` attribute with a value of `true` to prevent a route from being created for your application.

```
---
  ...
  no-route: true
```

The command line option that corresponds to this attribute is `--no-route`.

If you find that an application which should not have a route does have one:

1.  Remove the route using the `cf unmap-route` command.
2.  Push the app again with the `no-route: true` attribute in the manifest or the `--no-route` command line option.