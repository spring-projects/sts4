The source code in this package is taken from here:

https://github.com/spring-projects/spring-boot/tree/fca6dbaf09c32202d9d958f815221aad54b9fc7b/spring-boot-tools/spring-boot-configuration-metadata/src/main/java/org/springframework/boot/configurationmetadata

Notes:
 - This commit is from the master branch at a point in time where boot team is working on Boot 1.4.x on that branch.

There are currently no modifications being made to that code at all to accomodate STS. So it may now be possible to consume it as a proper dependency.
However, keep in mind that we are using a modified copy of 'org.json' to allow controlling key order in json maps. So that probably
complicates things.
