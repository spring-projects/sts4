The `env` block consists of a heading, then one or more environment variable/value pairs.

For example:

```
---
  ...
  env:
    RAILS_ENV: production
    RACK_ENV: production
```

`cf push` deploys the application to a container on the server. The variables belong to the container environment.

While the application is running, Cloud Foundry allows you to operate on environment variables.

*   View all variables: `cf env my-app`
*   Set an individual variable: `cf set-env my-app my-variable_name my-variable_value`
*   Unset an individual variable: `cf unset-env my-app my-variable_name my-variable_value`

Environment variables interact with manifests in the following ways:

*   When you deploy an application for the first time, Cloud Foundry reads the variables described in the environment block of the manifest, and adds them to the environment of the container where the application is deployed.
*   When you stop and then restart an application, its environment variables persist.