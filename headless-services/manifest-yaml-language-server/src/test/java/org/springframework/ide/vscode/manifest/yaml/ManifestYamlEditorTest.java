/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.NoTargetsException;
import org.springframework.ide.vscode.languageserver.testharness.CodeAction;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

import com.google.common.collect.ImmutableList;

public class ManifestYamlEditorTest {

	LanguageServerHarness harness;
	MockCloudfoundry cloudfoundry = new MockCloudfoundry();

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(()-> new ManifestYamlLanguageServer(cloudfoundry.factory, cloudfoundry.paramsProvider));
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
		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
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
				"memory: 1G\n" +
				"aplications:\n" +
				"  - buildpack: zbuildpack\n" +
				"    domain: zdomain\n" +
				"    name: foo"
		);
		editor.assertProblems("aplications|Unknown property");

		//mispelled or not allowed at toplevel
		editor = harness.newEditor(
				"name: foo\n" +
				"buildpeck: yahah\n" +
				"memory: 1G\n" +
				"memori: 1G\n"
		);
		editor.assertProblems(
				"name|Unknown property",
				"buildpeck|Unknown property",
				"memori|Unknown property"
		);

		//mispelled or not allowed as nested
		editor = harness.newEditor(
				"applications:\n" +
				"- name: fine\n" +
				"  buildpeck: yahah\n" +
				"  memory: 1G\n" +
				"  memori: 1G\n" +
				"  applications: bad\n"
		);
		editor.assertProblems(
				"buildpeck|Unknown property",
				"memori|Unknown property",
				"applications|Unknown property"
		);
	}

	@Test
	public void reconcileStructuralProblems() throws Exception {
		Editor editor;

		//forgot the 'applications:' heading
		editor = harness.newEditor(
				"- name: foo"
		);
		editor.assertProblems(
				"- name: foo|Expecting a 'Map' but found a 'Sequence'"
		);

		//forgot to make the '-' after applications
		editor = harness.newEditor(
				"applications:\n" +
				"  name: foo"
		);
		editor.assertProblems(
				"name: foo|Expecting a 'Sequence' but found a 'Map'"
		);

		//Using a 'composite' element where a scalar type is expected
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"memory:\n"+
				"- bad sequence\n" +
				"buildpack:\n" +
				"  bad: map\n"
		);
		editor.assertProblems(
				"- bad sequence|Expecting a 'Memory' but found a 'Sequence'",
				"bad: map|Expecting a 'Buildpack' but found a 'Map'"
		);
	}

	@Test
	public void reconcileSimpleTypes() throws Exception {
		Editor editor;

		//check for 'format' errors:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: not a number\n" +
				"  no-route: notBool\n"+
				"  memory: 1024\n" +
				"  disk_quota: 2048\n" +
				"  health-check-type: unhealthy"
		);
		editor.assertProblems(
				"not a number|NumberFormatException",
				"notBool|boolean",
				"1024|doesn't end with a valid unit of memory",
				"2048|doesn't end with a valid unit of memory",
				"unhealthy|Health Check Type"
		);

		//check for 'range' errors:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: -3\n" +
				"  memory: -1024M\n" +
				"  disk_quota: -2048M\n"
		);
		editor.assertProblems(
				"-3|Value must be at least 1",
				"-1024M|Negative value is not allowed",
				"-2048M|Negative value is not allowed"
		);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: true\n"+
				"  memory: 1024M\n" +
				"  disk_quota: 2048MB\n"
		);
		editor.assertProblems(/*none*/);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: false\n" +
				"  memory: 1024m\n" +
				"  disk_quota: 2048mb\n"
		);
		editor.assertProblems(/*none*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  memory: 1G\n" +
				"  disk_quota: 2g\n"
		);
		editor.assertProblems(/*none*/);
	}

	@Test
	public void noListIndent() throws Exception {
		Editor editor;
		editor = harness.newEditor("appl<*>");
		editor.assertCompletions(
				"applications:\n"+
				"- name: <*>"
		);

		editor = harness.newEditor("serv<*>");
		editor.assertCompletions(
				"services:\n"+
				"- <*>"
		);
	}

	@Test
	public void toplevelCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor("<*>");
		editor.assertCompletions(
				"applications:\n"+
				"- name: <*>",
				// ---------------
				"buildpack: <*>",
				// ---------------
				"command: <*>",
				// ---------------
				"disk_quota: <*>",
				// ---------------
				"domain: <*>",
				// ---------------
				"domains:\n"+
				"- <*>",
				// ---------------
				"env:\n"+
				"  <*>",
				// ---------------
				"health-check-http-endpoint: <*>",
				// ---------------
				"health-check-type: <*>",
				// ---------------
//				"host: <*>",
				// ---------------
//				"hosts: \n"+
//				"  - <*>",
				// ---------------
				"inherit: <*>",
				// ---------------
				"instances: <*>",
				// ---------------
				"memory: <*>",
				// ---------------
//				"name: <*>",
				// ---------------
				"no-hostname: <*>",
				// ---------------
				"no-route: <*>",
				// ---------------
				"path: <*>",
				// ---------------
				"random-route: <*>",
				// ---------------
				"routes:\n"+
				"- route: <*>",
				// ---------------
				"services:\n"+
				"- <*>",
				// ---------------
				"stack: <*>",
				// ---------------
				"timeout: <*>"
		);

		editor = harness.newEditor("ranro<*>");
		editor.assertCompletions(
				"random-route: <*>"
		);
	}

	@Test
	public void nestedCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor(
				"applications:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				// ---------------
				"applications:\n" +
				"- buildpack: <*>",
				// ---------------
				"applications:\n" +
				"- command: <*>",
				// ---------------
				"applications:\n" +
				"- disk_quota: <*>",
				// ---------------
				"applications:\n" +
				"- domain: <*>",
				// ---------------
				"applications:\n" +
				"- domains:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- env:\n"+
				"    <*>",
				// ---------------
				"applications:\n" +
				"- health-check-http-endpoint: <*>",
				// ---------------
				"applications:\n" +
				"- health-check-type: <*>",
				// ---------------
				"applications:\n" +
				"- host: <*>",
				// ---------------
				"applications:\n" +
				"- hosts:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- instances: <*>",
				// ---------------
				"applications:\n" +
				"- memory: <*>",
				// ---------------
				"applications:\n" +
				"- name: <*>",
				// ---------------
				"applications:\n" +
				"- no-hostname: <*>",
				// ---------------
				"applications:\n" +
				"- no-route: <*>",
				// ---------------
				"applications:\n" +
				"- path: <*>",
				// ---------------
				"applications:\n" +
				"- random-route: <*>",
				// ---------------
				"applications:\n" +
				"- routes:\n"+
				"  - route: <*>",
				// ---------------
				"applications:\n" +
				"- services:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- stack: <*>",
				// ---------------
				"applications:\n" +
				"- timeout: <*>"
		);
	}

	@Test
	public void completionDetailsAndDocs() throws Exception {
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- build<*>"
		);
		editor.assertCompletionDetails("buildpack", "Buildpack", "If your application requires a custom buildpack");
	}

	@Test
	public void valueCompletions() throws Exception {
		assertCompletions("disk_quota: <*>",
				"disk_quota: 1024M<*>",
				"disk_quota: 256M<*>",
				"disk_quota: 512M<*>"
		);
		assertCompletions("memory: <*>",
				"memory: 1024M<*>",
				"memory: 256M<*>",
				"memory: 512M<*>"
		);
		assertCompletions("no-hostname: <*>",
				"no-hostname: false<*>",
				"no-hostname: true<*>"
		);
		assertCompletions("no-route: <*>",
				"no-route: false<*>",
				"no-route: true<*>"
		);
		assertCompletions("random-route: <*>",
				"random-route: false<*>",
				"random-route: true<*>"
		);

		assertCompletions("health-check-type: <*>",
				"health-check-type: http<*>",
//				"health-check-type: none<*>", Still valid, but not suggested because its deprecated
				"health-check-type: port<*>",
				"health-check-type: process<*>"
		);
	}

	@Test public void reconcileHealthCheckType() throws Exception {
		Editor editor;
		Diagnostic problem;

		editor = harness.newEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: none"
		);
		problem = editor.assertProblems("none|'none' is deprecated in favor of 'process'").get(0);
		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: port"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: process"
		);
		editor.assertProblems(/*NONE*/);
	}

	@Test public void reconcileHealthHttpEndPointIgnoredWarning() throws Exception {
		Editor editor;
		Diagnostic problem;

		editor = harness.newEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: process\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");

		editor = harness.newEditor(
				"health-check-type: http\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-http-endpoint: /health"
		);
		problem = editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `port`)").get(0);
		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems(/*NONE*/);

		editor = harness.newEditor(
				"health-check-type: http\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  health-check-type: process\n" +
				"  health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");

		editor = harness.newEditor(
				"health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `port`)");

		editor = harness.newEditor(
				"health-check-type: process\n" +
				"health-check-http-endpoint: /health"
		);
		editor.assertProblems("health-check-http-endpoint|This has no effect unless `health-check-type` is `http` (but it is currently set to `process`)");
	}

	@Test public void reconcileRoutesWithNoHost() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"applications:\n" +
				"- name: my-app\n" +
				"  no-hostname: true\n" +
				"  routes:\n" +
				"  - route: myapp.org"
		);
		editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
				"no-hostname|Property cannot co-exist with property 'routes'",
				"routes|Property cannot co-exist with property 'no-hostname'"
			);

		editor = harness.newEditor(
				"no-hostname: true\n" +
				"applications:\n" +
				"- name: my-app\n" +
				"  routes:\n" +
				"  - route: myapp.org"
		);
		editor.ignoreProblem("UnknownDomainProblem");

		editor.assertProblems(
				"no-hostname|Property cannot co-exist with property 'routes'",
				"routes|Property cannot co-exist with property 'no-hostname'"
			);

