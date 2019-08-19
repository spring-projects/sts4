*Optional. Default is no limit*. A sempahore which limits the parallelism when executing the steps in a 
`in_parallel` step. When set, the number of running steps will not exceed the limit.

When not specified `in_parallel` will execute all steps immediately, making the default behavior 
identical to [aggregate](https://concourse-ci.org/aggregate-step.html).

