# Concourse Pipeline and Task Editor for Visual Studio Code

This extension provides validation, content assist and documentation hovers
for editing [Concourse](https://concourse.ci/) Pipeline and Task configuration files.

## Usage

The Concourse editor automatically activates when the name of the  `.yml` file you are editing 
follows a certain pattern:

  - `**/*pipeline*.yml` : activates support for editing pipelines
  - `**/tasks/*.yml` : activates support for editing tasks.
  
You can also define your own patterns and map them to the language-ids `concourse-pipeline-yaml` 
or `concourse-task-taml` by defining `files.associations` in workspace settings. 
See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

## Functionality

### Validation

As you type the text is parsed and checked for basic syntactic and structural correctness. Hover over
an error marker to see an explanation:

**TODO: screenshot(s)**

### Content assist

Having trouble remembering all the names of the attributes, and their spelling? Or can't remember
which resource properties to set in the `get` task params versus its `source` attributes? 
Content assist to the rescue:

**TODO: screenshot(s)**

### Documentation Hovers

Having trouble remembering exactly what the meaning of each attribute is? Hover over an attribute and 
read its detailed documentation:

**TODO: screenshot(s)**

## Limitations

This Vscode Extension is still a work in progress. At the moment only a select few of the [built-in resource-types](https://concourse.ci/resource-types.html)
have been fully defined in the Editor's Schema. 

The resource-types that are already defined in the schema are:

 - git
 - docker-image
 - s3
 - pool
 
For other resource-types content assist and checking is still very limited. We intend
to grow this list and provide a similar level of support for all of the built-in resource types in
the near future.

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

