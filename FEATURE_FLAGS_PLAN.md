# GH-2058: `boot-java.feature-flags` implementation plan

## Goal

One setting, `boot-java.feature-flags`, that maps feature ids to `true` (on) or `false` (off).
An empty object (the default) or a missing id means the feature is on. Unknown ids and non-boolean
values are ignored. Changes apply without a restart.

```json
"boot-java.feature-flags": {
  "value-property-completion": false,
  "bean-name-navigation": false
}
```

## Feature ids

| Id | What it turns off | Spring Tools code |
|---|---|---|
| `value-property-completion` | Property key proposals in `@Value` | `ValueCompletionProcessor` (checked inside the processor) |
| `value-classpath-completion` | `classpath:` resource proposals in `@Value` | `ValueCompletionProcessor` (checked inside the processor) |
| `property-key-navigation` | Definition / references on property keys in `@Value` and `@ConditionalOnProperty` | `ValueDefinitionProvider` (checked inside, its `classpath:` branch belongs to `classpath-resource-navigation`), `ValuePropertyReferencesProvider` |
| `bean-name-navigation` | Definition / references on bean names in `@DependsOn`, `@Qualifier`, `@Resource`, `@Named`, `@ConditionalOn(Missing)Bean` | `BeanNameDefinitionProvider`, `NamedDefinitionProvider`, `ConditionalOnBeanDefinitionProvider`, `QualifierReferencesProvider`, `NamedReferencesProvider` |
| `profile-navigation` | References on profile names in `@Profile` | `ProfileReferencesProvider` |
| `bean-name-completion` | Bean name proposals in `@DependsOn`, `@Qualifier`, `@Resource`, `@Named`, `@ConditionalOn(Missing)Bean(name=…)` | `DependsOnCompletionProcessor`, `QualifierCompletionProvider`, `ResourceCompletionProvider`, `NamedCompletionProvider`, `BeanNamesCompletionProcessor` |
| `bean-type-completion` | Bean type proposals in `@ConditionalOn(Missing)Bean(type=…, ignoredType=…)` | `BeanTypesCompletionProcessor` |
| `profile-completion` | Profile proposals in `@Profile` | `ProfileCompletionProvider` |
| `conditional-on-property-completion` | Key proposals in `@ConditionalOnProperty` | `ConditionalOnPropertyCompletionProcessor` |
| `scope-completion` | Scope proposals in `@Scope` | `ScopeCompletionProcessor` |
| `ignored-problem-fixes` | Quick fixes for problems whose severity is `IGNORE` (offered as refactorings) | `ReconcileProblemCodeActionProvider` |
| `query-text-block-conversion` | "Convert query to text block" code action | `ConvertQueryToTextBlockCodeActionProvider` |
| `context-configuration-completion` | Resource proposals in `@ContextConfiguration` | `ContextConfigurationProcessor` |
| `data-repository-completion` | Query method name proposals in Spring Data repository interfaces | `DataRepositoryCompletionProcessor` |
| `conditional-on-resource-completion` | Resource proposals in `@ConditionalOnResource` | `ConditionalOnResourceCompletionProcessor` |
| `cron-completion` | Cron expression proposals in `@Scheduled` | `CronExpressionCompletionProvider` |
| `java-snippet-completion` | Java snippet proposals (e.g. request mapping methods) | `JavaSnippetManager` (checked inside) |
| `classpath-resource-navigation` | Go to definition on `classpath:` resources in annotation values | `ConditionalOnResourceDefinitionProvider`, `ValueDefinitionProvider` (classpath branch) |
| `data-query-parameter-navigation` | Go to definition on parameters in data queries | `DataQueryParameterDefinitionProvider` |
| `spel-navigation` | Go to definition in SpEL expressions | `SpelDefinitionProvider` |
| `aot-query-method-implementation-navigation` | Go to the generated implementation of query methods | `GenAotQueryMethodImplProvider` |
| `event-navigation` | References on application events | `EventReferenceProvider` |
| `add-query-annotation` | "Add `@Query`" code action | `QueryMethodCodeActionProvider` (checked in `isApplicable`, the bean keeps its concrete type) |
| `sql-dialect-quickfix` | Quick fix to set the SQL dialect for native queries | `SqlDialectQuickFixProvider` |
| `webflux-handler-code-lens` | Code lens on WebFlux handler methods | `WebfluxHandlerCodeLensProvider` |
| `ai-assistant-code-lens` | Code lenses that send queries, SpEL and pointcut expressions to an AI assistant | `CopilotCodeLensProvider` |
| `router-function-code-lens` | Code lens on functional router methods (convert to builder pattern) | `RouterFunctionCodeLensProvider` |
| `data-query-aot-code-lens` | Code lens with the AOT generated query on Spring Data query methods | `DataRepositoryAotMetadataCodeLensProvider` |
| `web-config-code-lens` | Code lens with web config details on controller classes | `WebConfigCodeLensProvider` |
| `properties-hover` | Hovers on property keys in `application.properties` | `BootPropertiesLanguageServerComponents.getHoverProvider` (checked inside) |
| `yaml-hover` | Hovers on property keys in `application.yml` | `BootPropertiesLanguageServerComponents.getHoverProvider` (checked inside) |
| `webflux-route-highlight` | Highlights of WebFlux route parts | `WebfluxRouteHighlightProdivder` (wrapped in `createDocumentHighlightEngine`) |
| `data-query-highlight` | Highlights in data queries | `JdtQueryDocHighlightsProvider` (wrapped when the handler is built) |
| `data-query-inlay-hints` | Parameter hints for ordinal parameters in data queries | `JdtDataQueriesInlayHintsProvider` (wrapped when the handler is built) |
| `pom-inlay-hints` | Spring Boot version and starter inlay hints in `pom.xml` | `PomInlayHintHandler` (wrapped in `PomLanguageServerComponents`) |
| `data-query-semantic-tokens` | Highlighting of data queries (Java and named query properties files) | `JdtDataQuerySemanticTokensProvider`, `QueryPropertiesSemanticTokensHandler` (wrapped) |
| `spel-semantic-tokens` | Highlighting of SpEL in Java | `JdtSpelSemanticTokensProvider` (wrapped when the handler is built) |
| `cron-semantic-tokens` | Highlighting of cron expressions (Java, properties, YAML) | `JdtCronSemanticTokensProvider`, `CronPropertiesSemanticTokensHandler` (wrapped) |
| `properties-completion` | Proposals in `application.properties` | `SpringPropertiesCompletionEngine` (checked inside) |
| `yaml-completion` | Proposals in `application.yml` | `SpringYamlCompletionEngine` (checked inside) |
| `properties-navigation` | Go to definition in `application.properties` | `PropertiesJavaDefinitionHandler` (checked inside) |
| `yaml-navigation` | Go to definition in `application.yml` | `YamlPropertiesJavaDefinitionHandler` (checked inside) |
| `document-symbols` | Document symbols of Java, Spring XML and `spring.factories` files (retrieval only, indexing stays on) | document symbol handlers in `BootJavaLanguageServerComponents`, `SpringXMLLanguageServerComponents`, `SpringFactoriesLanguageServerComponents` (wrapped) |
| `workspace-symbols` | Workspace symbol search (retrieval only, indexing stays on; the `//` live app URL queries are not covered) | `BootJavaWorkspaceSymbolHandler` (checked inside) |

