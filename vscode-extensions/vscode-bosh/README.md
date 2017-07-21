# Bosh Deployment Manifest Editor for Visual Studio Code

This extension provides validation, content assist and documentation hovers
for editing [Bosh](https://bosh.io/) Deployment Manifest files.

## Usage

### Activating the Editor

The Bosh editor automatically activates when the name of the  `.yml` file you are editing 
follows a certain pattern:

  - `**/*deployment*.yml` : activates support for bosh manifest file.
  
You can also define your own patterns and map them to the language-id `bosh-deployment-manifest` 
by defining `files.associations` in workspace settings. 
See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

### Targetting a specific Director

Some Validation and Content Assist use information dymanically retrieved from an active Bosh director.
For these feature to work it is required that you 

- have the bosh cli V2 installed (information is obtained by executing commands using the V2 cli)
- target a director by setting the `BOSH_ENVIROMENT` variable.

You can verify that you have set things up right by executing command:

```
bosh cloud-config --json
```

If setup correctly, it should return information about the cloud-config on your intended bosh director/environment.

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

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

[linting]: https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/linting.png
[ca1]:     https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/content-assist-1.png
[ca2]:     https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/content-assist-2.png
[hovers]:  https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/hover.png
[peek]:    https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/peek.png
[goto_symbol]: https://raw.githubusercontent.com/spring-projects/sts4/master/vscode-extensions/vscode-bosh/readme-imgs/goto-symbol.png
