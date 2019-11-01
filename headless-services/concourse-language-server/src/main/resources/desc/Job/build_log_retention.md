*Optional*. Configures the retention policy for build logs. This is useful if you have a job that runs often but after
some amount of time the logs aren't worth keeping around.

The following fields may be specified: `days`, `builds`, `minimum_succeeded_builds`.

Builds which are not retained by one of the above configurations will have their logs reaped.

The following example will keep logs for any builds that have completed in the last 2 days, while also keeping the last 1000 builds, with at least one successful build.

    jobs:
    - name: smoke-tests
      build_log_retention:
        days: 2
        builds: 1000
        minimum_succeeded_builds: 1
      plan:
      - get: 10m
      - task: smoke-tests
        # ...

Note: if more than 1000 builds finish in the past 2 days, all of them will be retained thanks to the build_log_retention.days configuration. Similarly, if there are 1000 builds spanning more than 2 days, they will also be kept thanks to the build_log_retention.builds configuration. And if they all happened to have failed, the build_log_retention.minimum_succeeded_builds will keep around at least one successful build. All policies operate independently.
