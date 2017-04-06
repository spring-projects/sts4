*Optional.* When bumping, bump to a prerelease (e.g. `rc` or
`alpha`), or bump an existing prerelease.

 If present, and the version is already a prerelease matching this value,
its number is bumped. If the version is already a prerelease of another
type, (e.g. `alpha` vs. `beta`), the type is switched and the prerelease
version is reset to `1`. If the version is *not* already a pre-release, then
`pre` is added, starting at `1`.