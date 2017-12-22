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
import static org.junit.Assert.assertTrue;
import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.assertContains;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.commons.util.Unicodes;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.languageserver.testharness.CodeAction;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.languageserver.testharness.SynchronizationPoint;

import static org.springframework.ide.vscode.languageserver.testharness.Editor.*;

public class ConcourseEditorTest {

	private static final YamlCompletionEngineOptions OPTIONS = YamlCompletionEngineOptions.TEST_DEFAULT;

	private static final String CURSOR = "<*>";
	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(() -> {
				return new ConcourseLanguageServer(OPTIONS)
						.setMaxCompletions(100);
			},
			LanguageId.CONCOURSE_PIPELINE
		);
		harness.intialize(null);
	}

	@Test public void addSingleRequiredPropertiesQuickfix() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  source:\n" +
				"    username: someone\n" +
				"# Confuse"
		);
		Diagnostic problem = editor.assertProblems(
				"-|'type' is required",
				"foo|Unused"
		).get(0);
		CodeAction quickfix = editor.assertCodeAction(problem);
		assertEquals("Add property 'type'", quickfix.getLabel());
		quickfix.perform();

		editor.assertText(
				"resources:\n" +
				"- name: foo\n" +
				"  source:\n" +
				"    username: someone\n" +
				"  type: <*>\n" +
				"# Confuse"
		);
	}

	@Test public void addMultipleRequiredPropertiesQuickfix() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    username: someone\n"
		);
		Diagnostic problem = editor.assertProblems(
				"foo|Unused",
				"source|[branch, pool, uri] are required").get(1);
		CodeAction quickfix = editor.assertCodeAction(problem);
		assertEquals("Add properties: [branch, pool, uri]", quickfix.getLabel());
		quickfix.perform();

		editor.assertText(
				"resources:\n" +
				"- name: foo\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    username: someone\n" +
				"    uri: <*>\n" +
				"    branch: \n" +
				"    pool: \n"
		);
	}

	@Test public void quickfixForOneOfMultipleMarkersOnSameRange() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: myjob\n" +
				"  plan:\n" +
				"  - task: foo\n" +
				"    config:\n" +
				"      inputs:\n" +
				"      - name: foo"
		);
		List<Diagnostic> problems = editor.assertProblems(
			"config|[image_resource, rootfs_uri, image] is required",
			"config|[platform, run] are required"
		);

		assertTrue(editor.getCodeActions(problems.get(0)).isEmpty());

		CodeAction quickfix = editor.assertCodeAction(problems.get(1));
		assertEquals("Add properties: [platform, run]", quickfix.getLabel());
		quickfix.perform();

		editor.assertText(
				"jobs:\n" +
				"- name: myjob\n" +
				"  plan:\n" +
				"  - task: foo\n" +
				"    config:\n" +
				"      inputs:\n" +
				"      - name: foo\n" +
				"      platform: <*>\n" +
				"      run:\n" +
				"        path: "
		);

	}

	@Test public void reconcileResourceTypeType() throws Exception {
		Editor editor;
		editor = harness.newEditor(
				"resource_types:\n" +
				"- name: s3-multi\n" +
				"  type: # <- bad\n"
		);
		editor.assertProblems(
				"^ # <- bad|cannot be blank"
		);

		editor = harness.newEditor(
				"resource_types:\n" +
				"- name: s3-multi\n" +
				"  type: garbage\n"
		);
		editor.assertProblems(
				"garbage|Resource Type does not exist"
		);
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
		InputStream stream = ConcourseEditorTest.class.getClassLoader().getResourceAsStream(resourceName);
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
				"-^ task|One of [config, file] is required",
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
	public void concourse_3_0_rootfs_uri_prop() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"image: blah\n" +
				"run:\n" +
				"  path: demo-repo/ci/tasks/run-tests.sh"
		);
		Diagnostic p = editor.assertProblems("image|renamed to 'rootfs_uri'").get(0);
		assertEquals(DiagnosticSeverity.Warning, p.getSeverity());

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"image: blah\n" +
				"run:\n" +
				"  path: demo-repo/ci/tasks/run-tests.sh"
		);
		editor.assertHoverContains("image", "renamed to `rootfs_uri`");
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
				"jobs:\n" +
				"- name: $1\n" +
				"  plan:\n" +
				"  - $2<*>"
		);
	}

	@Test
	public void toplevelCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor(CURSOR);
		editor.assertCompletions(
				"groups:\n" +
				"- name: <*>"
				, // --------------
				"jobs:\n" +
				"- name: $1\n" +
				"  plan:\n" +
				"  - $2<*>"
				, // ---------------
				"resource_types:\n" +
				"- name: $1\n" +
				"  type: $2<*>"
				, // ---------------
				"resources:\n"+
				"- name: $1\n" +
				"  type: $2<*>"
		);

		editor = harness.newEditor("rety<*>");
		editor.assertCompletions(
				"resource_types:\n" +
				"- name: $1\n" +
				"  type: $2<*>"
		);
	}

	@Test
	public void valueCompletions() throws Exception {
		String [] builtInResourceTypes = {
				"git", "hg", "time", "s3", "archive",
				"semver", "github-release", "docker-image", "tracker",
				"pool", "cf",
				"bosh-io-release", "bosh-io-stemcell", "bosh-deployment",
				"vagrant-cloud"
		};
		Arrays.sort(builtInResourceTypes);

		String[] expectedCompletions = new String[builtInResourceTypes.length];
		for (int i = 0; i < expectedCompletions.length; i++) {
			expectedCompletions[i] =
					"resources:\n" +
					"- type: "+builtInResourceTypes[i]+"<*>\n" +
					"  source:";
		}

		assertCompletions(
				"resources:\n" +
				"- type: <*>\n" +
				"  source:"
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
				"    image: bogus-image\n" +
				"    file: some-file.yml\n" +
				"    input_mapping:\n" +
				"      task-input: bogus-input\n" +
				"      repo: sts4\n" +
				"  - put: bogus-put\n"
		);
		editor.assertProblems(
				"bogus-get|resource does not exist",
				"bogus-image|resource does not exist",
//				"bogus-input|resource does not exist", //Not checked anymore. See: https://www.pivotaltracker.com/story/show/145024233
				"bogus-put|resource does not exist"
		);

		editor.assertProblems(
				"bogus-get|[sts4]",
				"bogus-image|[sts4]",
//				"bogus-input|[sts4]",  //Not checked anymore. See: https://www.pivotaltracker.com/story/show/145024233
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
				"sts4|Unused",
				"utils|Unused",
				"sts4|Duplicate resource name",
				"sts4|Unused"
		);
	}

	@Test
	public void reconcileDuplicateResourceTypeNames() throws Exception {
		Editor editor = harness.newEditor(
				"resource_types:\n" +
				"- name: slack-notification\n" +
				"  type: docker-image\n" +
				"- name: slack-notification\n" +
				"  type: docker-image"
		);
		editor.assertProblems(
				"slack-notification|Duplicate resource-type name",
				"slack-notification|Duplicate resource-type name"
		);
	}

	@Test
	public void violatedPropertyConstraintsAreWarnings() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: blah"
		);
		Diagnostic problem = editor.assertProblems("-^ name: blah|'plan' is required").get(0);
		assertEquals(DiagnosticSeverity.Warning,  problem.getSeverity());

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - task: foo"
		);
		problem = editor.assertProblems("-^ task: foo|One of [config, file] is required").get(0);
		assertEquals(DiagnosticSeverity.Warning,  problem.getSeverity());

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - task: foo\n" +
				"    config: {}\n" +
				"    file: path/to/file"
		);
		{
			List<Diagnostic> problems = editor.assertProblems(
				"config|One of [image_resource, rootfs_uri, image]",
				"config|Only one of [config, file]",
				"config|[platform, run] are required",
				"file|Only one of [config, file]"
			);
			//All of the problems in this example are property contraint violations! So all should be warnings.
			for (Diagnostic diagnostic : problems) {
				assertEquals(DiagnosticSeverity.Warning, diagnostic.getSeverity());
			}
		}
	}

	@Test
	public void underlineParentPropertyForMissingNode() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/140709005

		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: hello-world\n" +
				"  plan:\n" +
				"  - task: say-hello\n" +
				"    config:\n" +
				"      image_resource:\n" +
				"        type: docker-image\n" +
				"        source: {repository: ubuntu}\n" +
				"      run:\n" +
				"        path: echo\n" +
				"        args: [\"Hello, world!\"]"
		);
		editor.assertProblems(
				"config|'platform' is required"
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
				"-^ name: job-1|'plan' is required",
				"job-1|Duplicate job name",
				"-^ name: utils|'plan' is required",
				"-^ name: job-1|'plan' is required",
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

		assertContextualCompletions(
				"resources:\n" +
				"- name: sts4\n" +
				"- name: repo-a\n" +
				"- name: repo-b\n" +
				"jobs:\n" +
				"- name: job1\n" +
				"  plan:\n" +
				"  - task: do-it\n" +
				"    input_mapping:\n" +
				"      remapped: <*>\n"
				, ////////////////////
				"<*>"
				, // =>
				"repo-a<*>", "repo-b<*>", "sts4<*>"
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
				"my-repo|Unused 'Resource'",
				"resources|Duplicate key",
				"your-repo|Unused 'Resource'",
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
				"    file: some-task.yml\n" +
				"  - put: build-artefact\n" +
				"- name: test\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"    passed:\n" +
				"    - not-a-job\n" +
				"    - build\n"
		);

		editor.assertProblems(
				"build-artefact|should define 'branch'",
				"not-a-job|does not exist"
		);
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
				"    file: tasks/some-task.yml\n" +
				"  - put: build-artefact # <- bad\n" +
				"- name: test\n" +
				"  plan:\n" +
				"  - get: git-repo\n" +
				"groups:\n" +
				"- name: some-group\n" +
				"  jobs: [build, test, bogus-job]\n" +
				"  resources: [git-repo, build-artefact, not-a-resource]"
		);

		editor.assertProblems(
				"build-artefact^ # <- bad|should define 'branch'",
				"bogus-job|does not exist",
				"not-a-resource|does not exist"
		);
	}

	@Test public void timeResourceCompletions() throws Exception {
		assertContextualCompletions(
				"resources:\n" +
				"- name: every5minutes\n" +
				"  type: time\n" +
				"  source:\n" +
				"    location: <*>"
				, // ======================
				"Van<*>"
				, // =>
				"America/Vancouver<*>",
				"Asia/Vientiane<*>",
				"Europe/Vatican<*>"
		);

		assertContextualCompletions(
			"resources:\n" +
			"- name: every5minutes\n" +
			"  type: time\n" +
			"  source:\n" +
			"    <*>\n" +
			"    blah: blah"
			, // ======================
			"<*>"
			, // =>
			"days:\n"+
			"    - <*>",
			"interval: <*>",
			"location: <*>",
			"start: <*>",
			"stop: <*>"
		);

		assertContextualCompletions(
			"resources:\n" +
			"- name: every5minutes\n" +
			"  type: time\n" +
			"  source:\n" +
			"    days:\n" +
			"    - <*>"
			, // ======================
			"<*>"
			, // =>
			"Friday<*>",
			"Monday<*>",
			"Saturday<*>",
			"Sunday<*>",
			"Thursday<*>",
			"Tuesday<*>",
			"Wednesday<*>"
		);

	}

	@Test public void timeResourceSourceReconcile() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: every5minutes\n" +
				"  type: time\n" +
				"  source:\n" +
				"    location: PST8PDT\n" +
				"    start: 7AM\n" +
				"    stop: 8AM\n" +
				"    interval: 5m\n" +
				"    days:\n" +
				"    - Thursday\n"
		);
		editor.assertProblems(
				"every5minutes|Unused"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: every5minutes\n" +
				"  type: time\n" +
				"  source:\n" +
				"    location: some-location\n" +
				"    start: the-start-time\n" +
				"    stop: the-stop-time\n" +
				"    interval: the-interval\n" +
				"    days:\n" +
				"    - Monday\n" +
				"    - Someday\n"
		);
		editor.assertProblems(
				"every5minutes|Unused",
				"some-location|Unknown 'Location'",
				"the-start-time|not a valid 'Time'",
				"the-stop-time|not a valid 'Time'",
				"the-interval|not a valid 'Duration'",
				"Someday|unknown 'Day'"
		);
	}

	@Test public void timeResourceSourceHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: timed-trigger\n" +
				"  type: time\n" +
				"  source:\n" +
				"    interval: 5m\n" +
				"    location: UTC\n" +
				"    start: 8:00PM\n" +
				"    stop: 9:00PM\n" +
				"    days: [Monday, Wednesday, Friday]"
		);
		editor.assertHoverContains("interval", "interval on which to report new versions");
		editor.assertHoverContains("location", "*Optional. Default `UTC`");
		editor.assertHoverContains("start", "The supported time formats are");
		editor.assertHoverContains("stop", "The supported time formats are");
		editor.assertHoverContains("days", "Run only on these day(s)");
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
				"sts4-out|Unused",
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
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: the-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    <*>"
				, //================
				"<*>"
				, // ==>
				"uri: <*>"
		);

		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: the-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri:\n" +
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
				"      <*>\n" +
				"      blah: blah";

		assertContextualCompletions(context,
				"<*>"
				, // ===>
				"depth: <*>"
				,
				"disable_git_lfs: <*>"
				,
				"submodules:\n"+
				"        <*>"
				,
				"fetch:\n" + // Deprecated, so suggested last
				"      - <*>"
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
		assertContextualCompletions(PLAIN_COMPLETION,
			"resources:\n" +
			"- name: my-git\n" +
			"  type: git\n" +
			"jobs:\n" +
			"- name: do-stuff\n" +
			"  plan:\n" +
			"  - put: my-git\n" +
			"    params:\n" +
			"      <*>"
			,
			"<*>"
			, // =>
			"repository: <*>"
		);

		String context =
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params:\n" +
				"      repository: blah\n" +
				"      <*>";

		assertContextualCompletions(PLAIN_COMPLETION, context,
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
				"  source:\n" +
				"    uri: some-uri\n" +
				"    branch: master\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - put: my-git\n" +
				"    params: {}\n"
		);
		editor.assertProblems("params|'repository' is required");

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-git\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: some-uri\n" +
				"    branch: master\n" +
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
				"  webhook_token: bladayadayaaa\n" +
				"  source:\n" +
				"    repository: https://github.com/spring-projects/sts4\n"
		);

		editor.assertHoverContains("name", "The name of the resource");
		editor.assertHoverContains("type", "The type of the resource. Each worker advertises");
		editor.assertHoverContains("source", 2, "The location of the resource");
		editor.assertHoverContains("webhook_token", "web hooks can be sent to trigger an immediate *check* of the resource");
		editor.assertHoverContains("check_every", "The interval on which to check for new versions");
	}

	@Test public void requiredPropertiesReconcile() throws Exception {
		Editor editor;

		//addProp(resource, "name", resourceNameDef).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- type: git"
		);
		editor.assertProblems("-^ type: git|'name' is required");

		//addProp(resource, "type", t_resource_type_name).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo"
		);
		editor.assertProblems(
				"-^ name: foo|'type' is required",
				"foo|Unused"
		);

		//Both name and type missing:
		editor = harness.newEditor(
				"resources:\n" +
				"- source: {}"
		);
		editor.assertProblems("-^ source:|[name, type] are required");

		//addProp(job, "name", jobNameDef).isRequired(true);
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo"
		);
		editor.assertProblems("-^ name: foo|'plan' is required");

		//addProp(job, "plan", f.yseq(step)).isRequired(true);
		editor = harness.newEditor(
				"jobs:\n" +
				"- plan: []"
		);
		editor.assertProblems("-^ plan: []|'name' is required");

		//addProp(resourceType, "name", t_ne_string).isRequired(true);
		editor = harness.newEditor(
				"resource_types:\n" +
				"- type: docker-image"
		);
		editor.assertProblems("-^ type: docker-image|'name' is required");

		//addProp(resourceType, "type", t_image_type).isRequired(true);
		editor = harness.newEditor(
				"resource_types:\n" +
				"- name: foo"
		);
		editor.assertProblems("-^ name: foo|'type' is required");

		//addProp(gitSource, "uri", t_string).isRequired(true);
		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    branch: master"
		);
		editor.assertProblems(
				"foo|Unused",
				"source|'uri' is required"
		);

		//addProp(group, "name", t_ne_string).isRequired(true);
		editor = harness.newEditor(
				"groups:\n" +
				"- jobs: []"
		);
		editor.assertProblems("-^ jobs: []|'name' is required");
	}

	@Test public void gitBranchRequiredInPutStep() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: git@github.com/johny-coder/test-repo\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: repo\n" +
				"  - put: repo # <- bad\n"
		);
		editor.assertProblems(
				"repo^ # <- bad|should define 'branch'"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: git@github.com/johny-coder/test-repo\n" +
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: repo\n" +
				"  - put: blah\n" +
				"    resource: repo # <- bad\n"
		);
		editor.assertProblems(
				"repo^ # <- bad|should define 'branch'"
		);
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
		editor.assertProblems(
			"my-docker-image|Unused 'Resource'",
			"source|'repository' is required"
		);

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
				"my-docker-image|Unused 'Resource'",
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

	@Test public void s3ResourceSourceReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: s3-snapshots\n" +
				"  type: s3\n" +
				"  source:\n" +
				"    access_key_id: the-key"
		);
		editor.assertProblems(
				"s3-snapshots|Unused 'Resource'",
				"source|One of [regexp, versioned_file] is required",
				"source|'bucket' is required"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: s3-snapshots\n" +
				"  type: s3\n" +
				"  source:\n" +
				"    bucket: the-bucket\n" +
				"    access_key_id: the-access-key\n" +
				"    secret_access_key: the-secret-key\n" +
				"    region_name: bogus-region\n" +
				"    private: is-private\n" +
				"    cloudfront_url: https://d5yxxxxx.cloudfront.net\n" +
				"    endpoint: https://blah.custom.com/blah/blah\n" +
				"    disable_ssl: no_ssl_checking\n" +
				"    server_side_encryption: some-encryption-algo\n" + //TODO: validation and CA? What values are acceptable?
				"    sse_kms_key_id: the-master-key-id\n" +
				"    use_v2_signing: should-use-v2\n" +
				"    regexp: path-to-file-(.*).tar.gz\n" +
				"    versioned_file: path/to/file.tar.gz\n"
		);
		editor.assertProblems(
				"s3-snapshots|Unused 'Resource'",
				"bogus-region|unknown 'S3Region'",
				"is-private|'boolean'",
				"no_ssl_checking|'boolean'",
				"should-use-v2|'boolean'",
				"regexp|Only one of [regexp, versioned_file] should be defined",
				"versioned_file|Only one of [regexp, versioned_file] should be defined"
		);

		editor.assertHoverContains("bucket", "The name of the bucket");
		editor.assertHoverContains("access_key_id", "The AWS access key");
		editor.assertHoverContains("secret_access_key", "The AWS secret key");
		editor.assertHoverContains("region_name", "The region the bucket is in");
		editor.assertHoverContains("private", "Indicates that the bucket is private");
		editor.assertHoverContains("cloudfront_url", "The URL (scheme and domain) of your CloudFront distribution");
		editor.assertHoverContains("endpoint", "Custom endpoint for using S3");
		editor.assertHoverContains("disable_ssl", "Disable SSL for the endpoint");
		editor.assertHoverContains("server_side_encryption", "An encryption algorithm to use");
		editor.assertHoverContains("sse_kms_key_id", "The ID of the AWS KMS master encryption key");
		editor.assertHoverContains("use_v2_signing", "Use signature v2 signing");
		editor.assertHoverContains("regexp", "The pattern to match filenames against within S3");
		editor.assertHoverContains("versioned_file", "If you enable versioning for your S3 bucket");
	}

	@Test public void s3ResourceRegionCompletions() throws Exception {
		String[] validRegions = {
				//See: http://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUT.html
				"us-west-1", "us-west-2", "ca-central-1",
				"EU", "eu-west-1", "eu-west-2", "eu-central-1",
				"ap-south-1", "ap-southeast-1", "ap-southeast-2",
				"ap-northeast-1", "ap-northeast-2",
				"sa-east-1", "us-east-2"
		};
		Arrays.sort(validRegions);

		String[] expectedCompletions = new String[validRegions.length];
		for (int i = 0; i < expectedCompletions.length; i++) {
			expectedCompletions[i] = validRegions[i]+"<*>";
		}

		assertContextualCompletions(
				"resources:\n" +
				"- name: s3-snapshots\n" +
				"  type: s3\n" +
				"  source:\n" +
				"    region_name: <*>"
				, //===================
				"<*>"
				, // ===>
				expectedCompletions
		);
	}

	@Test public void s3ResourceGetParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-s3-bucket\n" +
				"  type: s3\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - get: my-s3-bucket\n" +
				"    params:\n" +
				"      no-params-expected: bad"
		);

		editor.assertProblems(
				"no-params-expected|Unknown property"
		);
	}

	@Test public void s3ResourcePutParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-s3-bucket\n" +
				"  type: s3\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: my-s3-bucket\n" +
				"    params:\n" +
				"      acl: public-read\n" +
				"    get_params:\n" +
				"      no-params-expected: bad"
		);
		editor.assertProblems(
				"params|'file' is required",
				"no-params-expected|Unknown property"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-s3-bucket\n" +
				"  type: s3\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: my-s3-bucket\n" +
				"    params:\n" +
				"      file: path/to/file\n" +
				"      acl: bad-acl\n" +
				"      content_type: anything/goes\n"
		);
		editor.assertProblems(
				"bad-acl|unknown 'S3CannedAcl'"
		);

		editor.assertHoverContains("file", "Path to the file to upload");
		editor.assertHoverContains("acl", "Canned Acl");
		editor.assertHoverContains("content_type", "MIME");
	}

	@Test public void s3ResourcePutParamsContentAssist() throws Exception {
		String[] cannedAcls = { //See http://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#canned-acl
				"private",
				"public-read",
				"public-read-write",
				"aws-exec-read",
				"authenticated-read",
				"bucket-owner-read",
				"bucket-owner-full-control",
				"log-delivery-write"
		};
		Arrays.sort(cannedAcls);

		String conText =
				"resources:\n" +
				"- name: my-s3-bucket\n" +
				"  type: s3\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: my-s3-bucket\n" +
				"    params:\n" +
				"      <*>";

		assertContextualCompletions(conText,
				"content_type: json<*>"
				, //=>
				"content_type: application/json; charset=utf-8<*>",
				"content_type: application/manifest+json; charset=utf-8<*>"
		);

		String[] expectedAclCompletions = new String[cannedAcls.length];
		for (int i = 0; i < expectedAclCompletions.length; i++) {
			expectedAclCompletions[i] = "acl: "+cannedAcls[i]+"<*>";
		}
		assertContextualCompletions(conText,
				"acl: <*>"
				, // ===>
				expectedAclCompletions
		);
	}

	@Test public void poolResourceSourceReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: swimming-pool\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    private_key: stuff"
		);
		editor.assertProblems(
				"swimming-pool|Unused",
				"source|[branch, pool, uri] are required"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: the--locks\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    uri: git@github.com:concourse/locks.git\n" +
				"    branch: master\n" +
				"    pool: aws\n" +
				"    private_key: |\n" +
				"      -----BEGIN RSA PRIVATE KEY-----\n" +
				"      MIIEowIBAAKCAQEAtCS10/f7W7lkQaSgD/mVeaSOvSF9ql4hf/zfMwfVGgHWjj+W\n" +
				"      ...\n" +
				"      DWiJL+OFeg9kawcUL6hQ8JeXPhlImG6RTUffma9+iGQyyBMCGd1l\n" +
				"      -----END RSA PRIVATE KEY-----\n" +
				"    username: jonhsmith\n" +
				"    password: his-password\n" +
				"    retry_delay: retry-after\n"
		);
		editor.assertProblems(
				"the--locks|Unused",
				"retry-after|'Duration'"
		);

		editor.assertHoverContains("uri", "The location of the repository.");
		editor.assertHoverContains("branch", "The branch to track");
		editor.assertHoverContains("pool", 2, "The logical name of your pool of things to lock");
		editor.assertHoverContains("private_key", "Private key to use when pulling/pushing");
		editor.assertHoverContains("username", "Username for HTTP(S) auth");
		editor.assertHoverContains("password", "Password for HTTP(S) auth ");
		editor.assertHoverContains("retry_delay", "how long to wait until retrying");
	}

	@Test public void poolResourceGetParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-locks\n" +
				"  type: pool\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - get: my-locks\n" +
				"    params:\n" +
				"      no-params-expected: bad"
		);

		editor.assertProblems(
				"no-params-expected|Unknown property"
		);
	}

	@Test public void poolResourcePutParamsReconcileAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: my-locks\n" +
				"  type: pool\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: my-locks\n" +
				"    params:\n" +
				"      acquire: should-acquire\n" +
				"      claim: a-specific-lock\n" +
				"      release: path/to/lock\n" +
				"      add: path/to/lock\n" +
				"      add_claimed: path/to/lock\n" +
				"      remove: path/to/lock"
		);

		editor.assertProblems(
				"acquire|Only one of",
				"should-acquire|'boolean'",
				"claim|Only one of",
				"release|Only one of",
				"add|Only one of",
				"add_claimed|Only one of",
				"remove|Only one of"
		);

		editor.assertHoverContains("acquire", "attempt to move a randomly chosen lock");
		editor.assertHoverContains("claim", "the specified lock from the pool will be acquired");
		editor.assertHoverContains("release", "release the lock");
		editor.assertHoverContains("add", "add a new lock to the pool in the unclaimed state");
		editor.assertHoverContains("add_claimed", "in the *claimed* state");
		editor.assertHoverContains("remove", "remove the given lock from the pool");
	}

	@Test public void semverResourceSourceReconcileAtomNotAllowed() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source: an-atom"
		);
		editor.assertProblems(
				"version|Unused 'Resource'",
				"an-atom|Expecting a 'Map'"
		);
	}

	@Test public void semverResourceSourceReconcileRequiredProps() throws Exception {
		Editor editor;

		//required props for s3 driver
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: s3"
		);
		editor.assertProblems(
				"version|Unused",
				"source|[access_key_id, bucket, key, secret_access_key] are required"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source: {}"
		);
		editor.assertProblems(
				"version|Unused",
				"source|[access_key_id, bucket, key, secret_access_key] are required"
		);

		// required props for git driver
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git"
		);
		editor.assertProblems(
				"version|Unused",
				"source|[branch, file, uri] are required"
		);

		//required props for swift driver
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: swift"
		);
		editor.assertProblems(
				"version|Unused",
				"source|'openstack' is required"
		);
	}

	@Test public void semverResourceSourceBadDriver() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: bad-driver"
		);
		editor.assertProblems(
				"version|Unused",
				"bad-driver|'SemverDriver'"
		);
	}

	@Test public void semverGitResourceSourceContentAssist() throws Exception {
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    <*>"
				, // ===========
				"<*>"
				, // ==>
				    "uri: $1\n" +
				"    branch: $2\n" +
				"    file: $3<*>"
				, //---
				"uri: <*>"
		);
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    uri: something\n" +
				"<*>"
				, // =============
				"    <*>"
				, // ==>
				"    branch: <*>"
				,
				"    file: <*>"
		);
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    uri: something\n" +
				"    branch: master\n" +
				"    file: somefile\n" +
				"<*>"
				, // =============
				"    <*>"
				, // ==>
				"    git_user: <*>"
				,
				"    initial_version: <*>"
				,
				"    password: <*>"
				,
				"    private_key: <*>"
				,
				"    username: <*>"
		);

	}

	@Test public void semverGitResourceSourceReconcileAndHovers() throws Exception {
		Editor editor;

		// required props for git driver
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: not-a-version\n" + //TODO: should be marked as a error but isn't yet.
				"    driver: git\n" +
				"    uri: git@github.com:concourse/concourse.git\n" +
				"    branch: version\n" +
				"    file: version\n" +
				"    private_key: {{concourse-repo-private-key}}\n" +
				"    username: jsmith\n" +
				"    password: s3cre$t\n" +
				"    git_user: jsmith@mailhost.com\n" +
				"    bogus: bad"
		);
		editor.assertProblems(
				"version|Unused",
				"bogus|Unknown property"
		);

		editor.assertHoverContains("initial_version", "version number to use when bootstrapping");
		editor.assertHoverContains("driver", "The driver to use");
		editor.assertHoverContains("uri", "The repository URL");
		editor.assertHoverContains("branch", "The branch the file lives on");
		editor.assertHoverContains("file", "The name of the file");
		editor.assertHoverContains("private_key", "The SSH private key");
		editor.assertHoverContains("username", "Username for HTTP(S) auth");
		editor.assertHoverContains("password", "Password for HTTP(S) auth");
		editor.assertHoverContains("git_user", "The git identity to use");
	}

	@Test public void semverResourceSourcePrimaryContentAssist() throws Exception {

		//S3 completions
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    <*>"
				, /////////////
				"<*>"
				, // ==>
				//snippet:
				    "bucket: $1\n" +
				"    key: $2\n" +
				"    access_key_id: $3\n" +
				"    secret_access_key: $4<*>",
				//non-snippet:
				"bucket: <*>",
				"driver: <*>"
		);

		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: s3\n" +
				"    <*>"
				, /////////////
				"<*>"
				, // ==>
				//snippet:
				    "bucket: $1\n" +
				"    key: $2\n" +
				"    access_key_id: $3\n" +
				"    secret_access_key: $4<*>",
				//non-snippet:
				"bucket: <*>"
		);

		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: s3\n" +
				"    bucket: some-bucket\n" +
				"    <*>"
				, /////////////
				"<*>"
				,  // ==>
				"access_key_id: <*>",
				"key: <*>",
				"secret_access_key: <*>"
		);

		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    bucket: some-bucket\n" +
				"    <*>"
				, /////////////
				"<*>"
				,  // ==>
				"access_key_id: <*>",
				"driver: <*>",
				"key: <*>",
				"secret_access_key: <*>"
		);

		//git completions
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    <*>"
				, /////////////
				"<*>"
				, // ==>
				    "uri: $1\n" +
				"    branch: $2\n" +
				"    file: $3<*>"
				, //===
				"uri: <*>"
		);
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    uri: blah\n" +
				"    <*>"
				, /////////////
				"<*>"
				, // ==>
				"branch: <*>",
				"file: <*>"
		);
		assertContextualCompletions(PLAIN_COMPLETION,
				"resources:\n" +
				"- name: fff\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    driver: git\n" +
				"    uri: blah\n" +
				"    branch: master\n" +
				"    file: version-file\n"+
				"    <*>"
				, /////////////
				"<*>"
				, // ==>
				"git_user: <*>",
				"initial_version: <*>",
				"password: <*>",
				"private_key: <*>",
				"username: <*>"
		);
	}

	@Test public void semverS3ResourceSourceReconcileAndHovers() throws Exception {
		Editor editor;

		//without explicit 'driver'... should assume s3 by default
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: 1.2.3\n" +
				"    bucket: the-bucket\n" +
				"    key: object-key\n" +
				"    access_key_id: aws-access-key\n" +
				"    secret_access_key: aws-access-key\n" +
				"    region_name: bogus-region\n" +
				"    endpoint: https://blah.com/blah\n" +
				"    disable_ssl: no-use-ssl\n" +
				"    bogus-prop: bad"
		);
		editor.assertProblems(
				"version|Unused 'Resource'",
				"bogus-region|'S3Region'",
				"no-use-ssl|'boolean'",
				"bogus-prop|Unknown property"
		);


		//with explicit 'driver: s3'
		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: 1.2.3\n" +
				"    driver: s3\n" +
				"    bucket: the-bucket\n" +
				"    key: object-key\n" +
				"    access_key_id: aws-access-key\n" +
				"    secret_access_key: aws-access-key\n" +
				"    region_name: bogus-region\n" +
				"    endpoint: https://blah.com/blah\n" +
				"    disable_ssl: no-use-ssl\n" +
				"    bogus-prop: bad"
		);
		editor.assertProblems(
				"version|Unused 'Resource'",
				"bogus-region|'S3Region'",
				"no-use-ssl|'boolean'",
				"bogus-prop|Unknown property"
		);

		editor.assertHoverContains("initial_version", "version number to use when bootstrapping");
		editor.assertHoverContains("driver", "The driver to use");
		editor.assertHoverContains("bucket", "The name of the bucket");
		editor.assertHoverContains("key", "The key to use for the object");
		editor.assertHoverContains("access_key_id", "The AWS access key to");
		editor.assertHoverContains("secret_access_key", "The AWS secret key to");
		editor.assertHoverContains("region_name", "The region the bucket is in");
		editor.assertHoverContains("endpoint", "Custom endpoint for using S3");
		editor.assertHoverContains("disable_ssl", "Disable SSL for the endpoint");
	}

	@Test public void semverSwiftResourceSourceReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: 1.2.3\n" +
				"    driver: swift\n" +
				"    openstack:\n" +
				"       container: nice-container\n" +
				"       item_name: flubber-blub\n" +
				"       region_name: us-west-1\n"
		);
		editor.assertProblems("version|Unused 'Resource'");
		editor.assertHoverContains("openstack", "All openstack configuration");
	}

	@Test public void semverResourceGetParamsReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: 1.2.3\n" +
				"    driver: swift\n" +
				"    openstack: whatever\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - get: version\n" +
				"    params:\n" +
				"      bump: what-to-bump\n" +
				"      pre: beta\n" +
				"      bogus: bad\n"
		);
		editor.assertProblems(
				"what-to-bump|[final, major, minor, patch]",
				"bogus|Unknown property"
		);

		editor.assertHoverContains("bump", "Bump the version number");
		editor.assertHoverContains("pre", "bump to a prerelease");
	}

	@Test public void semverPutParamsReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"  source:\n" +
				"    initial_version: 1.2.3\n" +
				"    driver: swift\n" +
				"    openstack: whatever\n" +
				"jobs:\n" +
				"- name: a-job\n" +
				"  plan:\n" +
				"  - put: version\n" +
				"    params:\n" +
				"      file: version-file\n" +
				"      bump: what-to-bump\n" +
				"      pre: alpha\n" +
				"      bogus-one: bad\n" +
				"    get_params:\n" +
				"      file: not-expected-here\n" +
				"      bump: what-to-get-bump\n" +
				"      pre: beta\n" +
				"      bogus-two: bad\n"
		);
		editor.assertProblems(
				"what-to-bump|[final, major, minor, patch]",
				"bogus-one|Unknown property",
				"file|Unknown property",
				"what-to-get-bump|[final, major, minor, patch]",
				"bogus-two|Unknown property"
		);

		editor.assertHoverContains("file", 1, "Path to a file containing the version number");
		editor.assertHoverContains("bump", 1, "Bump the version number");
		editor.assertHoverContains("bump", 2, "Bump the version number");
		editor.assertHoverContains("pre", 1, "bump to a prerelease");
		editor.assertHoverContains("pre", 2, "bump to a prerelease");
	}

	@Test public void reconcileExplicitResourceAttributeInPutStep() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/138568839
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: aws-environments\n" +
				"  type: pool\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"    - put: environment-1\n" +
				"      resource: aws-environments\n" +
				"      params:\n" +
				"        acquire: true\n" +
				"        bogus_param: bad\n" +
				"      get_params:\n" +
				"        bogus_get_param: bad"
		);
		editor.assertProblems(
				"bogus_param|Unknown property",
				"bogus_get_param|Unknown property"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: aws-environments\n" +
				"  type: pool\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"    - get: environment-1\n" +
				"      resource: aws-environments\n" +
				"      params:\n" +
				"        bogus_param: bad\n"
		);
		editor.assertProblems(
				"bogus_param|Unknown property"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: aws-environments\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    uri: git@github.com:concourse/locks.git\n" +
				"    branch: master\n" +
				"    pool: aws\n" +
				"    private_key: |\n" +
				"      -----BEGIN RSA PRIVATE KEY-----\n" +
				"      MIIEowIBAAKCAQEAtCS10/f7W7lkQaSgD/mVeaSOvSF9ql4hf/zfMwfVGgHWjj+W\n" +
				"      ...\n" +
				"      DWiJL+OFeg9kawcUL6hQ8JeXPhlImG6RTUffma9+iGQyyBMCGd1l\n" +
				"      -----END RSA PRIVATE KEY-----\n" +
				"    username: jonhsmith\n" +
				"    password: his-password\n" +
				"    retry_delay: 10s\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"    - get: aws-environments\n" +
				"      params: {}     \n" +
				"    - put: environment-1\n" +
				"      resource: aws-environments\n" +
				"      params: {acquire: true}\n" +
				"    - put: environment-2\n" +
				"      resource: aws-environments\n" +
				"      params: \n" +
				"        acquire: true\n" +
				"        bogus_param: blah\n" +
				"    - task: test-multi-aws\n" +
				"      file: my-scripts/test-multi-aws.yml\n" +
				"    - put: aws-environments\n" +
				"      params: {release: environment-1}\n" +
				"    - put: aws-environments\n" +
				"      params: {release: environment-2}"
		);
		editor.assertProblems(
				"bogus_param|Unknown property"
		);
	}

	@Test
	public void resourceNameContentAssist() throws Exception {
		String conText;

		conText =
				"resources:\n" +
				"- name: foo-resource\n" +
				"- name: bar-resource\n" +
				"- name: other-resource\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"  - <*>";
		assertContextualCompletions(conText
				, // ==============
				"get: <*>"
				,
				"get: bar-resource<*>",
				"get: foo-resource<*>",
				"get: other-resource<*>"
		);
		assertContextualCompletions(conText
				, // ==============
				"put: <*>"
				, // ==>
				"put: bar-resource<*>",
				"put: foo-resource<*>",
				"put: other-resource<*>"
		);


		conText =
				"resources:\n" +
				"- name: foo-resource\n" +
				"- name: bar-resource\n" +
				"- name: other-resource\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"  - put: something\n" +
				"    <*>";
		assertContextualCompletions(conText
				, // ==============
				"resource: <*>"
				, // ==>
				"resource: bar-resource<*>",
				"resource: foo-resource<*>",
				"resource: other-resource<*>"
		);

		conText =
				"resources:\n" +
				"- name: foo-resource\n" +
				"- name: bar-resource\n" +
				"- name: other-resource\n" +
				"jobs:\n" +
				"- name: test-multi-aws\n" +
				"  plan:\n" +
				"  - <*>\n" +
				"    resource: foo-resource\n"; // presence of explicit 'resource' attribute should disable treating the name in put/get as a resource-name

		assertContextualCompletions(conText,
				"put: <*>"
				// ==> NONE
		);
		assertContextualCompletions(conText,
				"get: <*>"
				// ==> NONE
		);
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
				"  type: docker-image\n" +
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
			editor.assertProblems(
				"the-resource|Unused 'Resource'",
				badName+"|Resource Type does not exist"
			);
		}

		//All the good names are accepted:
		for (String goodName : goodNames) {
			Editor editor = harness.newEditor(
					userDefinedResourceTypesSnippet +
					"resources:\n" +
					"- name: the-resource\n" +
					"  type: "+goodName
			);
			editor.assertProblems(/*None*/
				"the-resource|Unused 'Resource'"
			);
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
				"- type: <*>\n" +
				"  source:\n";

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

	@Test public void reconcileTaskFileToplevelProperties() throws Exception {
		Editor editor;

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"rootfs_uri: some-image"
		);
		editor.assertProblems("rootfs_uri: some-imag^e^|[platform, run] are required");

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: a-platform\n" +
				"image_resource:\n" +
				"  name: should-not-be-here\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    bogus-source-prop: bad\n" +
				"    repository: ruby\n" +
				"    tag: '2.1'\n" +
				"rootfs_uri: some-image\n" +
				"inputs:\n" +
				"- path: path/to/input\n" +
				"outputs:\n" +
				"- path: path/to/output\n" +
				"run:\n" +
				"  path: my-app/scripts/test\n" +
				"params: the-params\n"
		);
		editor.assertProblems(
				"image_resource|Only one of [image_resource, rootfs_uri, image] should be defined",
				"name|Unknown property",
				"bogus-source-prop|Unknown property",
				"rootfs_uri|Only one of [image_resource, rootfs_uri, image] should be defined",
				"-^ path: path/to/input|'name' is required",
				"-^ path: path/to/output|'name' is required",
				"the-params|Expecting a 'Map'"
		);
	}

	@Test public void taskFileMissingToplevelPropertiesUnderlinesLastNonWhitespaceChar() throws Exception {
		Editor editor;

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"rootfs_uri: some-image"
		);
		editor.assertProblems("rootfs_uri: some-imag^e^|[platform, run] are required");

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"rootfs_uri: some-image\n" +
				"   \n"
		);
		editor.assertProblems("rootfs_uri: some-imag^e^|[platform, run] are required");

	}

	@Test public void contentAssistTaskFileToplevelProperties() throws Exception {
		assertTaskCompletions(
				"<*>"
				, // ==>
				"platform: $1\n" +
				"run:\n" +
				"  path: $2<*>"
				,
				"platform: <*>"
				,
				"run:\n" +
				"  path: <*>"
		);

		assertContextualTaskCompletions(
				"run: {}\n" +
				"platform: linux\n" +
				"<*>"
				,
				"<*>"
				, // ==>
				"caches:\n" +
				"- path: <*>"
				,
				"image_resource:\n" +
				"  type: <*>"
				,
				"inputs:\n" +
				"- name: <*>"
				,
				"outputs:\n" +
				"- name: <*>"
				,
				"params:\n" +
				"  <*>"
				,
				"rootfs_uri: <*>"
				,
				"image: <*>"
		);

		assertTaskCompletions(
				"platform: <*>"
				, //=>
				"platform: darwin<*>",
				"platform: linux<*>",
				"platform: windows<*>"
		);
	}

	@Test public void hoversForTaskFileToplevelProperties() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"image: some-image\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/sts4-build-env\n" +
				"inputs: []\n" +
				"outputs: []\n" +
				"params: {}\n" +
				"platform: linux\n" +
				"run:\n" +
				"  path: sts4/concourse/tasks/build-vscode-extensions.sh"
		);

		editor.assertHoverContains("platform", "The platform the task should run on");
		editor.assertHoverContains("image_resource", "The base image of the container");
		editor.assertHoverContains("image", "A string specifying the rootfs of the container");
		editor.assertHoverContains("inputs", "The expected set of inputs for the task");
		editor.assertHoverContains("outputs", "The artifacts produced by the task");
		editor.assertHoverContains("run", "Note that this is *not* provided as a script blob");
		editor.assertHoverContains("params", "A key-value mapping of values that are exposed to the task via environment variables");
		editor.assertHoverContains("repository", "The name of the repository");

		editor.assertHoverContains("platform", "The platform the task should run on");
	}

	@Test public void reconcileEmbeddedTaskConfig() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo\n" +
				"  plan:\n" +
				"  - task: the-task\n" +
				"    config:\n" +
				"      platform: a-platform\n" +
				"      image_resource:\n" +
				"        name: should-not-be-here\n" +
				"        type: docker-image\n" +
				"        source:\n" +
				"          bogus-source-prop: bad\n" +
				"          repository: ruby\n" +
				"          tag: '2.1'\n" +
				"      rootfs_uri: some-image\n" +
				"      inputs:\n" +
				"      - path: path/to/input\n" +
				"      outputs:\n" +
				"      - path: path/to/output\n" +
				"      run:\n" +
				"        path: my-app/scripts/test\n" +
				"      params: the-params"
		);
		editor.assertProblems(
				"image_resource|Only one of [image_resource, rootfs_uri, image] should be defined",
				"name|Unknown property",
				"bogus-source-prop|Unknown property",
				"rootfs_uri|Only one of [image_resource, rootfs_uri, image] should be defined",
				"-^ path: path/to/input|'name' is required",
				"-^ path: path/to/output|'name' is required",
				"the-params|Expecting a 'Map'"
		);
	}

	@Test public void taskRunPropertiesValidationAndHovers() throws Exception {
		Editor editor;

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"inputs:\n" +
				"- name: sts4\n" +
				"outputs:\n" +
				"- name: vsix-files\n" +
				"platform: linux\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/sts4-build-env\n" +
				"run:\n" +
				"  path: sts4/concourse/tasks/build-vscode-extensions.sh\n" +
				"  args: the-args\n" +
				"  user: admin\n" +
				"  dir: the-dir\n" +
				"  bogus: bad\n"
		);
		editor.assertProblems(
				"the-args|Expecting a 'Sequence'",
				"bogus|Unknown property"
		);

		editor.assertHoverContains("path", "The command to execute, relative to the task's working directory");
		editor.assertHoverContains("args", "Arguments to pass to the command");
		editor.assertHoverContains("dir", "A directory, relative to the initial working directory, to set as the working directory");
		editor.assertHoverContains("user", "Explicitly set the user to run as");

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"inputs:\n" +
				"- name: sts4\n" +
				"outputs:\n" +
				"- name: vsix-files\n" +
				"platform: linux\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: kdvolder/sts4-build-env\n" +
				"run:\n" +
				"  user: admin\n"
		);
		editor.assertProblems("run|'path' is required");
	}

	@Test public void nameAndPathHoversInTaskInputsAndOutputs() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"inputs:\n" +
				"- name: sts4\n" +
				"  path: botk\n" +
				"outputs:\n" +
				"- name: vsix-files\n" +
				"  path: zaza"
		);
		editor.assertHoverContains("name", 1, "The logical name of the input");
		editor.assertHoverContains("name", 2, "The logical name of the output");
		editor.assertHoverContains("path", 1, "The path where the input will be placed");
		editor.assertHoverContains("path", 2, "The path to a directory where the output will be taken from");
	}

	@Test public void PT_140711495_triple_dash_at_start_of_file_disrupts_content_assist() throws Exception {
		assertContextualCompletions(
				"#leading comment\n" +
				"---\n" +
				"resources:\n" +
				"- name: my-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: https://github.com/spring-projects/sts4.git\n" +
				"    <*>"
				, // ==================
				"bra<*>"
				, // ==>
				"branch: <*>"
		);

		assertContextualCompletions(
				"---\n" +
				"resources:\n" +
				"- name: my-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: https://github.com/spring-projects/sts4.git\n" +
				"    <*>"
				, // ==================
				"bra<*>"
				, // ==>
				"branch: <*>"
		);

		assertContextualCompletions(
//				"---\n" +
				"resources:\n" +
				"- name: my-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: https://github.com/spring-projects/sts4.git\n" +
				"    <*>"
				, // ==================
				"bra<*>"
				, // ==>
				"branch: <*>"
		);
	}

	@Test public void PT_141050891_language_server_crashes_on_CA_before_first_document_marker() throws Exception {
		Editor editor = harness.newEditor(
				"%Y<*>\n" +
				"#leading comment\n" +
						"---\n" +
						"resources:\n" +
						"- name: my-repo\n" +
						"  type: git\n" +
						"  source:\n" +
						"    uri: https://github.com/spring-projects/sts4.git\n" +
						"    <*>"
		);
		//We don't expect completions, but this should at least not crash!
		editor.assertCompletions(/*NONE*/);
	}

	@Test public void resourceInEmbeddedTaskConfigNotRequiredIfSpecifiedInTask() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: docker-image\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    username: {{docker_hub_username}}\n" +
				"    password: {{docker_hub_password}}\n" +
				"    repository: kdvolder/sts3-build-env\n" +
				"jobs:\n" +
				"- name: build-commons-update-site\n" +
				"  plan:\n" +
				"  - task: hello-world\n" +
				"    image: docker-image\n" + //Given here! So not required in config!
				"    config:\n" +
				"      inputs:\n" +
				"      - name: commons-git\n" +
				"      platform: linux\n" +
				"      run:\n" +
				"        path: which\n" +
				"        args:\n" +
				"        - mvn"
		);
		editor.assertProblems(/*none*/);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: docker-image\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    username: {{docker_hub_username}}\n" +
				"    password: {{docker_hub_password}}\n" +
				"    repository: kdvolder/sts3-build-env\n" +
				"jobs:\n" +
				"- name: build-commons-update-site\n" +
				"  plan:\n" +
				"  - task: hello-world\n" +
				"    config:\n" +
				"      inputs:\n" +
				"      - name: commons-git\n" +
				"      platform: linux\n" +
				"      run:\n" +
				"        path: which\n" +
				"        args:\n" +
				"        - mvn"
		);

		editor.assertProblems(
				"docker-image|Unused",
				"config|One of [image_resource, rootfs_uri, image] is required"
		);
	}

	@Test public void resourceInTaskConfigFileNotRequired() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"inputs:\n" +
				"- name: commons-git\n" +
				"platform: linux\n" +
				"run:\n" +
				"  path: which\n" +
				"  args:\n" +
				"  - mvn"
		);
		editor.assertProblems(/*NONE*/);
	}

	@Test public void resourceInEmbeddedTaskConfigDeprecated() throws Exception {
		Editor editor = harness.newEditor(
			"resources:\n" +
			"- name: my-docker-image\n" +
			"  type: docker-image\n" +
			"  source:\n" +
			"    username: {{docker_hub_username}}\n" +
			"    password: {{docker_hub_password}}\n" +
			"    repository: kdvolder/sts3-build-env\n" +
			"jobs:\n" +
			"- name: build-commons-update-site\n" +
			"  plan:\n" +
			"  - task: hello-world\n" +
			"    image: my-docker-image\n" +
			"    config:\n" +
			"      rootfs_uri: blah\n" +
			"      image_resource:\n" +
			"        type: docker-image\n" +
			"      inputs:\n" +
			"      - name: commons-git\n" +
			"      platform: linux\n" +
			"      run:\n" +
			"        path: which\n" +
			"        args:\n" +
			"        - mvn"
		);
		List<Diagnostic> problems = editor.assertProblems(
				"rootfs_uri|Deprecated",
				"image_resource|Deprecated"
		);
		for (Diagnostic d : problems) {
			assertEquals(DiagnosticSeverity.Warning, d.getSeverity());
		}
	}

	@Test public void relaxedIndentContextMoreSpaces() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"  <*>"
		);
		editor.assertCompletionLabels(
				//For the 'exact' context:
				"check_every",
				"webhook_token",
				//"name", exists
				//"source", exists
				//"type", exists
				//For the nested context:
				"→ uri",
				// For the top-level context:
				"← groups",
				"← jobs",
				"← resource_types",
				"← - Resource Snippet",
				// For the 'next job' context:
				"← - name"
		);

		editor.assertCompletionWithLabel("check_every",
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"  check_every: <*>"
		);

		editor.assertCompletionWithLabel("→ uri",
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: <*>"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"  <*>"
		);
		editor.assertCompletionWithLabel("→ commit_verification_key_ids",
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"    commit_verification_key_ids:\n" +
				"    - <*>"
		);
	}

	@Test public void relaxedIndentContextMoreSpaces2() throws Exception {
		assertContextualCompletions(INDENTED_COMPLETION,
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"  <*>"
				, // =========
				"ur"
				, //=>
				"  uri: <*>"
		);

		assertContextualCompletions(INDENTED_COMPLETION,
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"  <*>"
				, // =========
				"bra"
				, //=>
				"  branch: <*>"
		);

		assertContextualCompletions(
				"resources:\n" +
				"- name: foo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"  <*>"
				, // =========
				"comverids<*>"
				, //=>
				"  commit_verification_key_ids:\n" +
				"    - <*>"
		);
	}

	@Test public void jobPropertyHovers() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"  - name: job\n" +
				"    serial: true\n" +
				"    build_logs_to_retain: 10\n" +
				"    serial_groups: []\n" +
				"    max_in_flight: 3\n" +
				"    public: false\n" +
				"    disable_manual_trigger: true\n" +
				"    interruptible: true\n" +
				"    plan:\n" +
				"      - get: code\n" +
				"    on_failure:\n" +
				"      put: code\n" +
				"    on_success:\n" +
				"      put: code\n" +
				"    ensure:\n" +
				"      put: code\n" +
				"resources:\n" +
				"- name: code\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"    branch: master\n"
		);
		editor.assertProblems(/*NONE*/);
		editor.assertHoverContains("name", "The name of the job");
		editor.assertHoverContains("serial", "execute one-by-one");
		editor.assertHoverContains("build_logs_to_retain", "only the last specified number of builds");
		editor.assertHoverContains("serial_groups", "referencing the same tags will be serialized");
		editor.assertHoverContains("max_in_flight", "maximum number of builds to run at a time");
		editor.assertHoverContains("public", "build log of this job will be viewable");
		editor.assertHoverContains("disable_manual_trigger", "manual triggering of the job");
		editor.assertHoverContains("interruptible", "worker will not wait on the builds");
		editor.assertHoverContains("on_success", "Step to execute when the job succeeds");
		editor.assertHoverContains("on_failure", "Step to execute when the job fails");
		editor.assertHoverContains("ensure", "Step to execute regardless");
	}

	@Test public void jobPropertyReconcile() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"  - name: job\n" +
				"    serial: isSerial\n" +
				"    build_logs_to_retain: retainers\n" +
				"    serial_groups: no-list\n" +
				"    max_in_flight: flying-number\n" +
				"    public: publicize\n" +
				"    disable_manual_trigger: nomanual\n" +
				"    interruptible: nointerrupt\n" +
				"    plan:\n" +
				"      - get: a-resource\n" +
				"    on_failure:\n" +
				"      put: b-resource\n" +
				"    on_success:\n" +
				"      put: a-resource\n" +
				"    ensure:\n" +
				"      put: c-resource\n" +
				"resources:\n" +
				"- name: b-resource\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"- name: code\n" +
				"  type: git\n" +
				"  source:\n" +
				"    uri: blah\n" +
				"    branch: master\n"
		);
		editor.assertProblems(
				"isSerial|boolean",
				"retainers|Number",
				"no-list|Expecting a 'Sequence'",
				"flying-number|Number",
				"publicize|boolean",
				"nomanual|boolean",
				"nointerrupt|boolean",
				"a-resource|resource does not exist",
				"b-resource|should define 'branch'",
				"a-resource|resource does not exist",
				"c-resource|resource does not exist",
				"code|Unused"
		);
	}

	@Test public void relaxedIndentContextMoreSpaces3() throws Exception {
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: job-hello-world\n" +
				"  public: true\n" +
				"  plan:\n" +
				"  - get: resource-tutorial\n" +
				"  - task: hello-world\n" +
				"  <*>"
		);

		editor.assertCompletionLabels(
				//completions for current (i.e Job) context:
				"build_logs_to_retain",
				"disable_manual_trigger",
				"ensure",
				"interruptible",
				"max_in_flight",
				"on_failure",
				"on_success",
				"serial",
				"serial_groups",
				//"name", exists
				//"plan", exists
				//"public", exists
				//Completions for nested context (i.e. task step)
				"→ attempts",
				"→ config",
				"→ ensure",
				"→ file",
				"→ image",
				"→ input_mapping",
				"→ on_failure",
				"→ on_success",
				"→ output_mapping",
				"→ params",
				"→ privileged",
				"→ tags",
				"→ timeout",
				//Completions with '-'
				"- aggregate",
				"- do",
				"- get",
				"- put",
				"- task",
				"- try",
				//Dedented completions
				"← groups",
				"← resource_types",
				"← resources",
				"← - Job Snippet",
				"← - name"
			);
	}

	@Test public void gotoSymbolInPipeline() throws Exception {
		Editor editor = harness.newEditor(
				"resource_types:\n" +
				"- name: some-resource-type\n" +
				"resources:\n" +
				"- name: foo-resource\n" +
				"- name: bar-resource\n" +
				"jobs:\n" +
				"- name: do-some-stuff\n" +
				"- name: do-more-stuff\n"
		);

		editor.assertDocumentSymbols(
				"some-resource-type|ResourceType",
				"foo-resource|Resource",
				"bar-resource|Resource",
				"do-some-stuff|Job",
				"do-more-stuff|Job"
		);
	}

	@Test public void reconcilerRaceCondition() throws Exception {
		SynchronizationPoint reconcilerThreadStart = harness.reconcilerThreadStart();
		Editor editor = harness.newEditor("garbage");

		reconcilerThreadStart.reached(); // Blocks until the reconciler thread is reached.
		try {
			String editorContents = editor.getRawText();
			for (int i = 0; i < 4; i++) {
				editorContents = "\n" +editorContents;
				editor.setText(editorContents);
			}
		} finally {
			reconcilerThreadStart.unblock();
		}

		editor.assertRawText(
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"garbage"
		);
		editor.assertProblems("garbage|Expecting a 'Map'");
	}

	@Test public void noAutoInsertRequiredSourcePropertiesIfPresent() throws Exception {
		Editor editor;

		//Most common case
		editor = harness.newEditor(
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: <*>\n"+
				"  source:"
		);
		editor.assertCompletionWithLabel((l) -> l.startsWith("pool"),
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: pool<*>\n" +
				"  source:"
		);

	}

	@Test public void autoInsertRequiredSourceProperties() throws Exception {
		Editor editor;

		//Most common case
		editor = harness.newEditor(
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: <*>"
		);
		editor.assertCompletionWithLabel((l) -> l.startsWith("pool"),
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: pool\n" +
				"  source:\n" +
				"    uri: $1\n" +
				"    branch: $2\n" +
				"    pool: $3<*>"
		);

		// What if we use somewhat different indentation style?
		editor = harness.newEditor(
				"resources:\n" +
				"  - name: source-repo\n" +
				"    type: <*>"
		);
		editor.assertCompletionWithLabel((l) -> l.startsWith("pool"),
				"resources:\n" +
				"  - name: source-repo\n" +
				"    type: pool\n" +
				"    source:\n" +
				"      uri: $1\n" +
				"      branch: $2\n" +
				"      pool: $3<*>"
		);
	}

	@Ignore
	@Test public void autoInsertRequiredSourceProperties3() throws Exception {
		//This case can not be implemented correctly because of the magic indentations that vscode
		// automatically applies. The magic indents will allways indent the extra lines we insert after
		// the value to be indented to the level of that value. So it is impossible to create an edit
		// where the text on the lines following it is indented *less* than that value, which is what
		// is required to implement this case correctly.

		//What if the type was on a new line (this is odd, but anyhow)
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: \n" +
				"    <*>"
		);
		editor.assertCompletionWithLabel((l) -> l.startsWith("pool"),
				"resources:\n" +
				"- name: source-repo\n" +
				"  type: \n" +
				"    pool\n" +
				"  source:\n" +
				"    uri: $1\n" +
				"    branch: $2\n" +
				"    pool: $3<*>"
		);
	}



	@Test public void reconcilerJobFromPassedAttributeMustInteractWithResource() throws Exception {
		Editor editor;

		editor = harness.newEditor(
			"resources:\n" +
			"- name: version\n" +
			"  type: semver\n" +
			"- name: source-repo\n" +
			"  type: git\n" +
			"jobs:\n" +
			"- name: build-it\n" +
			"  plan:\n" +
			"  - aggregate:\n" +
			"    # - put: version\n" +
			"    - get: source-repo\n" +
			"- name: test-it\n" +
			"  plan:\n" +
			"  - get: source-repo\n" +
			"    passed:\n" +
			"    - build-it # <- good\n" +
			"  - get: version\n" +
			"    passed:\n" +
			"    - build-it # <- bad\n"
		);
		editor.assertProblems(
				"build-it^ # <- bad|Job 'build-it' does not interact with resource 'version'"
		);

		editor = harness.newEditor(
			"resources:\n" +
			"- name: version\n" +
			"  type: semver\n" +
			"- name: source-repo\n" +
			"  type: git\n" +
			"jobs:\n" +
			"- name: build-it\n" +
			"  plan:\n" +
			"  - aggregate:\n" +
			"    - put: version\n" +
			"    # - get: source-repo\n" +
			"- name: test-it\n" +
			"  plan:\n" +
			"  - get: source-repo\n" +
			"    passed:\n" +
			"    - build-it # <- bad\n" +
			"  - get: version\n" +
			"    passed:\n" +
			"    - build-it # <- good\n"
		);
		editor.assertProblems(
				"build-it^ # <- bad|Job 'build-it' does not interact with resource 'source-repo'"
		);

		//Check that we find interactions in steps that are at the top-level of the plan:
		editor = harness.newEditor(
			"resources:\n" +
			"- name: version\n" +
			"  type: semver\n" +
			"- name: source-repo\n" +
			"  type: git\n" +
			"jobs:\n" +
			"- name: build-it\n" +
			"  plan:\n" +
			"  - aggregate:\n" +
			"    - put: version\n" +
			"    - get: source-repo\n" +
			"- name: test-it\n" +
			"  plan:\n" +
			"  - get: source-repo\n" +
			"    passed:\n" +
			"    - build-it\n" +
			"  - get: version\n" +
			"    passed:\n" +
			"    - build-it\n"
		);
		editor.assertProblems(/*NONE*/);

		//Check that we find interactions in steps that are nested in other steps
		editor = harness.newEditor(
			"resources:\n" +
			"- name: version\n" +
			"  type: semver\n" +
			"- name: source-repo\n" +
			"  type: git\n" +
			"jobs:\n" +
			"- name: build-it\n" +
			"  plan:\n" +
			"  - put: version\n" +
			"  - get: source-repo\n" +
			"- name: test-it\n" +
			"  plan:\n" +
			"  - get: source-repo\n" +
			"    passed:\n" +
			"    - build-it\n" +
			"  - get: version\n" +
			"    passed:\n" +
			"    - build-it\n"
		);
		editor.assertProblems(/*NONE*/);

	}

	@Test public void reconcilerSkipInteractsWithChecckedForNonExistantResource() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/144217965
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"- name: source-repo\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: build-it\n" +
				"  plan:\n" +
				"  - aggregate:\n" +
				"    - put: version\n" +
				"    - get: source-repo\n" +
				"- name: test-it\n" +
				"  plan:\n" +
				"  - get: source-repo\n" +
				"    passed:\n" +
				"    - build-it\n" +
				"  - get: versi\n" +
				"    passed:\n" +
				"    - build-it"
		);

		editor.assertProblems("get: ^versi^|resource does not exist");
	}

	@Test public void relaxedContentAssistContextForListItem_sameLine() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  plan: <*>\n"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  plan: \n" +
				"  - put: <*>\n"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  plan: pu<*>\n"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  plan: \n" +
				"  - put: <*>\n"
		);
	}

	@Test public void relaxedContentAssistContextForListItem_indented() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"    - get: docker-git\n" +
				"      trigger: true\n" +
				"    <*>"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"    - get: docker-git\n" +
				"      trigger: true\n" +
				"    - put: <*>"
		);
	}
	@Test public void relaxedContentAssistContextForListItem_not_indented() throws Exception {
		Editor editor;

		//Note: this is a tricky case because when <*> lines up with 'plan' it will not be considered
		// to be a child of 'plan' but a child of the 'job' instead.
		// However, for hypothetical '- ' completions we'd have to treat as a child of plan instead!

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"  <*>"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"  - put: <*>"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  <*>"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - put: <*>"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  pu<*>"
		);
		editor.assertCompletionWithLabel("- put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - put: <*>"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  ag"
		);
		editor.assertCompletionWithLabel("- aggregate",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - aggregate:\n" +
				"    - <*>"
		);
	}

	@Test public void relaxedContentAssist_primary_properties() throws Exception{
		//See https://www.pivotaltracker.com/story/show/144584163
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: docker-git\n" +
				"<*>"
		);
		editor.assertCompletionLabels(
				"groups",
				"jobs",
				"resource_types",
				"→ type",
				"- Resource Snippet",
				"- name"
		);
	}

	@Test public void relaxedContentAssistLessSpaces() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"    <*>"
		);
		editor.assertCompletionWithLabel("← - put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"  - put: <*>"
		);

		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"    pu<*>"
		);
		editor.assertCompletionWithLabel("← - put",
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    trigger: true\n" +
				"  - put: <*>"
		);

		// De-indentation relaxation should not be allowed if they cause the context node to be split.
		// So in this example de-indented completions shouldn't be suggested.
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: build-docker-image\n" +
				"  serial: true\n" +
				"  plan:\n" +
				"  - get: docker-git\n" +
				"    <*>\n" +
				"    trigger: true\n"
		);
		editor.assertNoCompletionsWithLabel(label -> label.startsWith(Unicodes.LEFT_ARROW+" "));;
	}

	@Test public void reconcileUnusedResources() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resources:\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"- name: source-repo\n" +
				"  type: git\n" +
				"jobs:\n" +
				"- name: build-it\n" +
				"  plan:\n" +
				"  - get: version\n"
		);
		Diagnostic p = editor.assertProblems("source-repo|Unused 'Resource'").get(0);
		assertEquals(DiagnosticSeverity.Error, p.getSeverity());

		editor = harness.newEditor(
				"resources:\n" +
				"- name: not-used\n" +
				"  type: pool\n" +
				"- name: version\n" +
				"  type: semver\n" +
				"- name: source-repo\n" +
				"  type: git\n" +
				"  source:\n" +
				"    branch: master\n" +
				"    uri: git@github.com/blah\n" +
				"jobs:\n" +
				"- name: build-it\n" +
				"  plan:\n" +
				"  - aggregate:\n" +
				"    - get: not-used\n" +  // <-- This isn't a real use but looks like one!
				"      resource: version\n" +
				"    - put: source-repo\n"
		);
		editor.assertProblems(
				"not-used|Unused 'Resource'"
		);
	}

	@Test public void gitResourceFetchParameter() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"  - name: cf-networking-dev\n" +
				"    type: git\n" +
				"    source:\n" +
				"      uri: git@github.com:cloudfoundry-incubator/cf-networking-release.git\n" +
				"      branch: develop\n" +
				"      ignore_paths:\n" +
				"        - docs\n" +
				"      private_key: {{cf-networking-deploy-key}}\n" +
				"jobs:\n" +
				"- name: foo\n" +
				"  plan:\n" +
				"  - get: cf-networking-dev\n" +
				"    params:\n" +
				"      fetch: [master]\n" +
				"      submodules: none\n"
		);
		Diagnostic p = editor.assertProblems("fetch|Deprecated").get(0);
		assertEquals(DiagnosticSeverity.Warning, p.getSeverity());
	}

	@Test public void bug_150337510() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/150337510
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: test\n" +
				"  type: s3\n" +
				"  source:\n" +
				"    bucket: blah\n" +
				"    regexp: blah/blah*.tar.gz\n" +
				"jobs:\n" +
				"- name: build-it\n" +
				"  plan:\n" +
				"  - task: build-it\n" +
				"    file: tasks/build-it.yml\n" +
				"  on_success:\n" +
				"    put: test\n" +
				"- name: create-website\n" +
				"  plan:\n" +
				"  - get: test\n" +
				"    passed:\n" +
				"    - build-it"
		);
		editor.assertProblems(/*NONE*/);
	}

	@Test public void cfResourceSourceCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: pws\n" +
				"  type: cf\n" +
				"  source:\n" +
				"    <*>"
		);
		editor.assertContextualCompletions(PLAIN_COMPLETION, "<*>",
				//Snippet:
				"api: $1\n" +
				"    username: $2\n" +
				"    password: $3\n" +
				"    organization: $4\n" +
				"    space: $5<*>"
				, // non-snippet:
				"api: <*>",
				"organization: <*>",
				"password: <*>",
				"space: <*>",
				"username: <*>"
		);

		editor = harness.newEditor(
				"resources:\n" +
				"- name: pws\n" +
				"  type: cf\n" +
				"  source:\n" +
				"    api: {{cf_api}}\n" +
				"    username: {{cf_user}}\n" +
				"    password: {{cf_password}}\n" +
				"    organization: {{cf_org}}\n" +
				"    space: {{cf_space}}\n" +
				"    <*>"
		);
		editor.assertContextualCompletions(PLAIN_COMPLETION, "<*>",
				"skip_cert_check: <*>"
		);
	}

	@Test public void cfResourceSourceHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: pws\n" +
				"  type: cf\n" +
				"  source:\n" +
				"    api: {{cf_api}}\n" +
				"    username: {{cf_user}}\n" +
				"    password: {{cf_password}}\n" +
				"    organization: {{cf_org}}\n" +
				"    space: {{cf_space}}\n" +
				"    skip_cert_check: true<*>"
		);
		editor.assertHoverContains("api", "address of the Cloud Controller");
		editor.assertHoverContains("username", "username used to authenticate");
		editor.assertHoverContains("password", "password used to authenticate");
		editor.assertHoverContains("organization", "organization to push");
		editor.assertHoverContains("space", "space to push");
		editor.assertHoverContains("skip_cert_check", "Check the validity of the CF SSL cert");
	}

	@Test public void cfPutParamsHovers() throws Exception {
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: pws\n" +
				"  type: cf\n" +
				"jobs:\n" +
				"- name: deploy-stuff\n" +
				"  plan:\n" +
				"  - put: pws\n" +
				"    params:\n" +
				"      manifest: repo/manifest.yml\n" +
				"      path: out/*.jar\n" +
				"      current_app_name: the-name\n" +
				"      environment_variables:\n" +
				"        key: value\n" +
				"        key2: value2\n"
		);
		editor.assertHoverContains("manifest", "Path to a application manifest file");
		editor.assertHoverContains("path", "Path to the application to push");
		editor.assertHoverContains("current_app_name", "zero-downtime deploy");
		editor.assertHoverContains("environment_variables", "Environment variables");
	}

	@Test public void bug_152918825_no_reconciling_for_double_parens_placeholders() throws Exception {
		//https://www.pivotaltracker.com/story/show/152918825
		Editor editor = harness.newEditor(
				"resources:\n" +
				"- name: image-XXX\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: ((DOCKER_IMAGE))\n" +
				"    insecure_registries: ((DOCKER_INSECURE_REGISTRIES))\n" +
				"    tag: latest"
		);
		editor.assertProblems(
				"image-XXX|Unused 'Resource'"
		);
	}

	@Test public void getStepVersionShouldAcceptLatestAndEvery() throws Exception {
		//See https://github.com/spring-projects/sts4/pull/24
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: cf-deployment-git\n" +
				"    version: latest\n" +
				"  - get: some-resource\n" +
				"    version: every\n" +
				"  - get: other-resource\n" +
				"    version: bogus"
		);
		editor.assertProblems(
				"cf-deployment-git|resource does not exist",
				"some-resource|resource does not exist",
				"other-resource|resource does not exist",
				"bogus|Valid values are: [every, latest]"
		);
	}

	@Test public void getStepVersionShouldAcceptMap() throws Exception {
		//See https://github.com/spring-projects/sts4/pull/24
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: cf-deployment-git\n" +
				"    version: { ref: ((cf_deployment_commit_ref)) }"
		);
		editor.assertProblems("cf-deployment-git|resource does not exist");
	}

	@Test public void getStepVersionCompletionsSuggestLatestAndEvery() throws Exception {
		//See https://github.com/spring-projects/sts4/pull/24
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: cf-deployment-git\n" +
				"    version: <*>"
		);
		editor.assertCompletionLabels("every", "latest");
	}

	@Test public void getStepVersionMapStringStringValidation() throws Exception {
		//See https://github.com/spring-projects/sts4/pull/24
		Editor editor = harness.newEditor(
				"jobs:\n" +
				"- name: do-stuff\n" +
				"  plan:\n" +
				"  - get: cf-deployment-git\n" +
				"    version: { ref: good}\n" +
				"  - get: other-rsrc\n" +
				"    version: { ref: [bad]}\n"
		);
		editor.assertProblems(
				"cf-deployment-git|resource does not exist",
				"other-rsrc|resource does not exist",
				"[bad]|Expecting a 'String' but found a 'Sequence'"
		);
	}

	@Test public void taskCachesReconcile() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/153861788
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"\n" +
				"inputs:\n" +
				"- name: project-src\n" +
				"\n" +
				"caches:\n" +
				"- path: project-src/node_modules\n" +
				"  junk: bad\n" +
				"\n" +
				"run:\n" +
				"  path: project-src/ci/build"
		);
		editor.assertProblems("junk|Unknown property");
	}

	@Test public void taskCachesCompletions() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/153861788
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"\n" +
				"inputs:\n" +
				"- name: project-src\n" +
				"\n" +
				"caches:\n" +
				"- <*>\n" +
				"run:\n" +
				"  path: project-src/ci/build"
		);
		editor.assertContextualCompletions(PLAIN_COMPLETION, "<*>", "path: <*>");
	}

	@Test public void taskCachesHovers() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"\n" +
				"inputs:\n" +
				"- name: project-src\n" +
				"\n" +
				"caches:\n" +
				"- path: project-src/node_modules\n" +
				"  junk: bad\n" +
				"\n" +
				"run:\n" +
				"  path: project-src/ci/build"
		);
		editor.assertHoverContains("caches", "Caches are scoped to the worker the task is run on");
		editor.assertHoverContains("path", "The path to a directory to be cached");
	}

	@Test public void image_resource_completions() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"<*>"
		);
		editor.assertContextualCompletions(PLAIN_COMPLETION,
				"imgrs<*>"
				, // ==>
				"image_resource:\n" +
				"  type: <*>"
		);

		editor.setText(
			"platform: linux\n" +
			"run:\n" +
			"  path: blah\n" +
			"image_resource:\n" +
			"  type: <*>"
		);
		editor.assertContextualCompletions("dckr<*>",
				"docker-image\n" +
				"  source:\n" +
				"    repository: <*>"
		);

		editor.setText(
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  <*>"
		);
		editor.assertContextualCompletions(PLAIN_COMPLETION, "<*>"
				, // =>
				  "params:\n" +
				"    <*>"
				, // ----
				  "source:\n" +
				"    <*>" //Actually... would expect 'repository' to be filled in by snippet here!
				, // ----
				  "version:\n" +
				"    <*>"
		);
	}

	@Test public void image_resource_subHovers() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  params: {}\n" +
				"  source:\n" +
				"    repository: some-docker-image\n" +
				"  version: latest"
		);
		editor.assertHoverContains("type", "type of the resource. Usually `docker-image`.");
		editor.assertHoverContains(" source", "The location of the resource");
		editor.assertHoverContains("params", "A map of arbitrary configuration to forward to the resource");
		editor.assertHoverContains("version", "A specific version of the resource to fetch");
	}

	@Test public void image_resource_version_reconcile() throws Exception {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: some-docker-image\n" +
				"  version: latest"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: some-docker-image\n" +
				"  version: every"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: some-docker-image\n" +
				"  version:\n"+
				"    anystring: goes\n"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(LanguageId.CONCOURSE_TASK,
				"platform: linux\n" +
				"run:\n" +
				"  path: blah\n" +
				"image_resource:\n" +
				"  type: docker-image\n" +
				"  source:\n" +
				"    repository: some-docker-image\n" +
				"  version: not-a-valid-version"
		);
		editor.assertProblems("not-a-valid-version|Valid values are: [every, latest]");
	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertContextualCompletions(String conText, String textBefore, String... textAfter) throws Exception {
		assertContextualCompletions((c) -> true, conText, textBefore, textAfter);
	}

	private void assertContextualCompletions(Predicate<CompletionItem> isInteresting, String conText, String textBefore, String... textAfter) throws Exception {
		assertContextualCompletions(LanguageId.CONCOURSE_PIPELINE, isInteresting, conText, textBefore, textAfter);
	}

	private void assertContextualCompletions(LanguageId language, Predicate<CompletionItem> isInteresting, String conText, String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(language, conText);
		editor.reconcile(); //this ensures the conText is parsed and its AST is cached (will be used for
		                    //dynamic CA when the conText + textBefore is not parsable.
		assertContains(CURSOR, conText);
		textBefore = conText.replace(CURSOR, textBefore);
		textAfter = Arrays.stream(textAfter)
				.map((String t) -> conText.replace(CURSOR, t))
				.collect(Collectors.toList()).toArray(new String[0]);
		editor.setText(textBefore);
		editor.assertCompletions(isInteresting, textAfter);
	}

	private void assertCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		editor.assertCompletions(textAfter);
	}

	private void assertTaskCompletions(String textBefore, String... textAfter) throws Exception  {
		Editor editor = harness.newEditor(LanguageId.CONCOURSE_TASK, textBefore);
		editor.assertCompletions(textAfter);
	}

	private void assertContextualTaskCompletions(String conText, String textBefore, String... textAfter) throws Exception {
		assertContextualCompletions(LanguageId.CONCOURSE_TASK, c -> true, conText, textBefore, textAfter);
	}

}
