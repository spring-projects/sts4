The name of the variable that will be added to the ["." var source](https://concourse-ci.org/vars.html#local-vars). This variable will only be accessible in the scope of the step - each iteration of the step gets its own scope.

If a variable of the same name already exists in the parent scope, a warning will be printed.