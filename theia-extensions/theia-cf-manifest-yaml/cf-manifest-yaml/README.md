# Cloud Foundry Manifest Editor for Theia IDE

This extension provides basic validation, content assist and hover infos
for editing Cloud Foundry [Manifest](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html) Files.

## Usage

The CF manifest editor automatically activates when the name of the `.yml` file you are editing, 
matches the glob pattern: `manifest*.yml`.

## Functionality

### Validation

As you type the manifest is parsed and checked for basic syntactic and structural correctness. Hover over
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

Having trouble remembering exactly what the meaning of each attribute is? Hover over any attribute and 
read its detailed documentation.

![Hover Docs Screenshot][hovers]


[linting]: https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-cf-manifest-yaml/cf-manifest-yaml/readme-imgs/validation.png
[ca]: https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-cf-manifest-yaml/cf-manifest-yaml/readme-imgs/ca.png
[dcfca]: https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-cf-manifest-yaml/cf-manifest-yaml/readme-imgs/dynamic-ca.png
[hovers]: https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-cf-manifest-yaml/cf-manifest-yaml/readme-imgs/hovers.png

**Version: 1.9.0**
