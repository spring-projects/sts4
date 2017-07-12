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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.bosh.BoshLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

import static org.springframework.ide.vscode.languageserver.testharness.Editor.*;

public class BoshEditorTest {

	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(() -> {
				return new BoshLanguageServer()
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
				"  vm_type: medium\n" + 
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
				"  vm_type: medium\n" + 
				"  stemcell: default\n" + 
				"  persistent_disk_type: medium\n" + 
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

	@Test public void toplevelV2PropertyCompletions() throws Exception {
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
				"variables:\n- <*>"
				, // ============
				"name: blah\n" +
				"director_uuid: <*>"
				, // ============
				"name: blah\n" +
				"properties:\n  <*>"
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
				"  version: <*>"
		);
	}
		
	@Test public void releasesBlockReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" + 
				"- name: some-release\n" +
				"  version: some-version\n" +
				"  woot: dunno\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"woot|Unknown property"
		);
		
		editor.assertHoverContains("name", "Name of a release used in the deployment");
		editor.assertHoverContains("version", "The version of the release to use");
		
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
				
				, // =============
				"instance_groups:\n" + 
				"- name: foo-group\n" +
				"  properties:\n" +
				"    <*>"
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

}
