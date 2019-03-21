# Developer notes

## Getting and installing latest snapshot

The latest snapshot .vsix file can be downloaded from here:

https://dist.springsource.com/snapshot/STS4/nightly-distributions.html

You should get the file called `vscode-manifest-yaml-<version>.vsix`.

To install it in vscode follow these steps:

 - open vscode
 - Press `CTRL-SHIFT-P` and type 'vsix' in the search box
 - Select the `Extensions: Install from vsix file` command
 - Install the `.vsix` you downloaded earlier.

## Bulding and Running

This project consists of three pieces:

 - a vscode-extension which is a language-server client implemented in TypeScript.
 - commons-vscode: a local npm module with some utilities implemented in TypeScript.
 - a language server implemented in Java.

To build all these pieces you normally only need to run:

   npm install

**However, the first time you build** it might fail trying to
find the `commons-vscode` module on npm central. Once we publish a stable 
version of that module on npm central that will no longer be a problem. 
Until that time, you can work around this by doing a one time manual 
run of the `preinstall` script prior to running `npm install`:

    ./scripts/preinstall.sh
    npm install

Now you can open the client-app in vscode. From the root of this project.

    code .

To launch the language server in a vscode runtime, press F5.

## Debugging

To debug the language server, open `lib/Main.ts` and edit to set the
`DEBUG` option to `true`. When you laucnh the app next by pressing
`F5` it will launch with debug options being passed to the JVM.

You can then connect a 'Remote Java' Eclipse debugger on port 8000.

## Packaging as a vscode extension

First make sure the stuff is all built locally:

     ./scripts/preinstall.sh  # only needed if this is the first build.
     npm install

Then package it:

     npm run vsce-package

This produces a `.vsix` file which you can install directly into vscode.