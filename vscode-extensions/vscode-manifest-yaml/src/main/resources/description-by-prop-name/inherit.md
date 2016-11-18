A single manifest can describe multiple applications. Another powerful technique is to create multiple manifests with inheritance. Here, manifests have parent-child relationships such that children inherit descriptions from a parent. Children can use inherited descriptions as-is, extend them, or override them.

Content in the child manifest overrides content in the parent manifest, if the two conflict.

This technique helps in these and other scenarios:

*   An application has a set of different deployment modes, such as debug, local, and public. Each deployment mode is described in child manifests that extend the settings in a base parent manifest.

*   An application is packaged with a basic configuration described by a parent manifest. Users can extend the basic configuration by creating child manifests that add new properties or override those in the parent manifest.

The benefits of multiple manifests with inheritance are similar to those of minimizing duplicated content within single manifests. With inheritance, though, we “promote” content by placing it in the parent manifest.

Every child manifest must contain an “inherit” line that points to the parent manifest. Place the inherit line immediately after the three dashes at the top of the child manifest. For example, every child of a parent manifest called `base-manifest.yml` begins like this:

```
---
  ...
  inherit: base-manifest.yml
```
You do not need to add anything to the parent manifest.

In the simple example below, a parent manifest gives each application minimal resources, while a production child manifest scales them up.

**simple-base-manifest.yml**

```
---
path: .
domain: shared-domain.com
memory: 256M
instances: 1
services:
- singular-backend

# app-specific configuration
applications:
 - name: springtock
   host: 765shower
   path: ./april/build/libs/april-weather.war
 - name: wintertick
   host: 321flurry
   path: ./december/target/december-weather.war
```

**simple-prod-manifest.yml**

```
---
inherit: simple-base-manifest.yml
applications:
 - name:springstorm
   memory: 512M
   instances: 1
   host: 765deluge
   path: ./april/build/libs/april-weather.war
 - name: winterblast
   memory: 1G
   instances: 2
   host: 321blizzard
   path: ./december/target/december-weather.war
```

**Note**: Inheritance can add an additional level of complexity to manifest creation and maintenance. Comments that precisely explain how the child manifest extends or overrides the descriptions in the parent manifest can alleviate this complexity.