Ids are part of the settings contract and must stay stable once released.

## Implementation steps

1. **`FeatureFlagIds` + `FeatureFlag`** (`boot/app`): `FeatureFlagIds` holds one documented String constant per
   id (the list of valid ids); `FeatureFlag` is the enum built from them, with `fromId(String)`. A test keeps
   the two in sync.
2. **`BootJavaConfig.isFeatureEnabled(FeatureFlag)`**: reads `boot-java.feature-flags` from the current
   settings on every call; a feature is off only if its value is boolean `false`.
3. **`FeatureFlagGuards`**: static `onlyWhen(config, flag, delegate)` decorators for `CompletionProvider`,
   `AnnotationAttributeCompletionProvider`, `IJavaLocationLinksProvider`, `ReferenceProvider`.
4. **Wire up registrations**:
   - Completion: `BootJavaCompletionEngineConfigurer` (wrap the inner per-attribute providers);
     `ValueCompletionProcessor` gets `BootJavaConfig` and skips property-key proposals when off.
   - Definition: `BootLanguageServerBootApp.javaDefinitionHandler`.
   - References: `BootJavaLanguageServerComponents.createReferenceHandler`.
   - Code actions: `ReconcileProblemCodeActionProvider` and `ConvertQueryToTextBlockCodeActionProvider`
     get `BootJavaConfig` and return `false` from `isApplicable` when off (`isApplicable` is evaluated
     per request in `JdtCodeActionHandler`).
5. **VS Code `package.json`**: declare `boot-java.feature-flags` as `type: object`,
   `additionalProperties: boolean`, `default: {}`, with a description listing the ids. No client code
   changes (`boot-java` is already a synchronized configuration section). No Settings UI entry per feature.
6. **Tests**: unit test for `isFeatureEnabled`; per-id tests in the existing feature test classes using
   `harness.changeConfiguration(...)`, asserting the feature no longer answers (and that the other kind of
   `@Value` proposals remains).
7. **Housekeeping**: copyright year in touched files, EPL header + `@author` on new classes, changelog.

## Status

Steps 1-7 are implemented. Note for tests: a configuration change re-indexes the project, so tests that
change the flags wait for `indexer.waitOperation()` afterwards and re-apply beans injected via
`springIndex.updateBeans(...)`.

## Decisions

- No feature flags for the live data hovers (`ValueHoverProvider`, `RequestMappingHoverProvider`, `ActiveProfilesProvider`,
  `AutowiredHoverProvider`, `ComponentInjectionsHoverProvider`, `BeanInjectedIntoHoverProvider`, `ConditionalsLiveHoverProvider`).
  `BootJavaHoverProvider` only calls them while live data from a connected process exists. Live data is switched off as a
  whole (not connecting, `boot-java.live-information.automatic-connection.on`, `boot-java.remote-apps`,
  `boot-java.highlight-codelens.on`). This is documented in `FEATURE_FLAGS.md`.

- Inlay hints for cron expressions have no flag: `boot-java.cron.inlay-hints` already switches them. Embedded language
  highlighting is switched as a whole by `boot-java.embedded-syntax-highlighting`, JPA query support by `boot-java.jpql`.
  The general Java semantic tokens (`JavaSemanticTokensProvider`, a copy of JDT LS) have no flag. The semantic token,
  inlay hint and highlight providers are discovered as beans, so they are wrapped where the handlers are built
  (the data query hints and highlights depend on the concrete token provider bean, which must not be wrapped).

- Property file problems and their quick fixes have no flags (per-problem severity settings, `IGNORE`); the
  properties/YAML conversion commands are explicit client actions and have no flag.

## Open points to verify manually

- VS Code merges object-valued settings across extension defaults / user / workspace, so a user can
  re-enable a single id without restating the rest.
- Eclipse client builds its settings by hand (`DelegatingStreamConnectionProvider`) and does not send
  the flags yet; empty means everything is on, so no change is needed now.
- Standalone LS / Claude plugin reads nested JSON settings; the flags should work there without changes.
