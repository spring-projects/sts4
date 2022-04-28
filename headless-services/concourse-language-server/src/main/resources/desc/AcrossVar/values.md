The list of values that the [var](https://concourse-ci.org/across-step.html#schema.across_var.var) will iterate over when running the substep. If multiple vars are configured, all combinations of values across all vars will run.

The list of values may also be interpolated. For instance, you may use the [`load_var` step](https://concourse-ci.org/load-var-step.html) to first load a list of [*value* schema](https://concourse-ci.org/config-basics.html#schema.value) into a [local var](https://concourse-ci.org/vars.html#local-vars), and then iterate across that dynamic list of values.

