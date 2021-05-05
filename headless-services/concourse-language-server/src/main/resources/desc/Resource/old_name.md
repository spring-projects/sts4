*Optional* The old name of the resource. If configured, the history of the old resource will be inherited to the new one. Once the pipeline is set, this field can be removed as the history has been transferred.

This can be used to rename a resource without losing its history, like so:

```
resources:
- name: new-name
  old_name: current-name
  type: git
  source: uri: "https://github.com/vito/booklit"
 ```
 
After the pipeline is set, the resource was successfully renamed, so the `old_name` field can be removed from the resource:

```
resources:
- name: new-name
  type: git
  source: uri: "https://github.com/vito/booklit"
```