//		editor = harness.newEditor(
//				"no-hostname: true\n" +
//				"routes:\n" +
//				"- route: myapp.org" +
//				"applications:\n" +
//				"- name: my-app\n"
//		);
//		editor.ignoreProblem("UnknownDomainProblem");
//
//		editor.assertProblems(
//				"no-hostname|Property cannot co-exist with property 'routes'",
//				"routes|Property cannot co-exist with property 'no-hostname'"
//			);
	}

	@Test public void deprecatedHealthCheckTypeQuickfix() throws Exception {
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: none"
		);
		Diagnostic problem = editor.assertProblems("none|'none' is deprecated in favor of 'process'").get(0);
		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());
		CodeAction quickfix = editor.assertCodeAction(problem);
		assertEquals("Replace deprecated value 'none' by 'process'", quickfix.getLabel());
		quickfix.perform();

		editor.assertRawText(
				"applications:\n" +
				"- name: foo\n" +
				"  health-check-type: process"
		);
	}

	@Test
	public void hoverInfos() throws Exception {
		Editor editor = harness.newEditor(
			"memory: 1G\n" +
			"#comment\n" +
			"inherit: base-manifest.yml\n"+
			"applications:\n" +
			"- buildpack: zbuildpack\n" +
			"  domain: zdomain\n" +
			"  name: foo\n" +
			"  command: java main.java\n" +
			"  disk_quota: 1024M\n" +
			"  domains:\n" +
			"  - pivotal.io\n" +
			"  - otherdomain.org\n" +
			"  env:\n" +
			"    RAILS_ENV: production\n" +
			"    RACK_ENV: production\n" +
			"  host: apppage\n" +
			"  hosts:\n" +
			"  - apppage2\n" +
			"  - appage3\n" +
			"  instances: 2\n" +
			"  no-hostname: true\n" +
			"  no-route: true\n" +
			"  path: somepath/app.jar\n" +
			"  random-route: true\n" +
			"  routes:\n" +
			"  - route: tcp-example.com:1234\n" +
			"  services:\n" +
			"  - instance_ABC\n" +
			"  - instance_XYZ\n" +
			"  stack: cflinuxfs2\n" +
			"  timeout: 80\n" +
			"  health-check-type: none\n" +
			"  health-check-http-endpoint: /health\n"
		);

		editor.assertIsHoverRegion("memory");
		editor.assertIsHoverRegion("inherit");
		editor.assertIsHoverRegion("applications");
		editor.assertIsHoverRegion("buildpack");
		editor.assertIsHoverRegion("domain");
		editor.assertIsHoverRegion("name");
		editor.assertIsHoverRegion("command");
		editor.assertIsHoverRegion("disk_quota");
		editor.assertIsHoverRegion("domains");
		editor.assertIsHoverRegion("env");
		editor.assertIsHoverRegion("host");
		editor.assertIsHoverRegion("hosts");
		editor.assertIsHoverRegion("instances");
		editor.assertIsHoverRegion("no-hostname");
		editor.assertIsHoverRegion("no-route");
		editor.assertIsHoverRegion("path");
		editor.assertIsHoverRegion("random-route");
		editor.assertIsHoverRegion("routes");
		editor.assertIsHoverRegion("services");
		editor.assertIsHoverRegion("stack");
		editor.assertIsHoverRegion("timeout");
		editor.assertIsHoverRegion("health-check-type");

		editor.assertHoverContains("memory", "Use the `memory` attribute to specify the memory limit");
		editor.assertHoverContains("1G", "Use the `memory` attribute to specify the memory limit");
		editor.assertHoverContains("inherit", "For example, every child of a parent manifest called `base-manifest.yml` begins like this");
		editor.assertHoverContains("buildpack", "use the `buildpack` attribute to specify its URL or name");
	    editor.assertHoverContains("name", "The `name` attribute is the only required attribute for an application in a manifest file");
	    editor.assertHoverContains("command", "On the command line, use the `-c` option to specify the custom start command as the following example shows");
	    editor.assertHoverContains("disk_quota", "Use the `disk_quota` attribute to allocate the disk space for your app instance");
	    editor.assertHoverContains("domain", "You can use the `domain` attribute when you want your application to be served");
	    editor.assertHoverContains("domains", "Use the `domains` attribute to provide multiple domains");
	    editor.assertHoverContains("env", "The `env` block consists of a heading, then one or more environment variable/value pairs");
	    editor.assertHoverContains("host", "Use the `host` attribute to provide a hostname, or subdomain, in the form of a string");
	    editor.assertHoverContains("hosts", "Use the `hosts` attribute to provide multiple hostnames, or subdomains");
	    editor.assertHoverContains("instances", "Use the `instances` attribute to specify the number of app instances that you want to start upon push");
	    editor.assertHoverContains("no-hostname", "By default, if you do not provide a hostname, the URL for the app takes the form of `APP-NAME.DOMAIN`");
	    editor.assertHoverContains("no-route", "You can use the `no-route` attribute with a value of `true` to prevent a route from being created for your application");
	    editor.assertHoverContains("path", "You can use the `path` attribute to tell Cloud Foundry where to find your application");
	    editor.assertHoverContains("random-route", "Use the `random-route` attribute to create a URL that includes the app name and random words");
	    editor.assertHoverContains("routes", "Each route for this app is created if it does not already exist");
	    editor.assertHoverContains("services", "The `services` block consists of a heading, then one or more service instance names");
	    editor.assertHoverContains("stack", "Use the `stack` attribute to specify which stack to deploy your application to.");
	    editor.assertHoverContains("timeout", "The `timeout` attribute defines the number of seconds Cloud Foundry allocates for starting your application");
	    editor.assertHoverContains("health-check-type", "Use the `health-check-type` attribute to");
	    editor.assertHoverContains("health-check-http-endpoint", "customize the endpoint for the `http`");
	}

	@Test
	public void noHoverInfos() throws Exception {
		Editor editor = harness.newEditor(
		    "#comment\n" +
			"applications:\n" +
			"- buildpack: zbuildpack\n" +
			"  name: foo\n" +
			"  domains:\n" +
			"  - pivotal.io\n" +
			"  - otherdomain.org\n"

		);
		editor.assertNoHover("comment");

		// May fail in the future if hover support is added, but if hover support is added in the future,
		// it is expected that these should start to fail, as right now they have no hover
		editor.assertNoHover("pivotal.io");
		editor.assertNoHover("otherdomain.org");
	}

	@Test public void stacksCompletion() throws Exception {
		List<CFStack> stacks = ImmutableList.of(
				mockStack("linux"), mockStack("windows")
		);
		when(cloudfoundry.client.getStacks()).thenReturn(stacks);
		Editor editor = harness.newEditor(
				"stack: <*>"
		);
		CompletionItem c = editor.assertCompletions(
				"stack: linux<*>",
				"stack: windows<*>"
		).get(0);

		assertEquals("an-org : a-space [test.io]", c.getDocumentation());
	}


	@Test public void domainReconcile() throws Exception {
		List<CFDomain> domains = ImmutableList.of(mockDomain("one.com"), mockDomain("two.com"));
		when(cloudfoundry.client.getDomains()).thenReturn(domains);
		Editor editor;
		Diagnostic p;

		editor = harness.newEditor(
				"domain: bad.com"
		);
		p = editor.assertProblems("bad.com|unknown 'Domain'. Valid values are: [one.com, two.com]").get(0);
		assertEquals(DiagnosticSeverity.Warning, p.getSeverity());

		editor= harness.newEditor(
				"domains:\n" +
				"- one.com\n" +
				"- bad.com\n" +
				"- two.com"
		);
		editor.assertProblems("bad.com|unknown 'Domain'. Valid values are: [one.com, two.com]");
	}

	@Test public void stacksReconcile() throws Exception {
		List<CFStack> stacks = ImmutableList.of(
				mockStack("linux"), mockStack("windows")
		);
		when(cloudfoundry.client.getStacks()).thenReturn(stacks);
		{
			Editor editor = harness.newEditor(
					"stack: android<*>"
			);
			Diagnostic p = editor.assertProblems("android|'android' is an unknown 'Stack'. Valid values are: [linux, windows]").get(0);
			assertEquals(DiagnosticSeverity.Warning, p.getSeverity());
		}

		{
			Editor editor = harness.newEditor(
					"stack: <*>"
			);
			Diagnostic p = editor.assertProblems("|'Stack' cannot be blank").get(0);
			assertEquals(DiagnosticSeverity.Error, p.getSeverity());
		}

	}

	private CFStack mockStack(String name) {
		CFStack stack = Mockito.mock(CFStack.class);
		when(stack.getName()).thenReturn(name);
		return stack;
	}

	@Test
	public void reconcileDuplicateKeys() throws Exception {
		ImmutableList<CFDomain> domains = ImmutableList.of(mockDomain("pivotal.io"), mockDomain("otherdomain.org"));
		when(cloudfoundry.client.getDomains()).thenReturn(domains);
		Editor editor = harness.newEditor(
				"#comment\n" +
				"applications:\n" +
				"- buildpack: zbuildpack\n" +
				"  name: foo\n" +
				"  domains:\n" +
				"  - pivotal.io\n" +
				"  domains:\n" +
				"  - otherdomain.org\n"
		);
		editor.assertProblems(
				"domains|Duplicate key",
				"domains|Duplicate key"
		);
	}

	@Test public void PT_137299017_extra_space_with_completion() throws Exception {
		assertCompletions(
				"applications:\n" +
				"- name: foo\n" +
				"  random-route:<*>"
				, // ==>
				"applications:\n" +
				"- name: foo\n" +
				"  random-route: false<*>"
				, // --
				"applications:\n" +
				"- name: foo\n" +
				"  random-route: true<*>"
		);
	}

	@Test public void PT_137722057_extra_space_with_completion() throws Exception {
		assertCompletions(
				"applications:\n" +
				"-<*>",
				// ===>
				"applications:\n" +
				"- buildpack: <*>",
				// ---------------
				"applications:\n" +
				"- command: <*>",
				// ---------------
				"applications:\n" +
				"- disk_quota: <*>",
				// ---------------
				"applications:\n" +
				"- domain: <*>",
				// ---------------
				"applications:\n" +
				"- domains:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- env:\n"+
				"    <*>",
				// ---------------
				"applications:\n" +
				"- health-check-http-endpoint: <*>",
				// ---------------
				"applications:\n" +
				"- health-check-type: <*>",
				// ---------------
				"applications:\n" +
				"- host: <*>",
				// ---------------
				"applications:\n" +
				"- hosts:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- instances: <*>",
				// ---------------
				"applications:\n" +
				"- memory: <*>",
				// ---------------
				"applications:\n" +
				"- name: <*>",
				// ---------------
				"applications:\n" +
				"- no-hostname: <*>",
				// ---------------
				"applications:\n" +
				"- no-route: <*>",
				// ---------------
				"applications:\n" +
				"- path: <*>",
				// ---------------
				"applications:\n" +
				"- random-route: <*>",
				// ---------------
				"applications:\n" +
				"- routes:\n"+
				"  - route: <*>",
				// ---------------
				"applications:\n" +
				"- services:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- stack: <*>",
				// ---------------
				"applications:\n" +
				"- timeout: <*>"
		);

		//Second example
		assertCompletions(
				"applications:\n" +
				"-<*>\n" +
				"- name: test"
				, // ==>
				"applications:\n" +
				"- buildpack: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- command: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- disk_quota: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- domain: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- domains:\n" +
				"  - <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- env:\n" +
				"    <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- health-check-http-endpoint: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- health-check-type: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- host: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- hosts:\n" +
				"  - <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- instances: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- memory: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- name: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- no-hostname: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- no-route: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- path: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- random-route: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- routes:\n" +
				"  - route: <*>\n" +
				"- name: test"
				,// ---------------------
				"applications:\n" +
				"- services:\n" +
				"  - <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- stack: <*>\n" +
				"- name: test"
				, // ---------------------
				"applications:\n" +
				"- timeout: <*>\n" +
				"- name: test"
		);

	}

	@Test public void numberOfYamlDocumentsShouldBeExactlyOne() throws Exception {
		Editor editor;

		{
			//when the file is empty (there is no AST at all)
			editor = harness.newEditor("#Emptyfile");
			editor.assertProblems("#Emptyfile|'Cloudfoundry Manifest' must have at least some Yaml content");
		}

		{
			//when the file has too many documents... then highlight the '---' marker introducing the first document
			//exceeding the range.
			editor = harness.newEditor(
					"---\n" +
					"applications:\n"+
					"- name: foo\n" +
					"  bad-one: xx\n" +
					"---\n" +
					"applications:\n"+
					"- name: foo\n" +
					"  bad-two: xx"
			);
			editor.assertProblems(
				"bad-one|Unknown property", //should still reconcile the documents even thought there's too many of them!
				"---|'Cloudfoundry Manifest' should not have more than 1 Yaml Document",
				"bad-two|Unknown property" //should still reconcile the documents even thought there's too many of them!
			);
			//also check the location of the marker since there are two occurrences of '---' in the editor text.
			Diagnostic problem = editor.assertProblem("---");
			assertTrue(problem.getRange().getStart().getLine()>1);
		}

		{
			// Also check that looking for the '---' isn't confused by extra whitespace
			editor = harness.newEditor(
					"---\n" +
					"applications:\n"+
					"- name: foo\n" +
					"  \n"+
					"---\n" +
					"   \n"+
					"applications:\n"+
					"- name: foo\n"
			);
			editor.assertProblems(
				"---|'Cloudfoundry Manifest' should not have more than 1 Yaml Document"
			);
			//also check the location of the marker since there are two occurrences of '---' in the editor text.
			Diagnostic problem = editor.assertProblem("---");
			assertTrue(problem.getRange().getStart().getLine()>1);
		}

	}

	@Test public void namePropertyIsRequired() throws Exception {
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: this-is-good\n" +
				"- memory: 1G\n" +
				"- name:\n"
		);
		editor.assertProblems(
				"-^ memory: 1G|Property 'name' is required",
				"|should not be empty"
		);
	}

	@Test
	public void noReconcileErrorsWhenCFFactoryThrows() throws Exception {
		reset(cloudfoundry.factory);
		when(cloudfoundry.factory.getClient(any(), any())).thenThrow(new IOException("Can't create a client!"));
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  buildpack: bad-buildpack\n" +
				"  services:\n" +
				"  - bad-service\n" +
				"  bogus: bad" //a token error to make sure reconciler is actually running!
		);
		editor.assertProblems("bogus|Unknown property");
	}

	@Test
	public void noReconcileErrorsWhenClientThrows() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		when(cfClient.getBuildpacks()).thenThrow(new IOException("Can't get buildpacks"));
		when(cfClient.getServices()).thenThrow(new IOException("Can't get services"));
		when(cfClient.getStacks()).thenThrow(new IOException("Can't get stacks"));
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  buildpack: bad-buildpack\n" +
				"  stack: bad-stack\n" +
				"  services:\n" +
				"  - bad-service\n" +
				"  bogus: bad" //a token error to make sure reconciler is actually running!
		);
		editor.assertProblems("bogus|Unknown property");
	}

	@Test
	public void reconcileCFService() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFServiceInstance service = Mockito.mock(CFServiceInstance.class);
		when(service.getName()).thenReturn("myservice");
		when(cfClient.getServices()).thenReturn(ImmutableList.of(service));
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services:\n" +
				"  - myservice\n"

		);
		// Should have no problems
		editor.assertProblems(/*none*/);
	}

	@Test
	public void reconcileShowsWarningOnUnknownService() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFServiceInstance service = Mockito.mock(CFServiceInstance.class);
		when(service.getName()).thenReturn("myservice");
		when(cfClient.getServices()).thenReturn(ImmutableList.of(service));
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services:\n" +
				"  - bad-service\n"

		);
		editor.assertProblems("bad-service|There is no service instance called");

		Diagnostic problem = editor.assertProblem("bad-service");

		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());
	}

	@Test
	public void reconcileShowsWarningOnNoService() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		when(cfClient.getServices()).thenReturn(ImmutableList.of());
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services:\n" +
				"  - bad-service\n");
		editor.assertProblems("bad-service|There is no service instance called");

		Diagnostic problem = editor.assertProblem("bad-service");

		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());
	}

	@Test
	public void servicesContentAssistShowErrorMessageWhenNotLoggedIn() throws Exception {
		reset(cloudfoundry.paramsProvider);

		when(cloudfoundry.paramsProvider.getParams()).thenThrow(new NoTargetsException("No Cloudfoundry Targets: Please login"));

		String textBefore =
				"applications:\n" +
				"- name: foo\n" +
				"  services:\n" +
				"  - <*>";
		Editor editor = harness.newEditor(
				textBefore
		);

		//Applying the single completion should do nothing in the editor:
		editor.assertCompletions(textBefore);

		//The message from the exception should appear in the 'doc string':
		editor.assertCompletionDetails("No Cloudfoundry Targets", "Error", "Please login");

	}

	@Test
	public void servicesContentAssistShowErrorMessageWhenNotLoggedIn_nonEmptyQueryString() throws Exception {
		reset(cloudfoundry.paramsProvider);

		when(cloudfoundry.paramsProvider.getParams()).thenThrow(new NoTargetsException("No Cloudfoundry Targets: Please login"));

		String textBefore =
				"applications:\n" +
				"- name: foo\n" +
				"  services:\n" +
				"  - something<*>";
		Editor editor = harness.newEditor(
				textBefore
		);

		//Applying the single completion should do nothing in the editor:
		editor.assertCompletions(textBefore);

		//The message from the exception should appear in the 'doc string':
		CompletionItem completion = editor.assertCompletionDetails("No Cloudfoundry Targets", "Error", "Please login");
		//query string should match the 'filter text' otherwise vscode will filter the item and it will be gone!
		assertEquals("something", completion.getFilterText());
	}

	@Test
	public void serviceContentAssist() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFServiceInstance service = Mockito.mock(CFServiceInstance.class);
		when(service.getName()).thenReturn("mysql");
		when(service.getPlan()).thenReturn("medium");
		when(cfClient.getServices()).thenReturn(ImmutableList.of(service));

		CompletionItem completion = assertCompletions(
				"services:\n" +
				"  - <*>"
				, // ==>
				"services:\n" +
				"  - mysql<*>"
		).get(0);
		assertEquals("mysql - medium", completion.getLabel());
		assertEquals("an-org : a-space [test.io]", completion.getDocumentation());
	}

	@Test
	public void buildpackContentAssist() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFBuildpack buildPack = Mockito.mock(CFBuildpack.class);
		when(buildPack.getName()).thenReturn("java_buildpack");
		when(cfClient.getBuildpacks()).thenReturn(ImmutableList.of(buildPack));

		CompletionItem completion = assertCompletions("buildpack: <*>", "buildpack: java_buildpack<*>").get(0);
		assertEquals("java_buildpack", completion.getLabel());
		assertEquals("an-org : a-space [test.io]", completion.getDocumentation());
	}

	@Test
	public void domainContentAssist() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFDomain domain = Mockito.mock(CFDomain.class);
		when(domain.getName()).thenReturn("cfapps.io");
		when(cfClient.getDomains()).thenReturn(ImmutableList.of(domain));

		CompletionItem completion = assertCompletions("domain: <*>", "domain: cfapps.io<*>").get(0);
		assertEquals("an-org : a-space [test.io]", completion.getDocumentation());
	}

	@Test
	public void domainsContentAssist() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;

		CFDomain domain = Mockito.mock(CFDomain.class);
		when(domain.getName()).thenReturn("cfapps.io");
		when(cfClient.getDomains()).thenReturn(ImmutableList.of(domain));

		CompletionItem completion = assertCompletions(
				"domains:\n" +
				"- <*>"
				, // ===>
				"domains:\n" +
				"- cfapps.io<*>"
		).get(0);

		assertEquals("an-org : a-space [test.io]", completion.getDocumentation());
	}

	@Test
	public void reconcileRouteFormat() throws Exception {
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: http://springsource.org\n");
		editor.assertProblems("http://springsource.org|is not a valid 'Route'");
		Diagnostic problem = editor.assertProblem("http://springsource.org");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: spring source.org\n");
		editor.assertProblems("spring source.org|is not a valid 'Route'");
		problem = editor.assertProblem("spring source.org");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: springsource.org:kuku\n");
		editor.assertProblems("springsource.org:kuku|is not a valid 'Route'");
		problem = editor.assertProblem("springsource.org:kuku");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());


		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: springsource.org/kuku?p=23\n");
		editor.assertProblems("springsource.org/kuku?p=23|is not a valid 'Route'");
		problem = editor.assertProblem("springsource.org/kuku?p=23");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: springsource.org:645788\n");
		editor.assertProblems("springsource.org:645788|is not a valid 'Route'");
		problem = editor.assertProblem("springsource.org:645788");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());
	}

	@Test
	public void reconcileRoute_Advanced() throws Exception {
		ImmutableList<CFDomain> domains = ImmutableList.of(mockDomain("somedomain.com"));
		when(cloudfoundry.client.getDomains()).thenReturn(domains);
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: springsource.org:8765/path\n");
		editor.assertProblems("springsource.org:8765/path|Unable to determine type of route");
		Diagnostic problem = editor.assertProblem("springsource.org:8765/path");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: host.springsource.org:66000\n");
		editor.assertProblems("66000|Invalid port");
		problem = editor.assertProblem("66000");
		assertEquals(DiagnosticSeverity.Error, problem.getSeverity());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: host.springsource.org\n");
		editor.assertProblems("springsource.org|Unknown 'Domain'. Valid domains are: [somedomain.com]");
		problem = editor.assertProblem("springsource.org");
		assertEquals(DiagnosticSeverity.Warning, problem.getSeverity());
	}

	private CFDomain mockDomain(String name) {
		CFDomain domain = mock(CFDomain.class);
		when(domain.getName()).thenReturn(name);
		return domain;
	}

	@Test
	public void reconcileRouteValidDomain() throws Exception {
		ClientRequests cfClient = cloudfoundry.client;
		CFDomain domain = Mockito.mock(CFDomain.class);
		when(domain.getName()).thenReturn("springsource.org");
		when(cfClient.getDomains()).thenReturn(ImmutableList.of(domain));
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n" +
				"  - route: host.springsource.org\n");
		editor.assertProblems();
	}

	@Test public void dashedContentAssistForServices() throws Exception {
		Editor editor;
		CFServiceInstance service = mock(CFServiceInstance.class);
		when(cloudfoundry.client.getServices()).thenReturn(ImmutableList.of(service));
		when(service.getName()).thenReturn("my-service");
		when(service.getPlan()).thenReturn("cheap-plan");

		editor = harness.newEditor(
				"services:\n"+
				"- blah\n"+
				"ser<*>"
		);
		editor.assertCompletionWithLabel((s) -> s.startsWith("- "),
				"services:\n"+
				"- blah\n"+
				"- my-service<*>"
		);

		editor = harness.newEditor(
				"services:\n"+
				"- blah\n"+
				"<*>"
		);
		editor.assertCompletionWithLabel((s) -> s.startsWith("- "),
				"services:\n"+
				"- blah\n"+
				"- my-service<*>"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services:<*>\n"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: foo\n" +
				"  services: \n"+
				"  - my-service<*>\n"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services: <*>\n"
		);
		editor.assertCompletionLabels("- my-service - cheap-plan");
		editor.assertCompletions(
				"applications:\n" +
				"- name: foo\n" +
				"  services: \n" +
				"  - my-service<*>\n"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services: ser<*>\n"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: foo\n" +
				"  services: \n" +
				"  - my-service<*>\n"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  services: \n" +
				"  - blah\n" +
				"  <*>\n"
		);
		editor.assertCompletionWithLabel((s) -> s.startsWith("- "),
				"applications:\n" +
				"- name: foo\n" +
				"  services: \n" +
				"  - blah\n" +
				"  - my-service<*>\n"
		);
	}

	@Test public void noRelaxedValueCompletionsInListItemContexts() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/144393355

		CFServiceInstance service = mock(CFServiceInstance.class);
		when(cloudfoundry.client.getServices()).thenReturn(ImmutableList.of(service));
		when(service.getName()).thenReturn("my-service");
		when(service.getPlan()).thenReturn("cheap-plan");

		Editor editor = harness.newEditor(
				"services:\n" +
				"- some-service\n" +
				"<*>"
		);
		editor.assertCompletions((c) -> c.getLabel().contains("my-service"),
				"services:\n" +
				"- some-service\n" +
				"- my-service<*>"
		);
	}

	@Test public void autoInsertRouteProperty() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  ro<*>"
		);
		editor.assertCompletions(c -> c.getLabel().startsWith("routes"),
				"applications:\n" +
				"- name: foo\n" +
				"  routes:\n"+
				"  - route: <*>"
		);
	}

	@Test public void contentAssistInsideRouteDomain() throws Exception {
		Editor editor;

		ImmutableList<CFDomain> domains = ImmutableList.of(
				mockDomain("cfapps.io"),
				mockDomain("dsyer.com")
		);
		when(cloudfoundry.client.getDomains()).thenReturn(domains);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route:<*>"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: cfapps.io<*>"
				, // ==============
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: dsyer.com<*>"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: <*>"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: cfapps.io<*>"
				, // ==============
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: dsyer.com<*>"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: dsyer.<*>"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: dsyer.com<*>"
				, // ==============
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: dsyer.cfapps.io<*>"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.<*>"
		);
		CompletionItem c = editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.cfapps.io<*>"
				, // ==============
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.dsyer.com<*>"
		).get(0);
		assertEquals("cfapps.io", c.getLabel());

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.<*>"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.cfapps.io<*>"
				, // ==============
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.dsyer.com<*>"
		);

		/// no content assist inside of path or port section of route:

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.com/<*>"
		);
		editor.assertCompletions(/*NONE*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.com:<*>"
		);
		editor.assertCompletions(/*NONE*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: foo.bar.com:7777/blah<*>"
		);
		editor.assertCompletions(/*NONE*/);

		// Martin's most fancy example:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.cf<*>pps.io/superpath\n" +
				"  memory: 1024M\n"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.cfapps.io<*>/superpath\n" +
				"  memory: 1024M\n"
		);

		//Kris's variants of Martin's example:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.ds<*>pps.io/superpath\n" +
				"  memory: 1024M\n"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.dsyer.com<*>/superpath\n" +
				"  memory: 1024M\n"
		);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.ds<*>pps.io:8888/superpath\n" +
				"  memory: 1024M\n"
		);
		editor.assertCompletions(
				"applications:\n" +
				"- name: test\n" +
				"  routes:\n" +
				"  - route: test.dsyer.com<*>:8888/superpath\n" +
				"  memory: 1024M\n"
		);

	}



	//////////////////////////////////////////////////////////////////////////////

	private List<CompletionItem> assertCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		return editor.assertCompletions(textAfter);
	}

}
