Some languages and frameworks require that you provide a custom command to start an application. Refer to the [buildpack](/buildpacks/) documentation to determine if you need to provide a custom start command.

You can provide the custom start command in your application manifest or on the command line.

To specify the custom start command in your application manifest, add it in the `command: START-COMMAND` format as the following example shows:


```
---
  ...
  command: bundle exec rake VERBOSE=true
```

On the command line, use the `-c` option to specify the custom start command as the following example shows:

```
$ cf push my-app -c "bundle exec rake VERBOSE=true"
```

**Note**: The `-c` option with a value of ‘null’ forces `cf push` to use the buildpack start command. See [About Starting Applications](./app-startup.html) for more information.

If you override the start command for a Buildpack application, Linux uses `bash -c YOUR-COMMAND` to invoke your application. If you override the start command for a Docker application, Linux uses `sh -c YOUR-COMMAND` to invoke your application. Because of this, if you override a start command, you should prefix `exec` to the final command in your custom composite start command.

`exec` causes the last command to become the root process of your application. The [Cloud Foundry Updates and Your Application](./prepare-to-deploy.html#moving-apps) section of the _Considerations for Designing and Running an Application in the Cloud_ topic explains why your application should handle a `termination signal` during Cloud Foundry updates. Without an `exec` statement, the parent process remains as the implied bash process, and does not propagate signals to your application process.

For example, both of the following composite start commands run database migrations when the first instance of the app starts, then start the app to serve requests, but they behave differently on graceful shutdown.

*   `bin/rake cf:on_first_instance db:migrate && bin/rails server -p $PORT -e $RAILS_ENV`: The process tree is `bash -> ruby`, so on graceful shutdown only the `bash` process receives the TERM signal, and not the `ruby` process.

*   `bin/rake cf:on_first_instance db:migrate && exec bin/rails server -p $PORT -e $RAILS_ENV`: Because of the `exec` prefix on the final command, the `ruby` process invoked by `rails` takes over the `bash` process managing the execution of the composite command. The process tree is only `ruby`, so the ruby web server receives the TERM signal can shutdown gracefully for 10 seconds.