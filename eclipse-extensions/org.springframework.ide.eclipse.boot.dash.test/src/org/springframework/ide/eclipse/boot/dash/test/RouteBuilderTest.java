/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.routes.RouteAttributes;
import org.springframework.ide.eclipse.boot.dash.cf.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.cf.routes.RouteBuilder;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlTraversal;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

public class RouteBuilderTest {

	List<CFCloudDomain> TEST_DOMAINS = ImmutableList.of(
			new CFCloudDomainData("tcp.cfapps.io", CFDomainType.TCP, CFDomainStatus.SHARED),
			new CFCloudDomainData("testing.me", CFDomainType.HTTP, CFDomainStatus.OWNED),
			new CFCloudDomainData("cfapps.io")
	);

	@Test
	public void appNameOnly() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n"
				, //=>
				"my-app@cfapps.io"
		);
	}

	@Test
	public void domainAndHost() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domain: testing.me\n" +
				"  host: some-host"
				, //=>
				"some-host@testing.me"
		);
	}

	@Test
	public void domainOnly() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domain: testing.me\n"
				, //=>
				"my-app@testing.me"
		);
	}

	@Test
	public void domainAndNoHost() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domain: testing.me\n" +
				"  no-hostname: true"
				, //=>
				"testing.me"
		);
	}

	@Test
	public void noHostOnly() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  no-hostname: true"
				, //=>
				"cfapps.io"
		);
	}

	@Test public void noRoute() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: foo\n" +
				"  no-route: true"
				// ==>
				/* NONE */
		);
	}

	@Test
	public void multipleDomainsAndHosts() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  domains: [testing.me, cfapps.io]\n" +
				"  hosts: [foo, bar]\n"
				, // ==>
				"foo@testing.me",
				"foo@cfapps.io",
				"bar@testing.me",
				"bar@cfapps.io"
		);
	}

	@Test
	public void randomRouteNoDomain() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  random-route: true\n"
				,
				"?@cfapps.io"
		);
	}

	@Test
	public void randomRouteHttpDomain() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  random-route: true\n" +
				"  domain: testing.me\n"
				,
				"?@testing.me"
		);
	}
	@Test
	public void randomRouteTcpDomain() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  random-route: true\n" +
				"  domain: tcp.cfapps.io\n"
				,
				"tcp.cfapps.io:?"
		);
	}


	@Test
	public void variousUriRoutes() throws Exception {
		assertRoutes(
				"applications:\n" +
				"- name: my-app\n" +
				"  routes:\n" +
				"  - route: foo.cfapps.io/somepath\n" +
				"  - route: cfapps.io\n" +
				"  - route: tcp.cfapps.io:63111\n" +
				"  - route: bar.testing.me\n" +
				"  - route: ambiguous.tcp.cfapps.io\n"
				, // =>
				"foo@cfapps.io/somepath",
				"cfapps.io",
				"tcp.cfapps.io:63111",
				"bar@testing.me",
				"ambiguous@tcp.cfapps.io"
		);
	}

	@Test
	public void noCuttingInTheMiddleOfAWord() throws Exception {
		{
			List<CFCloudDomain> domains = ImmutableList.of(
					new CFCloudDomainData("irrellevant.com"),
					new CFCloudDomainData("thing.com"),
					new CFCloudDomainData("something.com")
			);
			assertRoutes(domains,
					"applications:\n" +
					"- name: my-app\n" +
					"  routes:\n" +
					"  - route: foo.something.com\n"
					, // ==>
					"foo@something.com"
			);
		}
		{
			List<CFCloudDomain> domains = ImmutableList.of(
					new CFCloudDomainData("irrellevant.com"),
					new CFCloudDomainData("thing.com"),
					new CFCloudDomainData("com")
			);
			assertRoutes(domains,
					"applications:\n" +
					"- name: my-app\n" +
					"  routes:\n" +
					"  - route: foo.something.com\n"
					, // ==>
					"foo.something@com"
			);
		}
	}

	/////// Some edge cases below. Their behavior isn't really that important, but the tests
	// just here to formally cover the corresponding branches in the code, and make sure
	// they don't behave irrationally or crash. These tests are probably 'over-specified' in
	// that they test behaviors client code shouldn't have to / want to care about.

	@Test
	public void noKnownDomains() throws Exception {
		assertRoutes(ImmutableList.of(),
				"applications:\n" +
				"- name: my-app\n" +
				"  routes:\n" +
				"  - route: some.thing.com\n"
				// =>
				/* NONE */
		);
		assertRoutes(ImmutableList.of(),
				"applications:\n" +
				"- name: my-app\n"
				// =>
				/* NONE */
		);
	}

	@Test
	public void bogusDomain() throws Exception {
		assertRoutes(ImmutableList.of(),
				"applications:\n" +
				"- name: my-app\n" +
				"  random-route: true\n" +
				"  domain: bogus.com\n" +
				"  domains: [ bogus.me, cfapps.io]\n"
				, // =>

				//Rationale for the below results. When determining random-route behavior
				// for unknown domain assume its http, i.e. the most common case.
				"?@bogus.me" ,
				"?@cfapps.io",
				"?@bogus.com"
		);
	}

	////////////////////////////////////////////////////////////////////////////////////
	private void assertRoutes(String manifestText, String... expectedRoutes) {
		assertRoutes(TEST_DOMAINS, manifestText, expectedRoutes);
	}

	private void assertRoutes(List<CFCloudDomain> domains, String manifestText, String... expectedRoutes) {
		//Why the loop? This improves test coverage. The test should pass the same repeatedly, its behavior
		// should not change. It *might* change if we have some kind of bug where cached state influences the
		// behavior inadvertently in a manner visible to a caller.
		for (int i = 0; i < 3; i++) {
			RouteBuilder rb = new RouteBuilder(domains);
			List<RouteBinding> routes = rb.buildRoutes(parse(domains,
				manifestText));
			StringBuilder expected = new StringBuilder();
			StringBuilder expectedUri = new StringBuilder();
			for (String r : expectedRoutes) {
				expected.append(r+"\n");
				expectedUri.append(r.replace("@", "."));
			}
			StringBuilder actual = new StringBuilder();
			StringBuilder actualUri = new StringBuilder();
			for (RouteBinding r : routes) {
				actual.append(r+"\n");
				actualUri.append(r.toUri());
			}
			assertEquals(expected.toString(), actual.toString());
			assertEquals(expectedUri.toString(), actualUri.toString());
		}
	}

	/**
	 * Parse a manifest with a single
	 * @param manifestText
	 * @return
	 */
	private RouteAttributes parse(List<CFCloudDomain> domains, String manifestText) {
		IDocument doc = new Document(manifestText);
		YamlASTProvider parser = new YamlASTProvider(new Yaml());
		YamlFileAST ast = parser.getAST(doc);
		List<Node> names = YamlPath.EMPTY
				.thenAnyChild()
				.thenValAt("applications")
				.thenAnyChild()
				.thenValAt("name")
				.traverseAmbiguously(ast)
				.collect(Collectors.toList());
		assertEquals("Number of apps in manifest", 1, names.size());
		String appName = NodeUtil.asScalar(names.get(0));
		CloudData cloudData = new CloudData(domains, "some-buildpack", ImmutableList.of());
		YamlGraphDeploymentProperties dp = new YamlGraphDeploymentProperties(manifestText, appName, cloudData);
		return new RouteAttributes(dp);
	}

}
