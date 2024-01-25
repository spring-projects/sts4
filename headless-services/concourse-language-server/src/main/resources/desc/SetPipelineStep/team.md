*Optional* By default, the `set_pipeline` step sets the pipeline for the same **team** that is running the build.

The `team` attribute can be used to specify another team.

Only the `main` **team** is allowed to set another team's pipeline. Any `team` other than the `main` **team** using the team attribute will error, unless they reference their own team.