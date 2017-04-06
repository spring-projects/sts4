*Optional.* Default `tag`. The specific tag to pull before
building when `cache` parameter is set. Instead of pulling the same tag
that's going to be built, this allows picking a different tag like
`latest` or the previous version. This will cause the resource to fail
if it is set to a tag that does not exist yet.