# Concourse Pipeline and Task Editor for Visual Studio Code

This extension provides validation, content assist and documentation hovers
for editing [Concourse](https://concourse-ci.org/) Pipeline and Task configuration files.

## Usage

The Concourse editor automatically activates when the name of the  `.yml` file you are editing 
follows a certain pattern:

  - `**/*pipeline*.yml` | `**/pipeline/*.yml` | `**/*pipeline*.yaml` | `**/pipeline/*.yaml`: activates support for editing pipelines
  - `**/ci/**/tasks/*.yml` | `**/*task.yml` |  `**/ci/**/tasks/*.yaml` | `**/*task.yaml` : activates support for editing tasks.
  
You can also define your own patterns and map them to the language-ids `concourse-pipeline-yaml` 
or `concourse-task-yaml` by defining `files.associations` in workspace settings. 
See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

## Functionality

### Validation

As you type the text is parsed and checked for basic syntactic and structural correctness. Hover over
an error marker to see an explanation:

![Linting Screenshot][linting]

### Content assist

Having trouble remembering all the names of the attributes, and their spelling? Or can't remember
which resource properties to set in the `get` task params versus its `source` attributes? Or
don't remember what 'special' values are acceptable for a certain property? Content assist
to the rescue:

![Content Assist Screenshot][ca1]

![Content Assist Screenshot][ca2]

### Documentation Hovers

Having trouble remembering exactly what the meaning of each attribute is? Hover over an attribute and 
read its detailed documentation:

![Hover Docs Screenshot][hovers]

### Goto Symbol in File

Is your Pipeline yaml file getting larger and is it becoming harder to find a particular Job, Resource or
Resource Type declaration? The "Goto Symbol in File" command helps you quickly jump to a specific
definition.

Type `CTRL-SHIFT-O` to popup a list of all symbols in your current Pipeline file. Start typing a name 
(or portion thereof) to narrow down the list. Select a symbol to jump directly to its location in the
file.

![Goto Symbol Screenshot][goto_symbol]

### Goto/Peek Definition

Use "Goto Defition" or "Peek Definition" to quickly go (or peek) from a a Job- or Resource name 
to its corresponding definition.

![Peek Definition Screenshot][peek]

## Limitations

This Vscode Extension is still a work in progress. At the moment only a select few of the [built-in resource-types](https://concourse-ci.org/resource-types.html)
have been fully defined in the Editor's Schema. 

The resource-types that are already defined in the schema are:

 - git
 - docker-image
 - s3
 - pool
 - semver
 - time

For other resource-types content assist and checking is still very limited. We intend
to grow this list and provide a similar level of support for all of the built-in resource types in
the near future.

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

[linting]: https://raw.githubusercontent.com/spring-projects/sts4/98148c08b608ff365fb87b2de955d6833f7ee082/vscode-extensions/vscode-concourse/readme-imgs/linting.png
[ca1]:     https://raw.githubusercontent.com/spring-projects/sts4/98148c08b608ff365fb87b2de955d6833f7ee082/vscode-extensions/vscode-concourse/readme-imgs/content-assist-1.png
[ca2]:     https://raw.githubusercontent.com/spring-projects/sts4/98148c08b608ff365fb87b2de955d6833f7ee082/vscode-extensions/vscode-concourse/readme-imgs/content-assist-2.png
[hovers]:  https://raw.githubusercontent.com/spring-projects/sts4/98148c08b608ff365fb87b2de955d6833f7ee082/vscode-extensions/vscode-concourse/readme-imgs/hover.png
[peek]:    https://raw.githubusercontent.com/spring-projects/sts4/98148c08b608ff365fb87b2de955d6833f7ee082/vscode-extensions/vscode-concourse/readme-imgs/peek.png
[goto_symbol]: https://raw.githubusercontent.com/spring-projects/sts4/d095208cfb34b0f129e6b66d41d099955a712f81/vscode-extensions/vscode-concourse/readme-imgs/goto-symbol.png
