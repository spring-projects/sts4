# Concourse CI YAML Editor for Atom
[![macOS Build Status](https://travis-ci.org/spring-projects/atom-concourse.svg?branch=master)](https://travis-ci.org/spring-projects/atom-concourse) [![Windows Build Status](https://ci.appveyor.com/api/projects/status/1jvknxt9jhykgrxo?svg=true)](https://ci.appveyor.com/project/spring-projects/atom-concourse/branch/master) [![Dependency Status](https://david-dm.org/spring-projects/atom-concourse.svg)](https://david-dm.org/spring-projects/atom-concourse)

This extension provides basic validation, content assist and hover infos
for editing Concourse [Pipeline](https://concourse.ci/pipelines.html) and [Task Configuration](https://concourse.ci/running-tasks.html) Files.

It is recommended to use this extension package when `atom-ide-ui` atom extension package is installed. Thus, reconciling (error/warning markers) and hover support is fully functional. 

## Usage

The Concourse editor automatically activates when the name of the file you are editing is `pipeline.yml` or `task.yml`. Alternately, you can select the grammar for your file by doing these steps:

- Open the file, and it will most likely open with the default Atom YAML editor.
- In the bottom-right of the editor, click on YAML.
- This opens the Grammar Selection dialogue. Search and select `Concourse-Pipeline-YAML` for pipeline files, or `Concourse-Task-YAML` for task files.

## Functionality

### Validation

(Requires `atom-ide-ui` package) As you type the file is parsed and checked for basic syntactic and structural correctness. Hover over an error marker to see an explanation.

![Linting Screenshot][linting]

### Content Assist

Having trouble remembering all the names of the attributes, and their spelling? Or can't remember
which resource properties to set in the `get` task params versus its `source` attributes? Or
don't remember what 'special' values are acceptable for a certain property? Content assist
to the rescue:

![Content Assist Screenshot][ca1]

![Content Assist Screenshot][ca2]

### Documentation Hovers

(Requires `atom-ide-ui` package) Having trouble remembering exactly what the meaning of each attribute is? Hover over any attribute and 
read its detailed documentation.

![Hover Docs Screenshot][hovers]

### Navigate to Symbol in File

Is your Pipeline yaml file getting larger and it is becoming harder to find a particular Job, Resource or
Resource Type declaration? The Atom Outline View (View -> Toggle Outline View) helps you quickly jump to a specific definition.

![Outline View][outline_view]

## Dev environment setup:
**Prerequisite**: Node 6.x.x or higher is installed, Atom 1.17 or higher is installed
1. Clone the repository
2. Run `npm install`
3. Execute `apm link .` from the folder above
5. Perform `Reload Window` in Atom (Cmd-Shift-P opens commands palette, search for `reload`, select, press `Return`)
6. Open any `pipeline.yml` or `task.yml` file in Atom observe reconciling, content assist and other IDE features

[linting]:
https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-concourse/readme-imgs/linting.png

[ca1]:
https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-concourse/readme-imgs/ca1.png

[ca2]:
https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-concourse/readme-imgs/ca2.png

[hovers]:
https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-concourse/readme-imgs/hovers.png

[outline_view]:
https://raw.githubusercontent.com/spring-projects/sts4/af715bad53bd6cf30a10a2dc6d34bfcc17968382/atom-extensions/atom-concourse/readme-imgs/outline_view.png

