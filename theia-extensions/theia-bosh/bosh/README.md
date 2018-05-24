# Bosh Deployment Manifest Editor for Theia IDE

This extension provides validation, content assist and documentation hovers
for editing [Bosh](https://bosh.io/) Deployment Manifest files and
Cloud Configs.

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

### V2 versus V1 Schema

The editor is intended primarily to support editing manifests in the [V2 schema](https://bosh.io/docs/manifest-v2.html).
When you use attributes from the V1 schema the editor will detect this however and switch to 'V1 tolerance' mode.

In this mode, V1 properties are accepted but marked with deprecation warnings and V2 properties are marked as (unknown property)
errors.

## Usage

### Activating the Editor

The Bosh editor automatically activates when the name of the  `.yml` file you are editing
follows a certain pattern:

  - `**/*deployment*.yml` : activates support for Bosh manifest file.
  - `**/*cloud-config*.yml` : activates support for Bosh cloud config.

### Targetting a specific Director

Some of the Validations and Content Assist depend on information dymanically retrieved from an active Bosh director.
The editor retreives information by executing commands using the Bosh CLI. For this to work the CLI (V2
CLI is required) and editor have to be installed and configured correctly.

There are two ways to set things up to make this work:

#### Explicitly Configure the CLI:

The bosh cli is configured by specifying keys of the form `bosh.cli.XXX`. These can be specified in the Theia preference
file (`~/.theia/settings.json`).

Note that the `bosh.cli.XXX` settings do not allow you to provide credentials to connect to the director.
The editor assumes that you are providing the credentials implicitly by using the `bosh login` command from a terminal.
The bosh cli will persist the credentials in `~/.bosh/config` and read them from there. A typical sequence of commands
to store the credentials would be something like the following:

First, create an alias for your environment:

```
$ bosh alias-env my-env -e 10.194.4.35 --ca-cert <(bosh int ./creds.yml --path /director_ssl/ca)
Using environment '10.194.4.35' as anonymous user
...
Succeeded
$
```

Second, obtain username/password for your director. For example:

```
$ bosh int ./creds.yml --path /admin_password
very-secret-admin-password

Succeeded
```

Now use `bosh login` to establish a session and store the credentials:

```
$ bosh login -e my-env
Username (): admin
Password (): very-secret-admin-password

Using environment '10.194.4.35' as client 'admin'

Logged in to '10.194.4.35'

Succeeded
```

You can verify that CLI is setup correctly by executing a command like:

```
$ bosh -e my-env cloud-config
...
Succeeded
```

#### Implictly Configure the CLI:

If the bosh cli is not explicitly configured, the editor will, by default, try to execute commands like `bosh cloud-config --json`
and `bosh stemcells --json` without an explicit `-e ...` argument. This works only if you ensure that the editor
process executes with a proper set of OS environment variables:

- `PATH`: must be set so that `bosh` cli executable can be found and refers to the V2 CLI.
- `BOSH_ENVIRONMENT`: must be set to point to the bosh director you want to target.

If you start Theia from a terminal, you can verify that things are setup correctly by executing command:

     bosh cloud-config

If that command executes without any errors and returns the cloud-config you expected, then things are setup correctly.
If you subsequently launch Theia from that same terminal the dynamic CA and linting should work correctly.

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues).

[linting]:     https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/validation.png
[ca1]:         https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/ca-1.png
[ca2]:         https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/ca-2.png
[hovers]:      https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/hover.png
[peek]:        https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/peek-definition.png
[goto_symbol]: https://raw.githubusercontent.com/spring-projects/sts4/1bdd6f45aaf779252a2f0203f10da1a67b3c018e/theia-extensions/theia-bosh/bosh/readme-imgs/doc-symbol.png
