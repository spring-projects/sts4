**Custom buildpacks**: If your app requires a custom buildpack, you can use the `buildpacks` attribute to specify it in a number of ways:
  * By name: `MY-BUILDPACK`.
  * By GitHub URL: `https://github.com/cloudfoundry/java-buildpack.git`.
  * By GitHub URL with a branch or tag: `https://github.com/cloudfoundry/java-buildpack.git#v3.3.0` for the `v3.3.0` tag.

```
---
  ...
  buildpacks: 
    - buildpack_URL
```
      
**Multiple buildpacks**: If you are using multiple buildpacks, you can add an additional value to your manifest:

```
---
  ...
  buildpacks: 
    - buildpack_URL
    - buildpack_URL
```

**Note**:  You must specify multiple buildpacks in the correct order: the buildpack will use the app start command given by the final buildpack. See https://github.com/cloudfoundry/multi-buildpack#usageapp for more information.

The `cf buildpacks` command lists the buildpacks that you can refer to by name in a manifest or a command line option.

The command line option that overrides this attribute is `-b`.



