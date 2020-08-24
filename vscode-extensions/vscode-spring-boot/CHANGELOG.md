## 2020-08-27 (4.7.2 RELEASE, incl. language servers version 1.21.0)

* no major changes

## 2020-07-30 (4.7.1 RELEASE, incl. language servers version 1.20.0)

* _(Spring Boot)_ enhancement: Syntax check for Annotations with Spring Expression Language ([#475](https://github.com/spring-projects/sts4/issues/475))
* _(Spring Boot)_ fixed: hard to reproduce BadLocationException inside of language server fixed now, occurred e.g. in ([#451](https://github.com/spring-projects/sts4/issues/451))

## 2020-06-18 (4.7.0 RELEASE, incl. language servers version 1.19.0)

* _(Spring Boot)_ bugfix: automatic live hover detection works again reliably
* _(VSCode)_ improvement: Flag to configure the "JAVA_HOME or PATH..." message display ([#478](https://github.com/spring-projects/sts4/issues/478))
* _(VSCode)_ bugfix: green live hovers markers are no longer gone after switching to a different editor

## 2020-05-28 (4.6.2 RELEASE, incl. language servers version 1.18.0)

* _(Spring Boot)_ improvement: additional check to auto-connect live hovers only when actuators are on the project classpath ([#450](https://github.com/spring-projects/sts4/issues/450))
* _(Spring Boot)_ improvement: added content assist for keys that exist in YAML and properties files ([#427](https://github.com/spring-projects/sts4/issues/427))
* _(Spring Boot)_ improvement: Yaml editor gives error when using @..@ placeholders ([#190](https://github.com/spring-projects/sts4/issues/190))
* _(Spring Boot)_ bugfix: super type hierarchy lookup might fail
* _(Spring Boot)_ bugfix: quickly restarting app caused error popup from live hover mechanism to show up
* _(VSCode/Theia)_ bugfix: Fix null reference when no JVM was found - contributed by [@tfriem](https://github.com/tfriem)

## 2020-04-23 (4.6.1 RELEASE)

* _(Spring Boot)_ improvement: Spring yaml validation doesn't understand the "<<:" syntax ([#440](https://github.com/spring-projects/sts4/issues/440))
* _(Spring Boot)_ improvement: YAML Property completion: Superclass properties not detected in case of List or Map ([#449](https://github.com/spring-projects/sts4/issues/449))
* _(Spring Boot)_ improvement: improved performance for content-assist for Spring XML config files
* _(Spring Boot)_ bugfix: Quick Fix for unknown properties broken ([#442](https://github.com/spring-projects/sts4/issues/442))

## 2020-03-19 (4.6.0 RELEASE)

* _(Spring Boot)_ performance: improved performance while scanning projects for symbols
* _(Spring Boot)_ performance: improved performance when multiple files change at once (e.g. after a switch to a different branch or a git pull + refresh)

## 2020-01-22 (4.5.1 RELEASE)

* _(Spring Boot)_ improvement: live hover mechanism now reports connection failures

## 2019-12-19 (4.5.0 RELEASE)

* _(Spring Boot)_ improvement: better and more consistent labels for running processes in live hover actions across the board

## 2019-11-21 (4.4.2 RELEASE)

* _(Spring Boot)_ bugfix: CTRL-click in yaml file inaccurate (for 'nested' properties)

## 2019-10-24 (4.4.1 RELEASE)

* _(Spring Boot)_ Provide UI to allow user to explicitly connect/disconnect to/from processes to collect Live Hover data from. See the [wiki](https://github.com/spring-projects/sts4/wiki/Live-Application-Information#managing-live-data-connections-to-local-processes) for details.
* _(Spring Boot)_ enhancement: Goto Symbol now also works from XML bean files.
* _(Spring Boot)_: improve performance of xml symbol scanning.

## 2019-09-19 (4.4.0 RELEASE)

* _(Spring Boot)_ fixed: Adding/removing bean in XML file doesn't update the symbol index
* _(VSCode)_ fixed: show error message when manually configured JDK is not there

## 2019-08-13 (4.3.2 RELEASE)

* _(Spring Boot)_ fixed: CTRL-click navigation does not handle properties on super class correctly ([#326](https://github.com/spring-projects/sts4/issues/326))
* _(Spring Boot)_ fixed: Configuration property analysis does not handle properties written in snake_case correctly ([#327](https://github.com/spring-projects/sts4/issues/327))
* _(VSCode)_ fixed: Spring boot tool vscode extension is causing system to run out of disk space ([#328](https://github.com/spring-projects/sts4/issues/328))

## 2019-07-12 (4.3.1 RELEASE)

* _(all language servers)_ performance: further improvements to the language server startup time
* _(Spring Boot)_ fixed: wrong error markers in properties files([#314](https://github.com/spring-projects/sts4/issues/314))
* _(VS Code)_ fixed: vscode goto definition error with lsp ([#309](https://github.com/spring-projects/sts4/issues/309))

## 2019-06-21 (4.3.0 RELEASE)

- _(Spring Boot)_ improvement: project classpath notifications now happen in batch on startup to further optimize performance and job load on the Eclipse side
- _(Spring Boot)_ improvement: symbols are now being re-created if dependent types change
- _(Spring Boot)_ fixed: Slow code completion takes more than a 1 sec. ([#293](https://github.com/spring-projects/sts4/issues/293))
- _(Spring Boot)_ fixed: content-assist for Spring XML config files now working again in VS Code and Theia
- _(Spring Boot)_ fixed: ClassCast Exception in Boot LS while application.yml file opened in the editor
- _(Spring Boot)_ fixed: Anonymous inner type beans don't have boot hints

## 2019-05-24 (4.2.2 RELEASE)

* _(all language servers)_ performance: additional improvements to language server startup time
* _(Spring Boot)_ new: additional fine-grained preferences for Spring XML config file support
* _(Spring Boot)_ new: navigation for bean identifiers, bean classes, and property names for Spring XML config files
* _(Spring Boot)_ new: content-assist rolled out for many more Spring XML config elements and attributes
* _(Spring Boot)_ new: live bean information now showing up in types from class files (when source code is shown) - _VSCode and Theia only at the moment_
* _(Spring Boot)_ improvement: hugely improved content-assist for bean class attribute in Spring XML config files (incl. package name proposals and vastly improved performance)
* _(Spring Boot)_ improvement: property name content-assist in Spring XML config files now shows proposals from properties defined in supertypes, too
* _(Spring Boot)_ improvement: symbol scanning skips output folders now
* _(Spring Boot)_ fixed: Detect @RequestMapping with path defined as constant instead of literal string ([#281](https://github.com/spring-projects/sts4/issues/281))
* _(Spring Boot)_ fixed: NPE when invoking property name content-assist in XML file without bean class being defined
* _(Spring Boot)_ fixed: tags in yaml files with dollar signs throw IllegalGroupReference in properties editor

## 2019-04-18 (4.2.1 RELEASE)

* _(Spring Boot)_ performance: additional performance and memory footprint improvements to symbol indexing, eats now less memory and is faster while doing the initial indexing run
* _(Spring Boot)_ new: content-assist for bean types in Spring XML config files
* _(Spring Boot)_ new: content-assist for property names in Spring XML config files
* _(Spring Boot)_ new: content-assist for bean references in property definitions in Spring XML config files (very rough early cut, needs a lot more work on proposal content, number of proposals, and sorting)
* _(Spring Boot)_ improvement: limit the number of XML files that are scanned for bean symbols, output folders now ignored
* _(Spring Boot)_ fixed: Ctrl-click in Java editor in Eclipse wasn't working due to issue with hyperlink detector
* _(VSCode, Atom, Theia)_ improvement: JVM args can now be configured for language server processes

## 2019-03-21 (4.2.0 RELEASE)

* _(Spring Boot)_ new: Allow configuration of VM arguments for LSP process "PropertiesLauncher" ([#211](https://github.com/spring-projects/sts4/issues/211))
* _(Spring Boot)_ performance: major performance improvements to symbol indexing infrastructure by caching created symbols across language server starts
* _(Spring Boot)_ performance: replaced internal type indexing with communication to JDT (language server) to save time and memory spend for keeping our own type index

## 2019-02-21 (4.1.2 RELEASE)

* _(Spring Boot)_ new: live hover information for bean wirings now supports war-packaged boot apps running in a local server installation
* _(Spring Boot)_ new: live hover information for `@Value` annotations ([#177](https://github.com/spring-projects/sts4/issues/177))
* _(Spring Boot)_ new: property completion now works for `.yaml` files as well ([#191](https://github.com/spring-projects/sts4/issues/191))
* _(Spring Boot)_ new: bean symbols from XML config files now include exact location information
* _(Spring Boot)_ new: bean symbols from XML config files now generated for beans without a bean ID, too
* _(Spring Boot)_ fixed: navigate to resource in live hovers for apps running on CF works again
* _(Spring Boot)_ fixed: ConcurrentModificationException while retrieving symbols from language server
* _(Spring Boot)_ fixed: race condition that sometimes caused initial project to be not indexed for symbols
* _(Spring Boot)_ fixed: search for symbols in project now happens on the server side to avoid no project-related symbols showing up on the client side before you start typing in a query
* _(Spring Boot)_ performance: improvement to further reduce the CPU load when checking processes for live hovers ([#140](https://github.com/spring-projects/sts4/issues/140))
* _(Spring Boot)_ performance: the language server doesn't trigger a full source and javadoc download for Maven projects anymore
* _(VSCode)_ fixed: "class" snippet is not available ([#192](https://github.com/spring-projects/sts4/issues/192))

## 2019-01-24 (4.1.1 RELEASE)

* (Spring Boot) quick navigation via symbols now available for non-Boot Spring projects
* (Spring Boot) live hover informations for bean wirings now available for non-Boot Spring projects - _for details how to enable this for your apps, take a look at the [user guide](https://github.com/spring-projects/sts4/wiki/Live-Application-Information) section for that_
* (Spring Boot) added support for deprecated properties (including corresponding quick-fix) 
* (Spring Boot) quick fix to generate default metadata for missing properties ([#101](https://github.com/spring-projects/sts4/issues/101))
* (Spring Boot) first steps towards generating symbols for Spring XML config files ([#108](https://github.com/spring-projects/sts4/issues/108#issuecomment-455135918))
* (Spring Boot) fixed: live hovers don't show up for classes with a name starting with multiple upper case charatecters
* (Spring Boot) fixed: type and resource navigation in bean live hovers don't work for types and resources from dependencies

## 2018-12-20 (4.1.0 RELEASE)

* (Spring Boot) first initial version of content-assist for Spring Data repository definitions
* (Spring Boot) live hover links to types now work for projects using JDK 9 and beyond, too
* (Spring Boot) fixed an issue with stopped apps on CF causing boot language server to get stuck when connected to JMX via SSH tunnel

## 2018-11-30 (4.0.2 RELEASE)

* (Spring Boot) Make CTRL-CLICK navigation from application.properties to Java work
* (Spring Boot) Make CTRL-CLICK navigation from application.yml to Java work
* (Spring Boot) Made content-assist for values in lists more consistent across .yml and .properties editors.
* (Spring Boot) Bugfix: Adding eureka client starter to classpath breaks requestmapping live hovers.
* (Spring Boot) More precise autowiring live hovers for @Bean method parameters.
* (Spring Boot) server.servlet.context-path now supported for Request Mapping live hover links.
* (Spring Boot) Improved 'Goto Symbol' support for functional style WebFlux requestmappings.
* (Spring Boot) Improved 'Live Hover' support for functional style WebFlux requestmappings.
* (Spring Boot) Bugfix: Insertion of mapping templates now takes into account leading @ in editor.
* (Spring Boot) Added support for Spring Boot log groups in properties and yaml editor.
* (General) Various bugfixes for bugs causing language servers to hang and become unresponsive.
* (Spring Boot) (Concourse) Bugfix: Quickfix not working (anymore?) in LSP editors.
* (Spring Boot) After adding Spring Boot configuration processor editor automatically becomes aware of new properties metadata.

## 2018-10-31 (4.0.1 RELEASE)

* _(Spring Boot)_ fixed NPE from SpringIndexer ([#105](https://github.com/spring-projects/sts4/issues/105))
* _(Spring Boot)_ filed: Spring Boot configuration property auto-completion does not offer properties on super classes ([#116](https://github.com/spring-projects/sts4/issues/116))
* _(Spring Boot)_ fixed: Lots of NPE noise in language server ([#90](https://github.com/spring-projects/sts4/issues/90))
* _(Spring Boot)_ fixed: Live Boot Hint Decorators not working when app ObjectMapper configured with NON_DEFAULT inclusion  ([#80](https://github.com/spring-projects/sts4/issues/80))
* _(Spring Boot)_ fixed: property support now understand nested project structure

## 2018-09-25 (4.0.0 RELEASE)

* _(Spring Boot)_ `Cmd-6` in Eclipse shows `Go To Symbols in Workspace` first, second `Cmd-6` switches to `Go To Symbols in File`
* _(Spring Boot)_ various bug fixes

## 2018-08-30 (M15)

* _(Spring Boot)_ improved the overall content of bean wiring live hovers
* _(Spring Boot)_ live hover information for bean wirings now show up more precisely on autowired fields and constructors as well as at `@Bean` definitions, including more complete information about the wirings
* _(Spring Boot)_ improved performance of live hovers for remote boot apps
* _(Spring Boot)_ added experimental option to show code lenses for live hover information, including bean wiring and request mapping URLs (use the preferences to switch that on)
* _(Spring Boot)_ added detailed information to the boot dashboard property view about JMX tunnels over SSH to boot apps running on CloudFoundry
* _(Spring Boot)_ added action to enable/disable JMX tunneling through SSH for already deployed and running apps
* _(Spring Boot)_ improved performance and reduced footprint of live hover update mechanism
* _(Spring Boot)_ bugfix: fixed missing line break in live hover for request mappings
* _(Spring Boot)_ bugfix: resource links in live hovers for remote boot apps now working
* _(Spring Boot)_ bugfix: make the overall classpath detection mechanism more reliable in case of project deletions ([#69](https://github.com/spring-projects/sts4/issues/69))

## 2018-08-09 (M14)

* _(Spring Boot)_ added support for showing live hovers for Spring Boot apps running remotely (on Cloud Foundry)
* _(Spring Boot)_ improved and simplified content for live hovers showing bean wiring information
* _(Spring Boot)_ improved look of live hover highlights
* (_Spring Boot)_ improved error handling when source code parsing goes wrong
* (_Spring Boot)_ added specific bean wiring live hovers for `@Autowired` fields and constructors
* (_Spring Boot)_ user-defined values in property files showing up as suggestions for `@Value` completions
* (_Spring Boot)_ bean symbols now directly contain additional annotations (like `@Conditional...` or `@Profile`)
* (_Spring Boot)_ added option to match running process directly to specific project in the workspace (for live hovers) via system property (set `-Dspring.boot.project.name=<project-name-in-workspace>` to show live hovers of that running process exclusively on the project with that name).
* (_Spring Boot)_ fixed bug that prevented property editing support to work on Windows ([#59](https://github.com/spring-projects/sts4/issues/59))

## 2018-07-23 (M13)

* _(Spring Boot)_ early prototype for detecting changed bean definitions in live-running (and restarted) boot applications
* _(Spring Boot)_ @Inject annotation now supported for live hovers
* _(Spring Boot)_ added option to match live running apps and workspace projects manually
* _(Spring Boot)_ improved JMX connector reuse (internal optimization)

## 2018-06-08 (M12)

* _(Spring Boot)_ live hovers now updated in all open editors, not just the active one
* _(Spring Boot)_ more detailed context shown in hover documentation when editing property files ([#265](https://github.com/spring-projects/spring-ide/issues/265))
* _(Spring Boot)_ performance improvement: project symbols now show up a lot faster for the open editors/projects (in a multi-root folder workspace)
* _(Spring Boot)_ bugfix: JDK9 and JDK10 projects supported now even if main editor and/or language server runs on JDK8
* _(Spring Boot)_ bugfix: various NPEs in Spring indexer fixed

## 2018-05-14 (M11)

* _(Spring Boot)_ major performance improvements and footprint reductions (due to a groundbreaking change to how projects are being resolved, this is now delegated to the surrounding Java tooling)
* _(Spring Boot)_ support for JDK10 added
* _(Spring Boot)_ bugfix - custom JMX domains now supported for live hovers ([#44](https://github.com/spring-projects/sts4/issues/44))
* _(Spring Boot)_ bugfix - live hovers now show up for all open editors (not limited to the one with focus anymore)
* _(all)_ JVM used to run the language servers can now be specified via custom settings ([#51](https://github.com/spring-projects/sts4/issues/51))

## 2018-03-15 (M10)

* _(Spring Boot)_ support added for request-mapping-like symbols for webflux router definitions
* _(Spring Boot)_ code lenses for webflux handler methods implemented that visualize the mapped route (VSCode only at the moment)
* _(Spring Boot)_ document highlight support added for webflux router definitions that highlight parts of the routes that belong together
* _(Spring Boot)_ request mapping symbols now include accept and content type defintions
* _(Spring Boot)_ support for direct navigation from live injection reports to source code added for Eclipse
* _(Spring Boot)_ abstract @Bean-annotated methods are now ignored when creating symbols for bean definitions
* _(Spring Boot)_ URLs from hovers (like request mapping URLs from running boot apps) now open in an internal browser that has a navigation and an address bar
* _(Spring Boot)_ bugfix for NPE that happened occasionally when creating a new Java file
* _(all)_ language server processes now show up with their real name when using `jps` instead of just `JarLauncher`

## 2018-02-23 (M9)

__Attention:__ We merged the two different extensions for Spring Boot (for Java code and for properties) into a single extension for the various platforms (Eclipse, VSCode, Atom). This might require that you manually uninstall the old extensions and install the new ones. Automatic updates don't work here. For the Eclipse case, you might want to start with a fresh STS4 M9 distribution and go from there to avoid manual uninstall/install steps.

* _(Spring Boot)_ support added for Spring Data repositories, they show up as bean symbols now
* _(Spring Boot)_ fixed a bug that caused an exception when using content-assist for a non-Spring-Boot java project
* _(Spring Boot, VSCode)_ support for navigation added to live injection reports, they allow you to directly navigate to the source code of the bean type and the resource where the bean got defined. Limitation: this works for VSCode only at the moment, support for Eclipse and Atom still in progress
* _(Spring Boot, Eclipse integration)_ fixed a bug that caused content-assist to be turned off in the java editor
* improved the way the JDK to run the language server is found together with an improved error message if no JDK can be found

## 2018-01-30 (M8)

* _(Spring Boot Java)_ function declarations are now being parsed into symbols for functions that directly inherit from `java.util.Function` ([#18](https://github.com/spring-projects/sts4/issues/18))
* _(Spring Boot Java)_ updated live hover mechanics to work with latest Spring Boot 2.0 snapshot versions 
* _(Spring Boot Java)_ improved the way the JDK (to run the language server) is found
* _(Spring Boot Java)_ improved warning message about missing tools.jar
* _(Spring Boot Java)_ live hovers now show up on class flies that are displayed as source
* _(Spring Boot Java)_ fixed a problem with outdated symbols showing up after file deletion/rename
* _(Spring Boot Java)_ fixed a deadlock issue
* _(Spring Boot Java)_ reduced number of threads used behind the scenes
* _(Spring Boot Java)_ reduced number of CPU cycles used by live hover mechanism
* _(Spring Boot Java, Spring Boot Properties)_ reduced memory footprint
* _(Spring Boot Properties)_ fixed an issue with wrong indentation after inserting property node

## 2017-12-15 (M7)

* _(all)_ issues solved when running on Windows ([#25](https://github.com/spring-projects/sts4/issues/25), [#26](https://github.com/spring-projects/sts4/issues/26), [#29](https://github.com/spring-projects/sts4/issues/29))
* _(Spring Boot Java)_ live hover information now works for inner classes
* _(Spring Boot Properties)_ boot property editing now activated for bootstrap*.yml files in VSCode automatically

## 2017-12-04

* initial public beta launch
