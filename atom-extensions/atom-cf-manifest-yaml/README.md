# Cloud Foundry Manifest YAML Editor for Atom
[![macOS Build Status](https://travis-ci.org/spring-projects/atom-cf-manifest-yaml.svg?branch=master)](https://travis-ci.org/spring-projects/atom-cf-manifest-yaml) [![Windows Build Status](https://ci.appveyor.com/api/projects/status/1jvknxt9jhykgrxo?svg=true)](https://ci.appveyor.com/project/spring-projects/atom-cf-manifest-yaml/branch/master) [![Dependency Status](https://david-dm.org/spring-projects/atom-cf-manifest-yaml.svg)](https://david-dm.org/spring-projects/atom-cf-manifest-yaml)

This extension provides basic validation, content assist and hover infos
for editing Cloud Foundry [Manifest](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html) Files.

It is recommended to use this extension package when `atom-ide-ui` atom extension package is installed. Thus, reconciling (error/warning markers) and hover support is fully functional. 

## Usage

The CF manifest editor automatically activates when the name of the file you are editing is `manifest.yml` or editor grammar is set to `Manifest-YAML`

## Functionality

### Validation

(Requires `atom-ide-ui` package) As you type the manifest is parsed and checked for basic syntactic and structural correctness. Hover over
an error marker to see an explanation.

![Linting Screenshot][linting]

### Content Assist

Having trouble remembering all the names of the attributes, and their spelling? Content assist to the
rescue:

![Content Assist Screenshot][ca]

### Dynamic Cloud Foundry Content Assist

Would you like to see actual Cloud Foundry values for attributes like `services`, `buildpack`, `domain`, and `stack`? If you have [cf CLI](https://docs.cloudfoundry.org/cf-cli/) installed and connected to a Cloud target, the extension will automatically display values for certain manifest attributes.

For example, if you are connected to a Cloud target that has services, and you invoke content assist in an entry under `services`, you will see the available service instances. In addition, service instances that do not exist in the Cloud target, like `redisserv` in the example below, are highlighted with a warning.

Dynamic content assist also shows which Cloud target you are connected to.

![Dynamic Cloud Foundry Content Assist Screenshot][dcfca]

### Documentation Hovers

(Requires `atom-ide-ui` package) Having trouble remembering exactly what the meaning of each attribute is? Hover over any attribute and 
read its detailed documentation.

![Hover Docs Screenshot][hovers]

## Dev environment setup:
**Prerequisite**: Node 6.x.x or higher is installed, Atom 1.17 or higher is installed
1. Clone the repository
2. Run `npm install`
3. Execute `apm link .` from the folder above
5. Perform `Reload Window` in Atom (Cmd-Shift-P opens commands palette, search for `reaload`, select, press `Return`)
6. Open any `manifest.yml` file in Atom observe reconciling, content assist and other IDE features

[linting]: https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-cf-manifest-yaml/readme-imgs/linting.png
[ca]: https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-cf-manifest-yaml/readme-imgs/content-assist.png
[dcfca]: https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-cf-manifest-yaml/readme-imgs/cf-dynamic-content-assist.png
[hovers]: https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-cf-manifest-yaml/readme-imgs/hovers.png

