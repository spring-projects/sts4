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
package org.springframework.ide.vscode.bosh;

import static org.junit.Assert.assertEquals;
import static org.springframework.ide.vscode.languageserver.testharness.Editor.PLAIN_COMPLETION;
import static org.springframework.ide.vscode.languageserver.testharness.TestAsserts.assertContains;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.bosh.mocks.MockCloudConfigProvider;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BoshEditorTest {

	LanguageServerHarness harness;

	private MockCloudConfigProvider cloudConfigProvider = new MockCloudConfigProvider();

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(() -> {
				return new BoshLanguageServer(cloudConfigProvider)
						.setMaxCompletions(100);
			},
			LanguageId.BOSH_DEPLOYMENT
		);
		harness.intialize(null);
	}

	@Test public void toplevelV2PropertyNamesKnown() throws Exception {
		Editor editor = harness.newEditor(
				"name: some-name\n" +
				"director_uuid: cf8dc1fc-9c42-4ffc-96f1-fbad983a6ce6\n" +
				"releases:\n" +
				"- name: redis\n" +
				"  version: 12\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu-trusty\n" +
				"  version: 3421.11\n" +
				"update:\n" +
				"  canaries: 1\n" +
				"  max_in_flight: 10\n" +
				"  canary_watch_time: 1000-30000\n" +
				"  update_watch_time: 1000-30000\n" +
				"instance_groups:\n" +
				"- name: redis-master\n" +
				"  instances: 1\n" +
				"  azs: [z1, z2]\n" +
				"  jobs:\n" +
				"  - name: redis-server\n" +
				"    release: redis\n" +
				"    properties:\n" +
				"      port: 3606\n" +
				"  vm_type: large\n" +
				"  vm_extensions: [public-lbs]\n" +
				"  stemcell: default\n" +
				"  persistent_disk_type: medium\n" +
				"  networks:\n" +
				"  - name: default\n" +
				"- name: redis-slave\n" +
				"  instances: 2\n" +
				"  azs: [z1, z2]\n" +
				"  jobs:\n" +
				"  - name: redis-server\n" +
				"    release: redis\n" +
				"    properties: {}\n" +
				"  vm_type: large\n" +
				"  stemcell: default\n" +
				"  persistent_disk_type: large\n" +
				"  networks:\n" +
				"  - name: default\n" +
				"variables:\n" +
				"- name: admin_password\n" +
				"  type: password\n" +
				"- name: default_ca\n" +
				"  type: certificate\n" +
				"  options:\n" +
				"    is_ca: true\n" +
				"    common_name: some-ca\n" +
				"- name: director_ssl\n" +
				"  type: certificate\n" +
				"  options:\n" +
				"    ca: default_ca\n" +
				"    common_name: cc.cf.internal\n" +
				"    alternative_names: [cc.cf.internal]\n" +
				"properties:\n" +
				"  a-property: the-value\n" +
				"tags:\n" +
				"  project: cf\n" +
				"blah: hoooo\n"
		);
		editor.assertProblems(
				"director_uuid|bosh v2 CLI no longer checks or requires",
				"properties|Deprecated in favor of job level properties and links",
				"blah|Unknown property"
		);

		editor.assertHoverContains("name", "The name of the deployment");
		editor.assertHoverContains("director_uuid", "This string must match the UUID of the currently targeted Director");
		editor.assertHoverContains("releases", "The name and version of each release in the deployment");
		editor.assertHoverContains("stemcells", "The name and version of each stemcell");
		editor.assertHoverContains("update", "This specifies instance update properties");
		editor.assertHoverContains("instance_groups", "Specifies the mapping between release [jobs](https://bosh.io/docs/terminology.html#job) and instance groups.");
		editor.assertHoverContains("properties", 3, "Describes global properties. Deprecated");
		editor.assertHoverContains("variables", "Describes variables");
		editor.assertHoverContains("tags", "Specifies key value pairs to be sent to the CPI for VM tagging");
	}

	//@Ignore //For now... because not passing yet.
	@Test public void reconcileCfManifest() throws Exception {
		Editor editor = harness.newEditorFromClasspath("/workspace/cf-deployment-manifest.yml");
		cloudConfigProvider.readWith(() -> {
			throw new IOException("Couldn't contact the director");
		});
		editor.assertProblems(/*NONE*/);
	}

	@Test public void toplevelPropertyCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"<*>"
		);
		editor.assertCompletions(
				"name: <*>"
		);

		editor = harness.newEditor(
				"name: blah\n" +
				"<*>"
		);

		editor.assertCompletions(
				"name: blah\n" +
				"instance_groups:\n" +
				"- name: <*>"
				, // ============
				"name: blah\n" +
				"releases:\n" +
				"- name: <*>"
				, // ============
				"name: blah\n" +
				"stemcells:\n- <*>"
				, // ============
				"name: blah\n" +
				"tags:\n  <*>"
				, // ============
				"name: blah\n" +
				"update:\n  <*>"
				, // ============
				"name: blah\n" +
				"variables:\n" +
				"- name: <*>"
// Below completions are suppressed because they are deprecated
//				, // ============
//				"name: blah\n" +
//				"director_uuid: <*>"
//				, // ============
//				"name: blah\n" +
//				"properties:\n  <*>"
		);
	}

	@Test public void stemcellCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"stemcells:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				"stemcells:\n" +
				"- alias: <*>"
				, // ==========
				"stemcells:\n" +
				"- name: <*>"
				, // ==========
				"stemcells:\n" +
				"- os: <*>"
				, // ==========
				"stemcells:\n" +
				"- version: <*>"
		);

		editor = harness.newEditor(
				"stemcells:\n" +
				"- alias<*>"
		);
	}

	@Test public void stemcellReconciling() throws Exception {
		Editor editor = harness.newEditor(
				"stemcells:\n" +
				"- {}"
		);
		editor.assertProblems(
				"-|One of [name, os] is required",
				"-|[alias, version] are required",
				"}|[instance_groups, name, releases, update] are required"
		);
	}

	@Test public void releasesBlockCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" +
				"- <*>"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertCompletions(
				"releases:\n" +
				"- name: <*>"
		);

		editor = harness.newEditor(
				"releases:\n" +
				"- name: foo\n" +
				"  <*>"
		);
		editor.assertCompletions(PLAIN_COMPLETION,
				"releases:\n" +
				"- name: foo\n" +
				"  sha1: <*>"
				, // ============
				"releases:\n" +
				"- name: foo\n" +
				"  url: <*>"
				, // ============
				"releases:\n" +
				"- name: foo\n" +
				"  version: <*>"
		);
	}

	@Test public void releasesAdvancedValidations() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" +
				"- name: some-release\n" +
				"  url: https://my.releases.com/funky.tar.gz\n" +
				"- name: other-relase\n" +
				"  url: file:///root/releases/a-nice-file.tar.gz\n" +
				"- name: bad-url\n" +
				"  url: proto://something.com\n" +
				"#x"
		);
		editor.assertProblems(
				"url|'sha1' is required when the 'url' is http(s)",
				"proto|Url scheme must be one of [http, https, file]",
				"x|are required"
		);
	}

	@Test public void releasesBlockPropertyReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" +
				"- name: some-release\n" +
				"  version: some-version\n" +
				"  url: https://my.releases.com/funky.tar.gz\n" +
				"  sha1: 440248a31253296b1626ad52886e58900730f32e\n" +
				"  woot: dunno\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"woot|Unknown property"
		);

		editor.assertHoverContains("name", "Name of a release used in the deployment");
		editor.assertHoverContains("version", "The version of the release to use");
		editor.assertHoverContains("url", "URL of the release to use");
		editor.assertHoverContains("sha1", "The SHA1 of the release tarball");

		editor = harness.newEditor(
				"releases:\n" +
				"- name: some-release\n" +
				"  version: <*>\n"
		);
		editor.assertCompletionLabels("latest");
	}

	@Test public void instanceGroupsCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"instance_groups:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				"instance_groups:\n" +
				"- name: <*>"
		);

		editor = harness.newEditor(
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  <*>"
		);
		editor.assertCompletions(PLAIN_COMPLETION,
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  azs:\n" +
				"  - <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  env:\n" +
				"    <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  instances: <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  jobs:\n" +
				"  - <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  lifecycle: <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  migrated_from:\n" +
				"  - <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  networks:\n" +
				"  - name: <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  persistent_disk_type: <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  stemcell: <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  update:\n" +
				"    <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  vm_extensions:\n" +
				"  - <*>"
				, // =============
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  vm_type: <*>"

//				, // =============
// Not suggested because its deprecated:
//				"instance_groups:\n" +
//				"- name: foo-group\n" +
//				"  properties:\n" +
//				"    <*>"
		);
	}

	@Test public void instanceGroupsHovers() throws Exception {
		Editor editor = harness.newEditor(
				"instance_groups:\n" +
				"- name: redis-master\n" +
				"  properties: {}\n" +
				"  instances: 1\n" +
				"  azs: [z1, z2]\n" +
				"  jobs:\n" +
				"  - name: redis-server\n" +
				"    release: redis\n" +
				"    properties:\n" +
				"      port: 3606\n" +
				"  vm_type: medium\n" +
				"  vm_extensions: [public-lbs]\n" +
				"  stemcell: default\n" +
				"  persistent_disk_type: medium\n" +
				"  networks:\n" +
				"  - name: default\n" +
				"\n" +
				"- name: redis-slave\n" +
				"  instances: 2\n" +
				"  azs: [z1, z2]\n" +
				"  jobs:\n" +
				"  - name: redis-server\n" +
				"    release: redis\n" +
				"    properties: {}\n" +
				"  update:\n" +
				"    canaries: 2\n" +
				"  lifecycle: errand\n" +
				"  migrated_from: []\n" +
				"  env: {}\n" +
				"  vm_type: medium\n" +
				"  stemcell: default\n" +
				"  persistent_disk_type: medium\n" +
				"  networks:\n" +
				"  - name: default\n"
		);
		editor.assertHoverContains("name", "A unique name used to identify and reference the instance group.");
		editor.assertHoverContains("azs", "List of AZs associated with this instance group");
		editor.assertHoverContains("instances", "The number of instances in this group");
		editor.assertHoverContains("jobs", "Specifies the name and release of jobs that will be installed on each instance.");
		editor.assertHoverContains("vm_type", "A valid VM type name from the cloud config");
		editor.assertHoverContains("vm_extensions", "A valid list of VM extension names from the cloud config");
		editor.assertHoverContains("stemcell", "A valid stemcell alias from the Stemcells Block");
		editor.assertHoverContains("persistent_disk_type", "A valid disk type name from the cloud config.");
		editor.assertHoverContains("networks", "Specifies the networks this instance requires");
		editor.assertHoverContains("update", "Specific update settings for this instance group");
		editor.assertHoverContains("migrated_from", "Specific migration settings for this instance group.");
		editor.assertHoverContains("lifecycle", "Specifies the kind of workload");
		editor.assertHoverContains("properties", "Specifies instance group properties");
		editor.assertHoverContains("env", "Specifies advanced BOSH Agent configuration");
	}

	@Test public void instanceGroups_job_hovers() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups:\n" +
				"- name: foo\n" +
				"  jobs:\n" +
				"  - name: the-job\n" +
				"    release: the-jobs-release\n" +
				"    properties:\n" +
				"      blah: blah\n" +
				"    consumes:\n" +
				"      blah: blah \n" +
				"    provides:\n" +
				"      blah: blah\n"
		);
		editor.assertHoverContains("name", 3, "The job name");
		editor.assertHoverContains("release", "The release where the job exists");
		editor.assertHoverContains("consumes", "Links consumed by the job");
		editor.assertHoverContains("provides", "Links provided by the job");
		editor.assertHoverContains("properties", "Specifies job properties");
	}

	@Test public void instanceGroups_network_hovers() throws Exception {
		Editor editor = harness.newEditor(
			"name: foo\n" +
			"instance_groups:\n" +
			"- name: foo\n" +
			"  networks:\n" +
			"  - name: the-network\n" +
			"    static_ips: []\n" +
			"    default: []\n"
		);
		editor.assertHoverContains("name", 3, "A valid network name from the cloud config");
		editor.assertHoverContains("static_ips", "Array of IP addresses");
		editor.assertHoverContains("default", "Specifies which network components");
	}

	@Test public void instanceGroups_env_hovers() throws Exception {
		Editor editor = harness.newEditor(
			"name: foo\n" +
			"instance_groups:\n" +
			"- name: foo\n" +
			"  env:\n" +
			"    bosh: {}\n" +
			"    password: []\n"
		);
		editor.assertHoverContains("bosh", "no description");
		editor.assertHoverContains("password", "Crypted password");
	}

	@Test public void updateBlockCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"update:\n" +
				"  <*>"
		);
		editor.assertCompletions(PLAIN_COMPLETION,
				"update:\n" +
				"  canaries: <*>"
				, // =====
				"update:\n" +
				"  canary_watch_time: <*>"
				, // =====
				"update:\n" +
				"  max_in_flight: <*>"
				, // =====
				"update:\n" +
				"  serial: <*>"
				, // =====
				"update:\n" +
				"  update_watch_time: <*>"
		);
	}

	@Test public void updateBlockHovers() throws Exception {
		Editor editor = harness.newEditor(
				"update:\n" +
				"  canaries: 1\n" +
				"  max_in_flight: 10\n" +
				"  canary_watch_time: 1000-30000\n" +
				"  update_watch_time: 1000-30000\n" +
				"  serial: false"
		);
		editor.assertHoverContains("canaries", "The number of [canary]");
		editor.assertHoverContains("max_in_flight", "maximum number of non-canary instances");
		editor.assertHoverContains("canary_watch_time", "checks whether the canary instances");
		editor.assertHoverContains("update_watch_time", "checks whether the instances");
		editor.assertHoverContains("serial", "deployed in parallel");
	}

	@Test public void variablesBlockCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"variables:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				"variables:\n" +
				"- name: <*>"
		);

		editor = harness.newEditor(
				"variables:\n" +
				"- name: foo\n" +
				"  <*>"
		);
		editor.assertCompletions(PLAIN_COMPLETION,
				"variables:\n" +
				"- name: foo\n" +
				"  options:\n" +
				"    <*>"
				, // ===============
				"variables:\n" +
				"- name: foo\n" +
				"  type: <*>"
		);

		editor = harness.newEditor(
				"variables:\n" +
				"- name: foo\n" +
				"  type: <*>"
		);
		editor.assertCompletionLabels("certificate", "password", "rsa", "ssh");
	}

	@Test public void variablesBlockHovers() throws Exception {
		Editor editor = harness.newEditor(
				"variables:\n" +
				"- name: admin_password\n" +
				"  type: password\n" +
				"- name: default_ca\n" +
				"  type: certificate\n" +
				"  options:\n" +
				"    is_ca: true\n" +
				"    common_name: some-ca\n" +
				"- name: director_ssl\n" +
				"  type: certificate\n" +
				"  options:\n" +
				"    ca: default_ca\n" +
				"    common_name: cc.cf.internal\n" +
				"    alternative_names: [cc.cf.internal]"
		);
		editor.assertHoverContains("name", "Unique name used to identify a variable");
		editor.assertHoverContains("type", "Type of a variable");
		editor.assertHoverContains("options", "Specifies generation options");
	}

	@Test public void tolerateV1Manifests() throws Exception {
		Editor editor = harness.newEditor(
				"---\n" +
				"name: my-redis-deployment\n" +
				"director_uuid: 1234abcd-5678-efab-9012-3456cdef7890\n" +
				"\n" +
				"releases:\n" +
				"- {name: redis, version: 12}\n" +
				"\n" +
				"resource_pools:\n" +
				"- name: redis-servers\n" +
				"  network: default\n" +
				"  stemcell:\n" +
				"    name: bosh-aws-xen-ubuntu-trusty-go_agent\n" +
				"    version: 2708\n" +
				"  cloud_properties:\n" +
				"    instance_type: m1.small\n" +
				"    availability_zone: us-east-1c\n" +
				"\n" +
				"disk_pools: []\n" +
				"\n" +
				"networks:\n" +
				"- name: default\n" +
				"  type: manual\n" +
				"  subnets:\n" +
				"  - range: 10.10.0.0/24\n" +
				"    gateway: 10.10.0.1\n" +
				"    static:\n" +
				"    - 10.10.0.16 - 10.10.0.18\n" +
				"    reserved:\n" +
				"    - 10.10.0.2 - 10.10.0.15\n" +
				"    dns: [10.10.0.6]\n" +
				"    cloud_properties:\n" +
				"      subnet: subnet-d597b993\n" +
				"\n" +
				"compilation:\n" +
				"  workers: 2\n" +
				"  network: default\n" +
				"  reuse_compilation_vms: true\n" +
				"  cloud_properties:\n" +
				"    instance_type: c1.medium\n" +
				"    availability_zone: us-east-1c\n" +
				"\n" +
				"update:\n" +
				"  canaries: 1\n" +
				"  max_in_flight: 3\n" +
				"  canary_watch_time: 15000-30000\n" +
				"  update_watch_time: 15000-300000\n" +
				"\n" +
				"jobs:\n" +
				"- name: redis-master\n" +
				"  instances: 1\n" +
				"  templates:\n" +
				"  - {name: redis-server, release: redis}\n" +
				"  persistent_disk: 10_240\n" +
				"  resource_pool: redis-servers\n" +
				"  networks:\n" +
				"  - name: default\n" +
				"\n" +
				"- name: redis-slave\n" +
				"  instances: 2\n" +
				"  templates:\n" +
				"  - {name: redis-server, release: redis}\n" +
				"  persistent_disk: 10_240\n" +
				"  resource_pool: redis-servers\n" +
				"  networks:\n" +
				"  - name: default\n" +
				"\n" +
				"properties:\n" +
				"  redis:\n" +
				"    max_connections: 10\n" +
				"\n" +
				"cloud_provider: {}"
		);
		editor.ignoreProblem(YamlSchemaProblems.DEPRECATED_PROPERTY);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(
				"name: foo\n" +
				"director_uuid: dca5480a-6b0e-11e7-907b-a6006ad3dba0\n" +
				"networks: {}" //This makes it a V1 schema
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"director_uuid|bosh v2 CLI no longer checks or requires",
				"networks|Deprecated: 'networks' is a V1 schema property"
		);
	}

	@Test public void documentSymbols() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"variables:\n" +
				"- name: blobstore_admin_users_password\n" +
				"  type: password\n" +
				"- name: blobstore_secure_link_secret\n" +
				"  type: password\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu-trusty\n" +
				"  version: '3421.11'\n" +
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  networks:\n" +
				"  - name: the-network\n" +
				"    static_ips: []\n" +
				"    default: []\n" +
				"- name: bar-group\n" +
				"  networks:\n" +
				"  - name: the-network\n" +
				"    static_ips: []\n" +
				"    default: []\n" +
				"releases:\n" +
				"- name: one-release\n" +
				"- name: other-release\n"
		);
		editor.assertDocumentSymbols(
				"default|StemcellAlias",
				"foo-group|InstanceGroup",
				"bar-group|InstanceGroup",
				"one-release|Release",
				"other-release|Release",
				"blobstore_admin_users_password|Variable",
				"blobstore_secure_link_secret|Variable"
		);
	}

	@Test public void duplicateSymbolChecking() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"variables:\n" +
				"- name: dup_var\n" +
				"  type: password\n" +
				"- name: blobstore_admin_users_password\n" +
				"  type: password\n" +
				"- name: blobstore_secure_link_secret\n" +
				"  type: password\n" +
				"- name: dup_var\n" +
				"  type: ssh\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu-trusty\n" +
				"  version: '3421.11'\n" +
				"- alias: dup_cell\n" +
				"  os: ubuntu-trusty\n" +
				"  version: '3421.11'\n" +
				"- alias: dup_cell\n" +
				"  os: ubuntu-trusty\n" +
				"  version: '3421.11'\n" +
				"instance_groups:\n" +
				"- name: foo-group\n" +
				"  networks:\n" +
				"  - name: the-network\n" +
				"    static_ips: []\n" +
				"    default: []\n" +
				"- name: bar-group\n" +
				"  networks:\n" +
				"  - name: the-network\n" +
				"    static_ips: []\n" +
				"    default: []\n" +
				"- name: bar-group\n" +
				"  networks:\n" +
				"  - name: the-network\n" +
				"    static_ips: []\n" +
				"    default: []\n" +
				"releases:\n" +
				"- name: one-release\n" +
				"- name: other-release\n" +
				"- name: one-release\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);

		editor.assertProblems(
				"dup_var|Duplicate 'VariableName'",
				"dup_var|Duplicate 'VariableName'",
				"dup_cell|Duplicate 'StemcellAlias'",
				"dup_cell|Duplicate 'StemcellAlias'",
				"bar-group|Duplicate 'InstanceGroupName'",
				"bar-group|Duplicate 'InstanceGroupName'",
				"one-release|Duplicate 'ReleaseName'",
				"one-release|Duplicate 'ReleaseName'"
		);
	}

	@Test public void contentAssistReleaseReference() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  jobs:\n" +
				"  - release: <*>\n" +
				"releases: \n" +
				"- name: some-release\n" +
				"  url: https://release-hub.info/some-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n" +
				"- name: other-release\n" +
				"  url: https://release-hub.info/other-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n"
		);
		editor.assertContextualCompletions("<*>"
				, // ==>
				"other-release<*>", "some-release<*>"
		);
	}

	@Test public void reconcileReleaseReference() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  jobs:\n" +
				"  - release: some-release\n" +
				"- name: some-other-server\n" +
				"  jobs:\n" +
				"  - release: bogus-release\n" +
				"releases: \n" +
				"- name: some-release\n" +
				"  url: https://release-hub.info/some-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n" +
				"- name: other-release\n" +
				"  url: https://release-hub.info/other-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"bogus-release|unknown 'ReleaseName'. Valid values are: [other-release, some-release]"
		);
	}

	@Test public void contentAssistStemcellReference() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  stemcell: <*>\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu\n" +
				"  version: 1346.77.1\n" +
				"- alias: windoze\n" +
				"  os: windows\n" +
				"  version: 678.9.1\n"
		);
		editor.assertContextualCompletions("<*>"
				, // ==>
				"default<*>", "windoze<*>"
		);
	}

	@Test public void reconcileStemcellReference() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  stemcell: default\n" +
				"- name: windoze-server\n" +
				"  stemcell: windoze\n" +
				"- name: bad-server\n" +
				"  stemcell: bogus-stemcell\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu\n" +
				"  version: 1346.77.1\n" +
				"- alias: windoze\n" +
				"  os: windows\n" +
				"  version: 678.9.1\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"bogus-stemcell|unknown 'StemcellAlias'. Valid values are: [default, windoze]"
		);
	}

	@Test public void contentAssistVMtype() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  stemcell: windoze\n" +
				"  vm_type: <*>"
		);
		editor.assertContextualCompletions(
				"<*>"
				, // ==>
				"default<*>",
				"large<*>"
		);

		//Verify that the cache is working. Shouldn't read the cc provier more than once, evem for multiple CA requests.
		editor.assertCompletionLabels("default", "large");
		editor.assertCompletionLabels("default", "large");
		assertEquals(1, cloudConfigProvider.getReadCount());
	}

	@Test public void reconcileVMtype() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  vm_type: bogus-vm\n" +
				"- name: other-server\n" +
				"  vm_type: large"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"bogus-vm|unknown 'VMType'. Valid values are: [default, large]"
		);
		assertEquals(1, cloudConfigProvider.getReadCount());
	}

	@Test public void reconcileVMTypeWhenCloudConfigUnavailable() throws Exception {
		cloudConfigProvider.readWith(() -> null);
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  vm_type: bogus-vm\n" +
				"- name: other-server\n" +
				"  vm_type: large"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(/*NONE*/); //Should not complain about unknown vm_types, if we can't determine what valid vm_types actually exist.
	}

	@Test public void reconcileVMTypeWhenCloudConfigThrows() throws Exception {
		cloudConfigProvider.readWith(() -> { throw new TimeoutException("Reading cloud config timed out"); });
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  vm_type: bogus-vm\n" +
				"- name: other-server\n" +
				"  vm_type: large"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(/*NONE*/); //Should not complain about unknown vm_types, if we can't determine what valid vm_types actually exist.
	}

	@Test public void contentAssistShowsWarningWhenCloudConfigThrows() throws Exception {
		cloudConfigProvider.readWith(() -> {
			throw new TimeoutException("Reading cloud config timed out");
		});
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  vm_type: <*>"
		);
		CompletionItem completion = editor.assertCompletionLabels("TimeoutException").get(0);
		completion = harness.resolveCompletionItem(completion);
		assertContains("Reading cloud config timed out", completion.getDocumentation());
	}

	@Test public void gotoReleaseDefinition() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  jobs:\n" +
				"  - release: some-release\n" +
				"- name: some-other-server\n" +
				"  jobs:\n" +
				"  - release: bogus-release\n" +
				"releases:\n" +
				"- name: some-release\n" +
				"  url: https://release-hub.info/some-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n" +
				"- name: other-release\n" +
				"  url: https://release-hub.info/other-release.tar.gz?version=99.3.2\n" +
				"  sha1: asddsfsd\n"
		);

		editor.assertGotoDefinition(editor.positionOf("some-release"),
			editor.rangeOf(
				"releases:\n" +
				"- name: some-release\n"
				,
				"some-release"
			)
		);
	}

	@Test public void gotoStemcellDefinition() throws Exception {
		Editor editor = harness.newEditor(
				"name: foo\n" +
				"instance_groups: \n" +
				"- name: some-server\n" +
				"  stemcell: default\n" +
				"- name: windoze-server\n" +
				"  stemcell: windoze\n" +
				"- name: bad-server\n" +
				"  stemcell: bogus-stemcell\n" +
				"stemcells:\n" +
				"- alias: default\n" +
				"  os: ubuntu\n" +
				"  version: 1346.77.1\n" +
				"- alias: windoze\n" +
				"  os: windows\n" +
				"  version: 678.9.1\n"
		);

		editor.assertGotoDefinition(
			editor.positionOf("stemcell: windoze", "windoze"),
			editor.rangeOf("- alias: windoze", "windoze")
		);
	}

}
