# VS Code Language Server for Spring Boot Application Properties

Initial implementation of the Language Server for Spring Boot application properties. 

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
