com.github.dockerjavaAlternate Docker Client
=====================

Repackaged as a single osgi bundle with all its dependencies.

Source: https://github.com/docker-java/docker-java/blob/master/docs/transports.md

Using the 'zero-dep' client in the hopes it poses the fewest complications / dependency messes.

Packaging Process:
==================

- Build the maven project `docker-client-wrapper` which is included as a sub-folder in this project.
- copy all the jars from `docker-client-wrapper/target/dependency` to `dependency`.
- Open `manifest.mf` with PDE tabbed editor. Select the 'Runtime' tab and then:
   - clear out exported packages section then add all `com.github.dockerjava*` packages back
   - clear our 'classpath' section and all jars in the 'dependency folder.


IMPORTANT:
   
The following dependencies must not be consumed from wrapped jars and should instead be replaced with
bundle dependencies. This is to avoid classloader errors because of two versions of the same types clashing
with eachother in boot.dash.docker plugin.

- guava 

Process to replace the dependency is to 

- remove it from the 'classpath' section of the manifest
- add equivalent bundle dependency. Try to preserve the minimum version constraint.