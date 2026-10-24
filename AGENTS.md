# Repository Guidelines

## Project Structure & Module Organization

- `headless-services/`: Maven Java language servers and shared `commons/` modules. Sources use `src/main/java`; tests and fixtures use `src/test/java` and `src/test/resources`.
- `eclipse-extensions/`, `eclipse-language-servers/`, and `eclipse-distribution/`: Eclipse integrations and Tycho builds.
- `vscode-extensions/`: TypeScript extensions, with implementation under each extension’s `lib/` directory and extension-specific icons and language assets.
- `agent-plugins/spring-tools/`: shared MCP launcher, skills, host adapters, and diagnostic playbooks. `agent-plugins/tools/` contains development checks and tests.
- `.agents/plugins/marketplace.json`: Codex catalog pointing to the shared plugin; it is not a second implementation.

## Build, Test, and Development Commands

Run commands from the repository root unless shown otherwise:

```bash
cd headless-services && ./mvnw clean install
```

Builds Java modules and runs tests. For focused work, use `./mvnw test -pl spring-boot-language-server -am` from `headless-services/`.

```bash
npm --prefix vscode-extensions/vscode-spring-boot run compile
npm --prefix vscode-extensions/vscode-spring-boot run lint
npm --prefix agent-plugins/tools run check
npm --prefix agent-plugins/tools test
./agent-plugins/update-local-jars.sh
```

These compile/lint the VS Code extension, validate plugin configuration and playbooks, run plugin tests, and build the local standalone server JAR. Install extension dependencies before compiling. See [the development guide](README-dev-env.md) for build profiles and debugging and [the plugin guide](agent-plugins/spring-tools/README.md) for local testing.

## Coding Style & Naming Conventions

Match surrounding indentation and formatting; plugin JavaScript uses four spaces. Use `UpperCamelCase` Java classes, `lowerCamelCase` methods, and diagnostic-code filenames such as `explanations/JAVA_PUBLIC_BEAN_METHOD.md`. Run ESLint and `npm run check-types` for TypeScript changes. Prefer JDT refactorings for new Java quick fixes. Preserve EPL headers, update copyright years when modifying source, and add author documentation as described in [CONTRIBUTING.adoc](CONTRIBUTING.adoc).

## Testing Guidelines

Java tests use JUnit Jupiter; plugin tests use Node’s `node:test` with `*.test.mjs` filenames. Add regression tests for behavior changes, including failure cases. Every diagnostic needs a playbook and coverage-table entry; every MCP tool needs skill/hook coverage and an eval mock. Maintain eval cases for skills and reviewer agents. Run the MCP smoke test after launcher, installer, hook, or standalone-server changes; it requires a built JAR and Java 21+.

## Commit & Pull Request Guidelines

Recent commits use imperative subjects such as “Fix MCP startup.” Keep commits focused and include `Fixes gh-XXXX` when resolving an issue. Follow CONTRIBUTING’s DCO requirement: include a `Signed-off-by` trailer (`git commit -s`). PR descriptions should explain the problem, resulting behavior, linked issues, and validation performed. Include screenshots for visible UI changes. Run `./nohttp.sh` for URL checks. Follow [RELEASE.md](RELEASE.md) for publishing.
