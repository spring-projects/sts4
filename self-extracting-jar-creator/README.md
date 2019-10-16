Self-extracting Jar Creator
===========================

Simple utility that creates self-extracting jars.

Building
--------

Run `./build.sh` from the directory containing this readme.
The creator utility is produced as `target/self-extracting-jar-creator-*.jar`.
A copy of this file is also installed into eclipse-distributions/commons for
use by the STS build.

Creating a Self Extracting Jar
------------------------------

Run command `java -jar self-extracing-jar-creator/target/self-extracing-jar-creator-0.0.1-SNAPSHOT.jar <path-to-zip-you-want-to-repackage>`.
The repackaged zip is created in the same directory as the original zip.

Unpacking a Self Extracting Jar
------------------------------

Double-click the self extracting jar (this works only if your OS environment is setup to recognize executable jars and run them
through a JVM, this seems to be the case in Windows if a JVM is installed). Alternatively, you can also unpack STS 
from the commandline via `java -jar <path-to-self-extracting-jar>`.
