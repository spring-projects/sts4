### Initial state

If no resource versions exist you can set up this resource to emit an initial version with a specified content. This won't create a real resource in S3 but only create an initial version for Concourse. The resource file will be created as usual when you `get` a resource with an initial version.

You can define one of the following two options:

* `initial_path`: *Optional.* Must be used with the `regexp` option. You should set this to the file path containing the initial version which would match the given regexp. E.g. if `regexp` is `file/build-(.*).zip`, then `initial_path` might be `file/build-0.0.0.zip`. The resource version will be `0.0.0` in this case.

* `initial_version`: *Optional.* Must be used with the `versioned_file` option. This will be the resource version.

By default the resource file will be created with no content when `get` runs. You can set the content by using one of the following options:

* `initial_content_text`: *Optional.* Initial content as a string.

* `initial_content_binary`: *Optional.* You can pass binary content as a base64 encoded string.
