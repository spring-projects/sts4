# Cloud Foundry Manifest Editor for Visual Studio Code

This extension provides basic validation, content assist and hover infos
for editing Cloud Foundry [Manifest](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html) Files.

## Usage

The CF manifest editor automatically activates when the name of the `.yml` file you are editing, 
matches the glob pattern: `manifest*.yml`.

## Functionality

### Validation

As you type the manifest is parsed and checked for basic syntactic and structural correctness. Hover over
an error marker to see an explanation.

![Linting Screenshot](https://github.com/spring-projects/sts4/raw/master/vscode-extensions/vscode-manifest-yaml/readme-imgs/linting.png)

### Content assist

Having trouble remembering all the names of the attributes, and their spelling? Content assist to the
rescue:

![Content Assist Screenshot](https://github.com/spring-projects/sts4/raw/master/vscode-extensions/vscode-manifest-yaml/readme-imgs/content-assist.png)

### Documentation Hovers

Having trouble remembering exactly what the meaning of each attribute is? Hover over any attribute and 
read its detailed documentation.

![Hover Docs Screenshot](https://github.com/spring-projects/sts4/raw/master/vscode-extensions/vscode-manifest-yaml/readme-imgs/hovers.png)

