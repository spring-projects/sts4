# Development Notes

## Testing Release repo with Atom
1. Clone the release repository for Atom package 'X'
2. Navigate to `X` folder
3. Run `npm install` and nothing more
4. Run `apm link .`
5. Either start Atom or Reload Window in Atom 

## Releasing Atom Extension

Before Atom package is released the RC language server `JAR` needs to be produced. Open [promote-fatjars-to-rc](https://ci.spring.io/teams/tools/pipelines/sts4-master/jobs/promote-fatjars-to-rc/builds/1) task in Concourse and kick off the build.
Once the build completes successfully it'd push the update to `properties.json` of the Atom extension with the link to the RC language server `JAR` file to the release git repository

The release process for an STS4 Atom package `X` is to be done with the contents of the release GitHub repository corresponding to the Atom package. Namely: `https://github.com/spring-projects/<X>`
(Example: `atom-bosh` -> Release repo: https://github.com/spring-projects/atom-bosh)

1. Clone the release repository for Atom package `X`
2. Navigate to `X` folder. Examine `package.json`. The version in that file is most likely the one to be published. (Verify tags of the release git repo to double-check the version)
3. Lets assume it's version `A.B.C` to be publsihed. The same version must be in the `package.json`
4. Execute `git tag vA.B.C`
5. Execute `git push --tags`
6. Execute `apm publish --tag vA.B.C`
