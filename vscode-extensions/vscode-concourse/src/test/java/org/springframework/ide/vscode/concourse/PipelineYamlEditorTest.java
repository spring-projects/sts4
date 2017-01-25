/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse;

import static org.junit.Assert.assertEquals;
import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.assertContains;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class PipelineYamlEditorTest {

	private static final String CURSOR = "<*>";
	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(ConcourseLanguageServer::new);
		harness.intialize(null);
	}

	@Test public void testReconcileCatchesParseError() throws Exception {
		Editor editor = harness.newEditor(
				"somemap: val\n"+
				"- sequence"
		);
		editor.assertProblems(
				"-|expected <block end>"
		);
	}

	@Test public void reconcileRunsOnDocumentOpenAndChange() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ConcourseLanguageServer::new);
		harness.intialize(null);

		Editor editor = harness.newEditor(
				"somemap: val\n"+
				"- sequence"
		);

		editor.assertProblems(
				"-|expected <block end>"
		);

		editor.setText(
				"- sequence\n" +
				"zomemap: val"
		);

		editor.assertProblems(
				"z|expected <block end>"
		);
	}

	@Test
	public void reconcileMisSpelledPropertyNames() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resorces:\n" +
				"- name: git\n" +
				"  type: git\n"
		);
		editor.assertProblems("resorces|Unknown property");
	}

	@Test
	public void reconcileAcceptsSensiblePipelineFile() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				getClasspathResourceText("workspace/pipeline.yml")
		);
		editor.assertProblems(/*NONE*/);;
	}

	private String getClasspathResourceText(String resourceName) throws Exception {
		InputStream stream = PipelineYamlEditorTest.class.getClassLoader().getResourceAsStream(resourceName);
		return IOUtil.toString(stream);
	}

	@Test
	public void reconcileStructuralProblems() throws Exception {
		Editor editor;

		//resources should be a sequence not a map, even if there's only one entry
		editor = harness.newEditor(
				"resources:\n" +
				"  name: git\n" +
				"  type: git\n"
		);
		editor.assertProblems(
				"name: git\n  type: git|Expecting a 'Sequence' but found a 'Map'"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - task: a-task\n" +
				"    tags: a-single-string\n"
		);
		editor.assertProblems(
				"a-single-string|Expecting a 'Sequence'"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - try:\n" +
				"      put: a-resource\n"
		);
		editor.assertProblems(
				"a-resource|does not exist"
		);

		//TODO: Add more test cases for structural problem?
	}

	@Test
	public void primaryStepCompletions() throws Exception {
		assertContextualCompletions(
				// Context:
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - <*>"
				, // ==============
				"<*>"
				, // =>
				"aggregate:\n" +
				"    - <*>"
				, // ==============
				"do:\n" +
				"    - <*>"
				, // ==============
				"get: <*>"
				, // ==============
				"put: <*>"
				, // ==============
				"task: <*>"
				, // ==============
				"try:\n" +
				"      <*>"
		);
	}

	@Test
	public void PT_136196057_do_step_completion_indentation() throws Exception {
		assertCompletions(
				"jobs:\n" +
				"- name:\n"+
				"  plan:\n" +
				"  - do<*>"
				, // =>
				"jobs:\n" +
				"- name:\n"+
				"  plan:\n" +
				"  - do:\n" +
				"    - <*>"
		);
	}

	@Test
	public void primaryStepHovers() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - get: something\n" +
				"  - put: something\n" +
				"  - do: []\n" +
				"  - aggregate:\n" +
				"    - task: perform-something\n" +
				"  - try:\n" +
				"      put: test-logs\n"
		);

		editor.assertHoverContains("get", "Fetches a resource");
		editor.assertHoverContains("put", "Pushes to the given [Resource]");
		editor.assertHoverContains("aggregate", "Performs the given steps in parallel");
		editor.assertHoverContains("task", "Executes a [Task]");
		editor.assertHoverContains("do", "performs the given steps serially");
		editor.assertHoverContains("try", "Performs the given step, swallowing any failure");
	}

	@Test
	public void putStepHovers() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - put: something\n" +
				"    resource: something\n" +
				"    params:\n" +
				"      some_param: some_value\n" +
				"    get_params:\n" +
				"      skip_download: true\n"
		);

		editor.assertHoverContains("resource", "The resource to update");
		editor.assertHoverContains("params", "A map of arbitrary configuration");
		editor.assertHoverContains("get_params", "A map of arbitrary configuration to forward to the resource that will be utilized during the implicit `get` step");
	}

	@Test
	public void getStepHovers() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - get: something\n" +
				"    resource: something\n" +
				"    version: latest\n" +
				"    passed: [other-job]\n" +
				"    params:\n" +
				"      some_param: some_value\n" +
				"    trigger: true\n" +
				"    attempts: 10\n" +
				"    on_failure:\n" +
				"    - bogus: bad\n" +
				"    on_success:\n" +
				"    - bogus: bad\n" +
				"    ensure:\n" +
				"      task: cleanups\n"
		);
		editor.assertHoverContains("resource", "The resource to fetch");
		editor.assertHoverContains("version", "The version of the resource to fetch");
		editor.assertHoverContains("params", "A map of arbitrary configuration");
		editor.assertHoverContains("trigger", "Set to `true` to auto-trigger");
		editor.assertHoverContains("attempts", "Any step can set the number of times it should be attempted");
		editor.assertHoverContains("on_failure", "Any step can have `on_failure` tacked onto it");
		editor.assertHoverContains("on_success", "Any step can have `on_success` tacked onto it");
		editor.assertHoverContains("ensure", "a second step to execute regardless of the result of the parent step");
	}

	@Test
	public void groupHovers() throws Exception {
		Editor editor = harness.newEditor(
				"groups:\n" +
				"- name: some-group\n" +
				"  resources: []\n" +
				"  jobs: []\n"
		);
		editor.assertHoverContains("name", "The name of the group");
		editor.assertHoverContains("resources", "A list of resources that should appear in this group");
		editor.assertHoverContains("jobs", " A list of jobs that should appear in this group");
	}

	@Test
	public void taskStepHovers() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - task: do-something\n" +
				"    file: some-file.yml\n" +
				"    privileged: true\n" +
				"    image: some-image\n" +
				"    params:\n" +
				"      map: of-stuff\n" +
				"    input_mapping:\n" +
				"      map: of-stuff\n" +
				"    output_mapping:\n" +
				"      map: of-stuff\n" +
				"    config: some-config\n" +
				"    tags: [a, b, c]\n"+
				"    attempts: 10\n" +
				"    timeout: 1h30m\n" +
				"    ensure:\n" +
				"      bogus: bad\n" +
				"    on_failure:\n" +
				"      bogus: bad\n" +
				"    on_success:\n" +
				"      bogus: bad\n"
		);
		editor.assertHoverContains("file", "`file` points at a `.yml` file containing the task config");
		editor.assertHoverContains("privileged", "If set to `true`, the task will run with full capabilities");
		editor.assertHoverContains("image", "Names an artifact source within the plan");
		editor.assertHoverContains("params", "A map of task parameters to set, overriding those configured in `config` or `file`");
		editor.assertHoverContains("input_mapping", "A map from task input names to concrete names in the build plan");
		editor.assertHoverContains("output_mapping", "A map from task output names to concrete names");
		editor.assertHoverContains("config", "Use `config` to inline the task config");
		editor.assertHoverContains("tags", "Any step can be directed at a pool of workers");
		editor.assertHoverContains("timeout", "amount of time to limit the step's execution");
	}

	@Test
	public void aggregateStepHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: some-job\n" +
				"  plan:\n" +
				"  - aggregate:\n" +
				"    - get: some-resource\n"
		);

		editor.assertHoverContains("aggregate", "Performs the given steps in parallel");
	}

	@Test
	public void reconcileSimpleTypes() throws Exception {
		Editor editor;

		//check for 'format' errors:
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: boohoo\n" +
				"  max_in_flight: -1\n" +
				"  plan:\n" +
				"  - get: git\n" +
				"    trigger: yohoho\n" +
				"    attempts: 0\n" +
				"    timeout: 1h:30m\n"
		);
		editor.assertProblems(
				"boohoo|boolean",
				"-1|must be positive",
				"git|resource does not exist",
				"yohoho|boolean",
				"0|must be at least 1",
				"1h:30m|Duration"
		);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"resources:\n" +
				"- name: git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: true\n" +
				"  max_in_flight: 2\n" +
				"  plan:\n" +
				"  - get: git\n" +
				"    trigger: true"
		);
		editor.assertProblems(/*none*/);

	}

	@Test
	public void noListIndent() throws Exception {
		Editor editor;
		editor = harness.newEditor("jo<*>");
		editor.assertCompletions(
				"jobs:\n"+
				"- <*>"
		);
	}

	@Test
	public void toplevelCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor(CURSOR);
		editor.assertCompletions(
				"groups:\n" +
				"- <*>"
				, // --------------
				"jobs:\n" +
				"- <*>"
				, // ---------------
				"resource_types:\n" +
				"- <*>"
				, // ---------------
				"resources:\n"+
				"- <*>"
		);

		editor = harness.newEditor("rety<*>");
		editor.assertCompletions(
				"resource_types:\n" +
				"- <*>"
		);
	}

	@Test
	public void valueCompletions() throws Exception {
		String [] builtInResourceTypes = {
				"git", "hg", "time", "s3",
				"archive", "semver", "github-release",
				"docker-image", "tracker", "pool", "cf", "bosh-io-release",
				"bosh-io-stemcell", "bosh-deployment", "vagrant-cloud"
		};
		Arrays.sort(builtInResourceTypes);

		String[] expectedCompletions = new String[builtInResourceTypes.length];
		for (int i = 0; i < expectedCompletions.length; i++) {
			expectedCompletions[i] =
					"resources:\n" +
					"- type: "+builtInResourceTypes[i]+"<*>";
		}

		assertCompletions(
				"resources:\n" +
				"- type: <*>"
				, //=>
				expectedCompletions
		);
		assertCompletions(
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: <*>"
				, // =>
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: false<*>"
				, // --
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: true<*>"
		);
	}

	@Test
	public void topLevelHoverInfos() throws Exception {
		Editor editor = harness.newEditor(
			"resource_types:\n" +
			"- name: s3-multi\n" +
			"  type: docker-image\n" +
			"  source:\n" +
			"    repository: kdvolder/s3-resource-simple\n" +
			"resources:\n" +
			"- name: docker-git\n" +
			"  type: git\n" +
			"  source:\n" +
			"    uri: git@github.com:spring-projects/sts4.git\n" +
			"    branch: {{branch}}\n" +
			"    username: kdvolder\n" +
			"    private_key: {{rsa_id}}\n" +
			"    paths:\n" +
			"    - concourse/docker\n" +
			"jobs:\n" +
			"- name: build-docker-image\n" +
			"  serial: true\n" +
			"  plan:\n" +
			"  - get: docker-git\n" +
			"    trigger: true\n" +
			"  - put: docker-image\n" +
			"    params:\n" +
			"      build: docker-git/concourse/docker\n" +
			"    get_params: \n" +
			"      skip_download: true\n" +
			"groups:\n" +
			"- name: a-groups\n"
		);

		editor.assertHoverContains("resource_types", "each pipeline can configure its own custom types by specifying `resource_types` at the top level.");
		editor.assertHoverContains("resources", "A resource is any entity that can be checked for new versions");
		editor.assertHoverContains("jobs", "At a high level, a job describes some actions to perform");
		editor.assertHoverContains("groups", "A pipeline may optionally contain a section called `groups`");
	}

	@Test
	public void reconcileResourceReferences() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: sts4\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: https://github.com/kdvolder/somestuff\n" +
				"    branch: master\n" +
				"jobs:\n" +
				"- name: job1\n" +
				"  plan:\n" +
				"  - get: sts4\n" +
				"  - get: bogus-get\n" +
				"  - task: do-stuff\n" +
				"    input_mapping:\n" +
				"      task-input: bogus-input\n" +
				"      repo: sts4\n" +
				"  - put: bogus-put\n"
		);
		editor.assertProblems(
				"bogus-get|resource does not exist",
				"bogus-input|resource does not exist",
				"bogus-put|resource does not exist"
		);

		editor.assertProblems(
				"bogus-get|[sts4]",
				"bogus-input|[sts4]",
				"bogus-put|[sts4]"
		);
	}

	@Test
	public void reconcileDuplicateResourceNames() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: sts4\n" +
				"  type: git\n" +
				"- name: utils\n" +
				"  type: git\n" +
				"- name: sts4\n" +
				"  type: git\n"
		);
		editor.assertProblems(
				"sts4|Duplicate resource name",
				"sts4|Duplicate resource name"
		);
	}

	@Test
	public void reconcileDuplicateResourceTypeNames() throws Exception {
		Editor editor = harness.newEditor(
				"resource_types:\n" +
				"- name: slack-notification\n" +
				"  type: docker_image\n" +
				"- name: slack-notification\n" +
				"  type: docker_image"
		);
		editor.assertProblems(
				"slack-notification|Duplicate resource-type name",
				"slack-notification|Duplicate resource-type name"
		);
	}

	@Test
	public void reconcileDuplicateJobNames() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: job-1\n" +
				"- name: utils\n" +
				"- name: job-1\n"
		);
		editor.assertProblems(
				"name: job-1|'plan' is required",
				"job-1|Duplicate job name",
				"name: utils|'plan' is required",
				"name: job-1|'plan' is required",
				"job-1|Duplicate job name"
		);
	}

	@Test
	public void completionsResourceReferences() throws Exception {
		assertContextualCompletions(
				"resources:\n" +
				"- name: sts4\n" +
				"- name: repo-a\n" +
				"- name: repo-b\n" +
				"jobs:\n" +
				"- name: job1\n" +
				"  plan:\n" +
				"  - get: <*>\n"
				, ////////////////////
				"<*>"
				, // =>
				"repo-a<*>", "repo-b<*>", "sts4<*>"
		);

		assertContextualCompletions(
				"resources:\n" +
				"- name: sts4\n" +
				"- name: repo-a\n" +
				"- name: repo-b\n" +
				"jobs:\n" +
				"- name: job1\n" +
				"  plan:\n" +
				"  - put: <*>\n"
				, ////////////////////
				"r<*>"
				, // =>
				"repo-a<*>", "repo-b<*>"
		);
	}

	@Test
	public void reconcileDuplicateKeys() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    branch: master\n" +
				"    uri: https://github.com/kdvolder/my-repo\n" +
				"resources:\n" +
				"- name: your-repo\n" +
				"  type: git\n" +
				"  type: git\n" +
				"  source:\n" +
				"    branch: master\n" +
				"    uri: https://github.com/kdvolder/forked-repo\n"
		);

		editor.assertProblems(
				"resources|Duplicate key",
				"resources|Duplicate key",
				"type|Duplicate key",
				"type|Duplicate key"
		);
	}

	@Test
	public void reconcileJobNames() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: git-repo\n" +
				"  type: git\n" +
				"- name: build-artefact\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: build\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"  - task: run-build\n" +
				"  - put: build-artefact\n" +
				"- name: test\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"    passed:\n" +
				"    - not-a-job\n" +
				"    - build\n"
		);

		editor.assertProblems("not-a-job|does not exist");
	}

	@Test
	public void reconcileGroups() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: git-repo\n" +
				"  type: git\n" +
				"- name: build-artefact\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: build\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"  - task: run-build\n" +
				"  - put: build-artefact\n" +
				"- name: test\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"groups:\n" +
				"- name: some-group\n" +
				"  jobs: [build, test, bogus-job]\n" +
				"  resources: [git-repo, build-artefact, not-a-resource]"
		);

		editor.assertProblems(
				"bogus-job|does not exist",
				"not-a-resource|does not exist"
		);
	}

	@Test public void gitResourceSourceReconcile() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: sts4-out\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: git@github.com:spring-projects/sts4.git\n" +
				"    bogus: bad\n" +
				"    branch: {{branch}}\n" +
				"    private_key: {{rsa_id}}\n" +
				"    username: jeffy\n" +
				"    password: {{git_passwords}}\n" +
				"    paths: not-a-list\n" +
				"    ignore_paths: also-not-a-list\n" +
				"    skip_ssl_verification: skip-it\n" +
				"    tag_filter: RELEASE_*\n" +
				"    git_config:\n" +
				"    - name: good\n" +
				"      val: bad\n" +
				"    disable_ci_skip: no_ci_skip\n" +
				"    commit_verification_keys: not-a-list-of-keys\n" +
				"    commit_verification_key_ids: not-a-list-of-ids\n" +
				"    gpg_keyserver: hkp://somekeyserver.net"
		);
		editor.assertProblems(
				"bogus|Unknown property",
				"not-a-list|Expecting a 'Sequence'",
				"also-not-a-list|Expecting a 'Sequence'",
				"skip-it|'boolean'",
				"val|Unknown property",
				"no_ci_skip|'boolean'",
				"not-a-list-of-keys|Expecting a 'Sequence'",
				"not-a-list-of-ids|Expecting a 'Sequence'"
		);
	}

	@Test public void gitResourceSourceCompletions() throws Exception {
		assertContextualCompletions(
				"resources:\n" +
				"- name: the-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    <*>"
				, //================
				"<*>"
				, // ==>
				"branch: <*>"
				,
				"commit_verification_key_ids:\n" +
				"    - <*>"
				,
				"commit_verification_keys:\n" +
				"    - <*>"
				,
				"disable_ci_skip: <*>"
				,
				"git_config:\n" +
				"    - <*>"
				,
				"gpg_keyserver: <*>"
				,
				"ignore_paths:\n" +
				"    - <*>"
				,
				"password: <*>"
				,
				"paths:\n" +
				"    - <*>"
				,
				"private_key: <*>"
				,
				"skip_ssl_verification: <*>"
				,
				"tag_filter: <*>"
				,
				"uri: <*>"
				,
				"username: <*>"
		);

		assertContextualCompletions(
				"resources:\n" +
				"- name: the-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    git_config:\n" +
				"    - <*>"
				, // =============
				"<*>"
				, // ==>
				"name: <*>",
				"value: <*>"
		);
	}

	@Test public void gitResourceSourceHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: sts4-out\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: git@github.com:spring-projects/sts4.git\n" +
				"    bogus: bad\n" +
				"    branch: {{branch}}\n" +
				"    private_key: {{rsa_id}}\n" +
				"    username: jeffy\n" +
				"    password: {{git_passwords}}\n" +
				"    paths: not-a-list\n" +
				"    ignore_paths: also-not-a-list\n" +
				"    skip_ssl_verification: skip-it\n" +
				"    tag_filter: RELEASE_*\n" +
				"    git_config:\n" +
				"    - name: good\n" +
				"      val: bad\n" +
				"    disable_ci_skip: no_ci_skip\n" +
				"    commit_verification_keys: not-a-list-of-keys\n" +
				"    commit_verification_key_ids: not-a-list-of-ids\n" +
				"    gpg_keyserver: hkp://somekeyserver.net"
		);
		editor.assertHoverContains("uri", "*Required.* The location of the repository.");
		editor.assertHoverContains("branch", "The branch to track");
		editor.assertHoverContains("private_key", "Private key to use when pulling/pushing");
		editor.assertHoverContains("username", "Username for HTTP(S) auth");
		editor.assertHoverContains("password", "Password for HTTP(S) auth");
		editor.assertHoverContains("paths", "a list of glob patterns");
		editor.assertHoverContains("ignore_paths", "The inverse of `paths`");
		editor.assertHoverContains("skip_ssl_verification", "Skips git ssl verification");
		editor.assertHoverContains("tag_filter", "the resource will only detect commits");
		editor.assertHoverContains("git_config", "configure git global options");
		editor.assertHoverContains("disable_ci_skip", "Allows for commits that have been labeled with `[ci skip]`");
		editor.assertHoverContains("commit_verification_keys", "Array of GPG public keys");
		editor.assertHoverContains("commit_verification_key_ids", "Array of GPG public key ids");
	}

	@Test public void gitResourceGetParamsCompletions() throws Exception {
		String context =
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"    params:\n" +
				"      <*>";

		assertContextualCompletions(context,
				"<*>"
				, // ===>
				"depth: <*>"
				,
				"disable_git_lfs: <*>"
				,
				"submodules:\n"+
				"        <*>"
		);
		assertContextualCompletions(context,
				"disable_git_lfs: <*>"
				, // ===>
				"disable_git_lfs: false<*>",
				"disable_git_lfs: true<*>"
		);
		assertContextualCompletions(context,
				"submodules: <*>"
				, // ===>
				"submodules: all<*>",
				"submodules: none<*>"
		);
	}

	@Test public void gitResourceGetParamsHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"    params:\n" +
				"      depth: -1\n" +
				"      disable_git_lfs: not-bool\n" +
				"      submodules: none"
		);
		editor.assertHoverContains("depth", "using the `--depth` option");
		editor.assertHoverContains("submodules", "If `none`, submodules will not be fetched");
		editor.assertHoverContains("disable_git_lfs", "will not fetch Git LFS files");
	}

	@Test public void gitResourceGetParamsReconcile() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"    params:\n" +
				"      depth: -1\n" +
				"      disable_git_lfs: not-bool\n"
		);
		editor.assertProblems(
				"-1|must be positive",
				"not-bool|'boolean'"
		);
	}

	@Test public void gitResourcePutParamsCompletions() throws Exception {
		String context =
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params:\n" +
				"      <*>";

		assertContextualCompletions(context,
				"<*>"
				, // ===>
				"annotate: <*>"
				,
				"force: <*>"
				,
				"only_tag: <*>"
				,
				"rebase: <*>"
				,
				"repository: <*>"
				,
				"tag: <*>"
				,
				"tag_prefix: <*>"
		);
		assertContextualCompletions(context,
				"rebase: <*>"
				, // ===>
				"rebase: false<*>",
				"rebase: true<*>"
		);
		assertContextualCompletions(context,
				"only_tag: <*>"
				, // ===>
				"only_tag: false<*>",
				"only_tag: true<*>"
		);
		assertContextualCompletions(context,
				"force: <*>"
				, // ===>
				"force: false<*>",
				"force: true<*>"
		);
	}

	@Test public void gitResourcePutParamsReconcile() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params: {}\n"
		);
		editor.assertProblems("{}|'repository' is required");

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params:\n" +
				"      repository: some-other-repo\n" +
				"      rebase: do-rebase\n" +
				"      only_tag: do-tag\n" +
				"      force: force-it\n"
		);
		editor.assertProblems(
				"do-rebase|'boolean'",
				"do-tag|'boolean'",
				"force-it|'boolean'"
		);
	}

	@Test public void gitResourcePutParamsHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params:\n" +
				"      repository: some-other-repo\n" +
				"      rebase: do-rebase\n" +
				"      tag: the-tag-file\n" +
				"      only_tag: do-tag\n" +
				"      tag_prefix: RELEASE\n" +
				"      force: force-it\n" +
				"      annotate: release-annotion\n"
		);

		editor.assertHoverContains("repository", "The path of the repository");
		editor.assertHoverContains("rebase", "attempt rebasing");
		editor.assertHoverContains("tag", "HEAD will be tagged");
		editor.assertHoverContains("only_tag", "push only the tags");
		editor.assertHoverContains("tag_prefix", "prepended with this string");
		editor.assertHoverContains("force", "pushed regardless of the upstream state");
		editor.assertHoverContains("annotate", "path to a file containing the annotation message");
	}

	@Test public void gitResourcePut_get_params_Hovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    get_params:\n" +
				"      depth: 1\n" +
				"      submodules: none\n" +
				"      disable_git_lfs: true\n"
		);

		editor.assertHoverContains("depth", "using the `--depth` option");
		editor.assertHoverContains("submodules", "If `none`, submodules will not be fetched");
		editor.assertHoverContains("disable_git_lfs", "will not fetch Git LFS files");
	}

	@Test
	public void contentAssistJobNames() throws Exception {
		assertContextualCompletions(
				"resources:\n" +
				"- name: git-repo\n" +
				"- name: build-artefact\n" +
				"jobs:\n" +
				"- name: build\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"  - task: run-build\n" +
				"  - put: build-artefact\n" +
				"- name: test\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"    passed:\n" +
				"    - <*>\n"
				, ///////////////////////////
				"<*>"
				, // =>
				"build<*>",
				"test<*>"
		);
	}

	@Test
	public void resourceTypeAttributeHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resource_types:\n" +
				"- name: s3-multi\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/s3-resource-simple\n"
		);

		editor.assertHoverContains("name", "This name will be referenced by `resources` defined within the same pipeline");
		editor.assertHoverContains("type", 2, "used to provide the resource type's container image");
		editor.assertHoverContains("source", 2, "The location of the resource type's resource");
	}

	@Test
	public void resourceAttributeHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: sts4\n" +
				"  type: git\n" +
				"  check_every: 5m\n" +
				"  source:\n" +
				"    repository: https://github.com/spring-projects/sts4\n"
		);

		editor.assertHoverContains("name", "The name of the resource");
		editor.assertHoverContains("type", "The type of the resource. Each worker advertises");
		editor.assertHoverContains("source", 2, "The location of the resource");
		editor.assertHoverContains("check_every", "The interval on which to check for new versions");
	}

	@Test public void requiredPropertiesReconcile() throws Exception {
		Editor editor;

		//addProp(resource, "name", resourceNameDef).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- type: git"
		);
		editor.assertProblems("type: git|'name' is required");

		//addProp(resource, "type", t_resource_type_name).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo"
		);
		editor.assertProblems("name: foo|'type' is required");

		//Both name and type missing:
		editor = harness.newEditor(
				"resources:\n" +
				"- source: {}"
		);
		editor.assertProblems("source: {}|[name, type] are required");

		//addProp(job, "name", jobNameDef).isRequired(true);
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo"
		);
		editor.assertProblems("name: foo|'plan' is required");

		//addProp(job, "plan", f.yseq(step)).isRequired(true);
		editor = harness.newEditor(
				"jobs:\n" +
				"- plan: []"
		);
		editor.assertProblems("plan: []|'name' is required");

		//addProp(resourceType, "name", t_ne_string).isRequired(true);
		editor = harness.newEditor(
				"resource_types:\n" +
				"- type: docker-image"
		);
		editor.assertProblems("type: docker-image|'name' is required");

		//addProp(resourceType, "type", t_image_type).isRequired(true);
		editor = harness.newEditor(
				"resource_types:\n" +
				"- name: foo"
		);
		editor.assertProblems("name: foo|'type' is required");

		//addProp(gitSource, "uri", t_string).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    branch: master"
		);
		editor.assertProblems("branch: master|'uri' is required");

		//addProp(gitSource, "branch", t_string).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: https://yada"
		);
		editor.assertProblems("uri: https://yada|'branch' is required");

		//addProp(group, "name", t_ne_string).isRequired(true);
		editor = harness.newEditor(
				"groups:\n" +
				"- jobs: []"
		);
		editor.assertProblems("jobs: []|'name' is required");
	}


	@Test public void dockerImageResourceSourceReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-docker-image\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    tag: latest\n"
		);
		editor.assertProblems("tag: latest|'repository' is required");

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-docker-image\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/sts4-build-env\n" +
				"    tag: latest\n" +
				"    username: kdvolder\n" +
				"    password: {{docker_password}}\n" +
				"    aws_access_key_id: {{aws_access_key}}\n" +
				"    aws_secret_access_key: {{aws_secret_key}}\n" +
				"    insecure_registries: no-list\n" +
				"    registry_mirror: https://my-docker-registry.com\n" +
				"    ca_certs:\n" +
				"    - domain: example.com:443\n" +
				"      cert: |\n" +
				"        -----BEGIN CERTIFICATE-----\n" +
				"        ...\n" +
				"        -----END CERTIFICATE-----\n" +
				"      bogus_ca_certs_prop: bad\n" +
				"    client_certs:\n" +
				"    - domain: example.com:443\n" +
				"      cert: |\n" +
				"        -----BEGIN CERTIFICATE-----\n" +
				"        ...\n" +
				"        -----END CERTIFICATE-----\n" +
				"      key: |\n" +
				"        -----BEGIN RSA PRIVATE KEY-----\n" +
				"        ...\n" +
				"        -----END RSA PRIVATE KEY-----\n" +
				"      bogus_client_cert_prop: bad\n"
		);
		editor.assertProblems(
				"no-list|Expecting a 'Sequence'",
				"bogus_ca_certs_prop|Unknown property", //ca_certs
				"bogus_client_cert_prop|Unknown property" //client_certs
		);

		editor.assertHoverContains("repository", "The name of the repository");
		editor.assertHoverContains("tag", "The tag to track");
		editor.assertHoverContains("username", "username to authenticate");
		editor.assertHoverContains("password", "password to use");
		editor.assertHoverContains("aws_access_key_id",  "AWS access key to use");
		editor.assertHoverContains("aws_secret_access_key", "AWS secret key to use");
		editor.assertHoverContains("insecure_registries", "array of CIDRs");
		editor.assertHoverContains("registry_mirror", "URL pointing to a docker registry");
		editor.assertHoverContains("ca_certs", "Each entry specifies the x509 CA certificate for");
		editor.assertHoverContains("client_certs", "Each entry specifies the x509 certificate and key");
	}

	@Test public void dockerImageResourceGetParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-docker-image\n" +
				"  type: docker-image\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - get: my-docker-image\n" +
				"    params:\n" +
				"      save: save-it\n" +
				"      rootfs: tar-it\n" +
				"      skip_download: skip-it\n"
		);

		editor.assertProblems(
				"save-it|'boolean'",
				"tar-it|'boolean'",
				"skip-it|'boolean'"
		);

		editor.assertHoverContains("save", "docker save");
		editor.assertHoverContains("rootfs", "a `.tar` file of the image");
		editor.assertHoverContains("skip_download", "Skip `docker pull`");
	}

	@Test public void dockerImageResourcePutParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-docker-image\n" +
				"  type: docker-image\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: my-docker-image\n" +
				"    params:\n" +
				"      build: path/to/docker/dir\n" +
				"      load: path/to/image\n" +
				"      dockerfile: path/to/Dockerfile\n"+
				"      cache: cache-it\n" +
				"      cache_tag: the-cache-tag\n" +
				"      load_base: path/to/base-image\n" +
				"      load_file: path/to/file-to-load\n" +
				"      load_repository: some-repo\n" +
				"      load_tag: some-tag\n" +
				"      import_file: path/to/file-to-import\n" +
				"      pull_repository: path/to/repository-to-pull\n" +
				"      pull_tag: tag-to-pull\n" +
				"      tag: path/to/file-containing-tag\n" +
				"      tag_prefix: v\n" +
				"      tag_as_latest: tag-latest\n" +
				"      build_args: the-build-args\n" +
				"      build_args_file: path/to/file-with-build-args.json\n" +
				"    get_params:\n" +
				"      save: save-it\n" +
				"      rootfs: tar-it\n" +
				"      skip_download: skip-it\n"
		);

		editor.assertProblems(
				"cache-it|'boolean'",
				"pull_repository|Deprecated",
				"pull_tag|Deprecated",
				"tag-latest|'boolean'",
				"the-build-args|Expecting a 'Map'",

				"save-it|'boolean'",
				"tar-it|'boolean'",
				"skip-it|'boolean'"
		);
		assertEquals(DiagnosticSeverity.Warning, editor.assertProblem("pull_repository").getSeverity());
		assertEquals(DiagnosticSeverity.Warning, editor.assertProblem("pull_tag").getSeverity());

		editor.assertHoverContains("build", "directory containing a `Dockerfile`");
		editor.assertHoverContains("load", "directory containing an image");
		editor.assertHoverContains("dockerfile", "path of the `Dockerfile` in the directory");
		editor.assertHoverContains("cache", "first pull `image:tag` from the Docker registry");
		editor.assertHoverContains("cache_tag", "specific tag to pull");
		editor.assertHoverContains("load_base", "path to a directory containing an image to `docker load`");
		editor.assertHoverContains("load_file", "path to a file to `docker load`");
		editor.assertHoverContains("load_repository", "repository of the image loaded from `load_file`");
		editor.assertHoverContains("load_tag", "tag of image loaded from `load_file`");
		editor.assertHoverContains("import_file", "file to `docker import`");
		editor.assertHoverContains("pull_repository", "repository to pull down");
		editor.assertHoverContains("pull_tag", "tag of the repository to pull down");
		editor.assertHoverContains(" tag:", "a path to a file containing the name"); // The word 'tag' occurs many times in editor so add use " tag: " to be precise
		editor.assertHoverContains("tag_prefix", "prepended with this string");
		editor.assertHoverContains("tag_as_latest", "tagged as `latest`");
		editor.assertHoverContains("build_args", "map of Docker build arguments");
		editor.assertHoverContains("build_args_file", "JSON file containing");

		editor.assertHoverContains("save", "docker save");
		editor.assertHoverContains("rootfs", "a `.tar` file of the image");
		editor.assertHoverContains("skip_download", "Skip `docker pull`");
	}

	@Test
	public void gotoResourceDefinition() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"- name: build-env\n" +
				"  type: docker-image\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"    params:\n" +
				"      rootfs: true\n" +
				"      save: true\n" +
				"  - put: build-env\n" +
				"    build: my-git/docker\n"
		);

		editor.assertGotoDefinition(editor.positionOf("get: my-git", "my-git"),
				editor.rangeOf("- name: my-git", "my-git")
		);
	}

	@Test
	public void gotoJobDefinition() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: prepare-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"  - task: preparations\n" +
				"    file: my-git/ci/tasks/preparations.yml\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: my-git\n" +
				"    passed:\n" +
				"    - prepare-stuff\n"
		);
		editor.assertGotoDefinition(editor.positionOf("- prepare-stuff", "prepare-stuff"),
				editor.rangeOf("- name: prepare-stuff", "prepare-stuff")
		);
	}

	@Test
	public void gotoResourceTypeDefinition() throws Exception {
		Editor editor = harness.newEditor(
				"resource_types:\n" +
				"- name: slack-notification\n" +
				"  type: docker_image\n" +
				"resources:\n" +
				"- name: zazazee\n" +
				"  type: slack-notification\n"
		);
		editor.assertGotoDefinition(editor.positionOf("type: slack-notification", "slack-notification"),
				editor.rangeOf("- name: slack-notification", "slack-notification")
		);
	}

	@Test public void reconcileResourceTypeNames() throws Exception {
		String userDefinedResourceTypesSnippet =
				"resource_types:\n" +
				"- name: s3-multi\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/s3-resource-simple\n" +
				"- name: slack-notification\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: cfcommunity/slack-notification-resource\n" +
				"    tag: latest\n";
		String[] goodNames = {
				//user-defined:
				"s3-multi", "slack-notification",
				//built-in:
				"git", "hg", "time", "s3",
				"archive", "semver", "github-release",
				"docker-image", "tracker", "pool", "cf", "bosh-io-release",
				"bosh-io-stemcell", "bosh-deployment", "vagrant-cloud"
		};
		String[] badNames = {
				"bogus", "wrong", "not-defined-resource-type"
		};

		//All the bad names are detected and flagged:
		for (String badName : badNames) {
			Editor editor = harness.newEditor(
					userDefinedResourceTypesSnippet +
					"resources:\n" +
					"- name: the-resource\n" +
					"  type: "+badName
			);
			editor.assertProblems(badName+"|Resource Type does not exist");
		}

		//All the good names are accepted:
		for (String goodName : goodNames) {
			Editor editor = harness.newEditor(
					userDefinedResourceTypesSnippet +
					"resources:\n" +
					"- name: the-resource\n" +
					"  type: "+goodName
			);
			editor.assertProblems(/*None*/);
		}
	}

	@Test public void contentAssistResourceTypeNames() throws Exception {
		String[] goodNames = {
				//user-defined:
				"s3-multi", "slack-notification",
				//built-in:
				"git", "hg", "time", "s3",
				"archive", "semver", "github-release",
				"docker-image", "tracker", "pool", "cf", "bosh-io-release",
				"bosh-io-stemcell", "bosh-deployment", "vagrant-cloud"
		};
		Arrays.sort(goodNames);

		String context =
				"resource_types:\n" +
				"- name: s3-multi\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/s3-resource-simple\n" +
				"- name: slack-notification\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: cfcommunity/slack-notification-resource\n" +
				"    tag: latest\n" +
				"resources:\n" +
				"- type: <*>";

		//All the good names are accepted:
		String[] expectedCompletions = new String[goodNames.length];
		for (int i = 0; i < expectedCompletions.length; i++) {
			expectedCompletions[i] = goodNames[i] +"<*>";
		}

		assertContextualCompletions(context,
				"<*>"
				, // ===>
				expectedCompletions
		);

	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertContextualCompletions(String conText, String textBefore, String... textAfter) throws Exception {
		assertContains(CURSOR, conText);
		textBefore = conText.replace(CURSOR, textBefore);
		textAfter = Arrays.stream(textAfter)
				.map((String t) -> conText.replace(CURSOR, t))
				.collect(Collectors.toList()).toArray(new String[0]);
		assertCompletions(textBefore, textAfter);
	}

	private void assertCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		editor.assertCompletions(textAfter);
	}
}
