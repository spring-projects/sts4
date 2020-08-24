## 2020-08-27 (4.7.2 RELEASE, incl. language servers version 1.21.0)

* no major changes

## 2020-07-30 (4.7.1 RELEASE, incl. language servers version 1.20.0)

* _(Concourse)_ fixed: Typo in README ([#494](https://github.com/spring-projects/sts4/issues/494))

## 2020-06-18 (4.7.0 RELEASE, incl. language servers version 1.19.0)

* no major changes

## 2020-05-28 (4.6.2 RELEASE, incl. language servers version 1.18.0)

* _(VSCode/Theia)_ bugfix: Fix null reference when no JVM was found - contributed by [@tfriem](https://github.com/tfriem)
* _(Concourse)_ improvement: add support for set-pipeline step ([#464](https://github.com/spring-projects/sts4/issues/464)) - contributed by [@deepakmohanakrishnan1984](https://github.com/deepakmohanakrishnan1984)

## 2020-04-23 (4.6.1 RELEASE)

* no major changes

## 2020-03-19 (4.6.0 RELEASE)

* no major changes

## 2020-01-22 (4.5.1 RELEASE)

* no major changes

## 2019-12-19 (4.5.0 RELEASE)

* no major changes

## 2019-11-21 (4.4.2 RELEASE)

* _(Concourse)_ enhancement: Concourse extension doesn't know about check_every for a resource type
   ([#382](https://github.com/spring-projects/sts4/issues/382))
* _(Concourse)_ enhancement: Concourse extension doesn't know about on_error for jobs
   ([#383](https://github.com/spring-projects/sts4/issues/383))
* _(Concourse)_ enhancement: complains about old_name in a job
   ([#388](https://github.com/spring-projects/sts4/issues/388))
* _(Concourse)_ enhancement: Fix concourse task schema
   ([#387](https://github.com/spring-projects/sts4/issues/387)) - PR from @z4ce
* _(Concourse)_ enhancement: Add support for `build_log_retention` attribute in 'Job'
   ([#389](https://github.com/spring-projects/sts4/issues/389))
* _(Concourse)_ enhancement: Support validation and completions inside `params` of `resource_type`
   ([#390](https://github.com/spring-projects/sts4/issues/390))
* _(Concourse)_ enhancement: Concourse 'aggregate' property is now marked as deprecated

## 2019-10-24 (4.4.1 RELEASE)

* _(Concourse)_ enhancement: added support for `registry-image` resource type.
   ([#380](https://github.com/spring-projects/sts4/issues/380))

## 2019-09-19 (4.4.0 RELEASE)

* _(Concourse)_ fixed: in_parallel - support both array of steps; and explicit "steps" child ([#345](https://github.com/spring-projects/sts4/issues/345))
* _(Concourse)_ fixed: support 'inputs' property for type 'PutStep' ([#341](https://github.com/spring-projects/sts4/issues/341))
* _(VSCode)_ fixed: show error message when manually configured JDK is not there

## 2019-08-13 (4.3.2 RELEASE)

* _(Concourse)_ fixed: VS Code Extensions missing Cloud Foundry params options ([#330](https://github.com/spring-projects/sts4/issues/330))
* _(Concourse)_ fixed: Snippet completions in vscode-concourse not working
* _(VSCode)_ fixed: Spring boot tool vscode extension is causing system to run out of disk space ([#328](https://github.com/spring-projects/sts4/issues/328))

## 2019-07-12 (4.3.1 RELEASE)

* _(all language servers)_ performance: further improvements to the language server startup time

## 2019-06-21 (4.3.0 RELEASE)

- _(Concourse)_ fixed: Added support for `vars` property in task step ([#307](https://github.com/spring-projects/sts4/issues/307))

## 2019-05-24 (4.2.2 RELEASE)

* _(all language servers)_ performance: additional improvements to language server startup time
* _(Concourse)_ fixed: Concourse VSCode Does not recognize "initial_path" param for S3 resource ([#284](https://github.com/spring-projects/sts4/issues/284))

## 2019-04-18 (4.2.1 RELEASE)

* _(VSCode, Atom, Theia)_ improvement: JVM args can now be configured for language server processes

## 2019-03-21 (4.2.0 RELEASE)

* no major changes

## 2019-02-21 (4.1.2 RELEASE)

* _(Concourse)_ new: support for hierarchical symbols in file added, produces nice outline view information now
* _(Concourse)_ new: support for YAML anchors, references, extend added ([#58](https://github.com/spring-projects/sts4/issues/58))
* _(Concourse)_ fixed: Concourse extension doesn't know about tag_file for docker-image resource ([#197](https://github.com/spring-projects/sts4/issues/197))
* _(Concourse)_ fixed: Concourse extension confused by docker-image resource type ([#196](https://github.com/spring-projects/sts4/issues/196))
* _(Concourse)_ fixed: valid uri's are flagged as errors in git resource ([#194](https://github.com/spring-projects/sts4/issues/194))
* _(Concourse)_ fixed: content-assist can't deal with empty lines

## 2019-01-24 (4.1.1 RELEASE)

* no major changes

## 2018-12-20 (4.1.0 RELEASE)

* no major changes

## 2018-11-30 (4.0.2 RELEASE)

* (General) Various bugfixes for bugs causing language servers to hang and become unresponsive.
* (Spring Boot) (Concourse) Bugfix: Quickfix not working (anymore?) in LSP editors.

## 2018-10-31 (4.0.1 RELEASE)

* _(Concourse)_ added support for new attributes for S3 resource

## 2018-09-25 (4.0.0 RELEASE)

* no major changes

## 2018-08-30 (M15)

* no major changes

## 2018-08-09 (M14)

*  _(Concourse)_ added support for `tags` property in `resources` ([#66](https://github.com/spring-projects/sts4/issues/66))

## 2018-07-23 (M13)

* _(Concourse)_ Added support for missing attributes to `GitGetParams`, `GitPutParams`, `Job` and `Step` schemas ([#64](https://github.com/spring-projects/sts4/issues/64)), ([#65](https://github.com/spring-projects/sts4/issues/65))
* _(Concourse)_ fixed: Concourse VSCode Extension: Does not recognize GCS buckets in semver resource ([#60](https://github.com/spring-projects/sts4/issues/60))

## 2018-06-08 (M12)

* _(Concourse)_ add support for new attributes of DockerImageSource (aws_session_token, max_concurrent_downloads, max_concurrent_uploads) and DockerImagePutParams (additional_tags, cache_from, load_bases, target_name) ([#56](https://github.com/spring-projects/sts4/issues/56))
* _(Concourse)_ bugfix: quick fixes work again

## 2018-05-14 (M11)

* _(all)_ JVM used to run the language servers can now be specified via custom settings ([#51](https://github.com/spring-projects/sts4/issues/51))

## 2018-03-15 (M10)

* _(Concourse)_ added support for symbols for groups
* _(Concourse)_ updated URLs in documentation to point to new Concourse domain
* _(all)_ language server processes now show up with their real name when using `jps` instead of just `JarLauncher`

## 2018-02-23 (M9)

* _(Concourse)_ Concourse CI Pipeline Editor reports errors on valid `pipeline.yml` fixed (([#41](https://github.com/spring-projects/sts4/issues/41)))
* _(all)_ improved the way the JDK to run the language server is found together with an improved error message if no JDK can be found

## 2018-01-30 (M8)

* _(Concourse)_ added support for cache attribute in tasks
* _(Concourse)_ added missing version attribute in image_resource

## 2017-12-15 (M7)

* _(all)_ issues solved when running on Windows ([#25](https://github.com/spring-projects/sts4/issues/25), [#26](https://github.com/spring-projects/sts4/issues/26), [#29](https://github.com/spring-projects/sts4/issues/29))
* _(Concourse)_ getstep.version property in concourse pipeline now accepts a Map<String,String> ([#24](https://github.com/spring-projects/sts4/issues/24)) as well as special values 'every' and 'latest' ([#28](https://github.com/spring-projects/sts4/issues/28)). 

## 2017-12-04

* initial public beta launch