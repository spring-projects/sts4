The `timeout` attribute defines the number of seconds Cloud Foundry allocates for starting your application.

For example:

```
---
  ...
  timeout: 80
```

You can increase the timeout length for very large apps that require more time to start. The default timeout is 60 seconds with an upper bound of 180 seconds.

**Note**: Administrators can set the upper bound of the `maximum_health_check_timeout` property to any value. Any changes to Cloud Controller properties in the deployment manifest require running `bosh deploy`.

The command line option that overrides the timeout attribute for the shell is `-t`. Manifest values still apply to applications pushed to the deployment.