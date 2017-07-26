# Bosh Deployment Manifest Editor for Visual Studio Code

This extension provides validation, content assist and documentation hovers
for editing [Bosh](https://bosh.io/) Deployment Manifest files.

## Functionality

### Validation

As you type the text is parsed and checked for basic syntactic and structural correctness. Hover over
an error marker to see an explanation:

![Linting Screenshot][linting]

### Content assist

Having trouble remembering all the names of the attributes, and their spelling? Or can't remember
the exact name/version of the stemcell you just uploaded to your bosh environment? Content assist
to the rescue:

![Content Assist Screenshot][ca1]

![Content Assist Screenshot][ca2]

### Documentation Hovers

Having trouble remembering exactly what the meaning of each attribute is? Hover over an attribute and 
read its detailed documentation:

![Hover Docs Screenshot][hovers]

### Goto Symbol in File

Is your Deployment Manifest getting larger and is it becoming harder to find a particular Instance Group, 
Release, or Stemcell definition? The "Goto Symbol in File" command helps you quickly jump to a specific
definition.

Type `CTRL-SHIFT-O` to popup a list of all symbols in your current file. Start typing a name 
(or portion thereof) to narrow down the list. Select a symbol to jump directly to its location in the
file.

![Goto Symbol Screenshot][goto_symbol]

### Goto/Peek Definition

Use "Goto Defition" or "Peek Definition" to quickly go (or peek) from a Release or Stemcell name 
to its corresponding definition.

![Peek Definition Screenshot][peek]

### V2 versus V1 Schena

The editor is intended primarily to support editing manifests in the [V2 schema](https://bosh.io/docs/manifest-v2.html).
When you use attributes from the V1 schema the editor will detect this however and switch to 'V1 tolerance' mode.

In this mode, V1 properties are accepted but marked with deprecation warnings and V2 properties are marked as (unknown property)
errors.

## Usage

### Activating the Editor

The Bosh editor automatically activates when the name of the  `.yml` file you are editing 
follows a certain pattern:

  - `**/*deployment*.yml` : activates support for bosh manifest file.
  
You can also define your own patterns and map them to the language-id `bosh-deployment-manifest` 
by defining `files.associations` in workspace settings. 
See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

### Targetting a specific Director

Some of the Validations and Content Assist depend on information dymanically retrieved from an active Bosh director.
This information is retrieve by the editor by executing commands using the Bosh CLI. For this to work the CLI (V2 
CLI is required) and editor have to be installed and configured correctly.

There are two ways to set things up:

#### Explicitly Configure the CLI:

From vscode, press `CTRL-SHIFT-P` and type `Settings` then select either "Open User Settings" or "Open Workspace Settings".
The bosh cli can be conigured by specifying values for keys of the form `bosh.cli.XXX`. Content assist and hover docs 
explain the meaning of the available keys.

#### Implictly Configure the CLI:

If the bosh cli is not explicitly configure it will, by default try to execute commands like `bosh cloud-config --json` 
without an explicit `-e ...` argument to target a specific director. This works only if you ensure that the editor
process executes with a proper set of environment variables:

- `PATH`: must be set so that `bosh` command can be found and refers to the V2 CLI.
- `BOSH_ENVIRONMENT`: must be set to point to the bosh director you want to target.

If you start vscode from a terminal, you can verify that things are setup correctly by executing command:

     bosh cloud-config

If that command executes without any errors and returns the cloud-config, then things are setup correctly 
and if you launch vscode from that same terminal the dynamic CA and linting should work correctly.

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

[linting]:     https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/linting.png
[ca1]:         https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/content-assist-1.png
[ca2]:         https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/content-assist-2.png
[hovers]:      https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/hover.png
[peek]:        https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/peek.png
[goto_symbol]: https://raw.githubusercontent.com/spring-projects/sts4/7e3cf4808095f8b126bf1e5a90c09f3917f60fa4#diff-805398ce75c761e0cebc09416ddad306/vscode-extensions/vscode-bosh/readme-imgs/goto-symbol.png
