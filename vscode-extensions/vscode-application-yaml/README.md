# VS Code Language Server Example in Java

The repo contains the end-result of an 'exercise' to implement
a simple VS Code Language Server in Java. 

The starting point is a clone of this repo:

https://github.com/georgewfraser/vscode-javac

It provides Java support using the javac API.

As such that repo contains all the pieces necessary
to create a working Language Server. In this
exercise we stripped it down to the bare minimum
to make a very basic language server similar to
the one built in the 
[VScode tutorial on building a language server](https://code.visualstudio.com/docs/extensions/example-language-server).

# Running

The extension implemented in this example consists out of two pieces:
 
  - client: a typescript js app that launches and connects to the language server.
  - server: server app, implemented in Java.
  
First build the server:

    mvn clean package

The server will be produced in `out/fat-jar.jar`.

Then build the client:

    npm clean install

Now you can open the client-app in vscode. From the root of this project.

    code .

To launch the language server in a vscode runtime, press F5.

# Debugging

To debug the language server, open `lib/Main.ts` and edit to set the
`DEBUG` constant to `true`. When you laucnh the app next by pressing
`F5` it will launch with debug options being passed to the JVM.

You can then connect a 'Remote Java' Eclipse debugger on port 8000.

Note that in debug mode we launch not from the 'fatjar' produced by the
maven build, but instead use the classes from 'target/classes' directory.
This allows you to edit the server code in Eclipse and relaunch the
client from vscode without rebuilding the fatjar.
