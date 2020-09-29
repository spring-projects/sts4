/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.yaml;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Tests for parsing YAML deployment manifest into
 * {@link CloudApplicationDeploymentProperties}
 *
 * @author Alex Boyko
 *
 */
public class Yaml2DeploymentPropertiesTest {

	private static CloudApplicationDeploymentProperties readDeploymentPropertiesFile(final String filePath) throws Exception {
		ApplicationManifestHandler handler = new ApplicationManifestHandler(null, ManifestCompareMergeTests.createCloudData()) {
			@Override
			protected InputStream getInputStream() throws Exception {
				return new FileInputStream(ManifestCompareMergeTests.getTestFile(filePath));
			}
		};
		return handler.load(new NullProgressMonitor()).get(0);
	}

	@Test
	public void test_health_check_process() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/health-check-process.yml");
		assertEquals("Health check types not equal", "process", props.getHealthCheckType());
	}

	@Test
	public void test_health_check_port() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/health-check-port.yml");
		assertEquals("Health check types not equal", "port", props.getHealthCheckType());
	}

	@Test
	public void test_health_check_http() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/health-check-http.yml");
		assertEquals("Health check types not equal", "http", props.getHealthCheckType());
		assertEquals("Health check http endpoints not equal", "/testhealth", props.getHealthCheckHttpEndpoint());
	}

	@Test
	public void test_no_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-route-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-route-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-route-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-route-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-route-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/no-hostname-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("springsource.org", "spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/random-route-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/random-route-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/random-route-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		assertEquals(1, uris.size());
		String uri = uris.iterator().next();
		String host = uri.substring(0, uri.indexOf('.'));
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/random-route-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		assertEquals(1, uris.size());
		String uri = uris.iterator().next();
		String host = uri.substring(0, uri.indexOf('.'));
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/random-route-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		Set<String> expected = ImmutableSet.of("spring.io", "spring.framework");
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.spring.framework", "my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.what"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_7() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-7.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_8() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-8.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_9() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/domains-9.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org", "my-app-3.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/hosts-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org",
				"my-app-3.springsource.org", "my-root-2.springsource.org", "my-root-3.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_uris_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/uris-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org",
				"my-app-3.springsource.org", "my-app-1.spring.io", "my-app-2.spring.io", "my-app-3.spring.io",
				"my-app-1.spring.framework", "my-app-2.spring.framework", "my-app-3.spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_root_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/root-1.yml");
		assertEquals("app", props.getAppName());
		assertEquals(1024, props.getMemory());
		assertEquals(new HashSet<>(Arrays.asList("app.spring.io")), props.getUris());
	}

	@Test
	public void test_command_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/command-1.yml");
		assertEquals("mycommand", props.getCommand());
	}

	@Test
	public void test_stack_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/stack-1.yml");
		assertEquals("stack1", props.getStack());
	}

	@Test
	public void test_memory_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-1.yml");
		assertEquals("Uris sets not equal", DeploymentProperties.DEFAULT_MEMORY, props.getMemory());
	}

	@Test
	public void test_memory_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-2.yml");
		assertEquals("Uris sets not equal", 768, props.getMemory());
	}

	@Test
	public void test_memory_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-3.yml");
		assertEquals("Uris sets not equal", 768, props.getMemory());
	}

	@Test
	public void test_memory_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-4.yml");
		assertEquals("Uris sets not equal", 768, props.getMemory());
	}

	@Test
	public void test_memory_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-5.yml");
		assertEquals("Uris sets not equal", 1024, props.getMemory());
	}

	@Test
	public void test_memory_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-6.yml");
		assertEquals("Uris sets not equal", 1024, props.getMemory());
	}

	@Test(expected=CoreException.class)
	public void test_memory_7() throws Exception {
		readDeploymentPropertiesFile("manifest-parse-data/memory-7.yml");
	}

	@Test(expected=CoreException.class)
	public void test_memory_8() throws Exception {
		readDeploymentPropertiesFile("manifest-parse-data/memory-8.yml");
	}

	@Test
	public void test_memory_9() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-9.yml");
		assertEquals("Uris sets not equal", 3072, props.getMemory());
	}

	@Test
	public void test_memory_10() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-10.yml");
		assertEquals("Uris sets not equal", 4096, props.getMemory());
	}

	@Test
	public void test_memory_11() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile("manifest-parse-data/memory-11.yml");
		assertEquals("Uris sets not equal", 1500, props.getMemory());
	}

	@Test
	public void test_routes_no_route() throws Exception {
		// Manifest has a route but if "no-route" is also present, test that
		// list of URI is empty in the props
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-generate-data/routes-no-route.yml");

		assertEquals("Routes and URIs not equal", ImmutableList.of(), ImmutableList.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-1.yml");

		assertEquals("Routes not equal", ImmutableSet.of("my-route.springsource.org"),  ImmutableSet.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-2.yml");

		assertEquals("Routes not equal", ImmutableSet.of("my-route-1.springsource.org", "my-route-2.springsource.org"),  ImmutableSet.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-3.yml");

		assertEquals("Routes not equal", ImmutableSet.of(), ImmutableSet.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-4.yml");

		assertEquals("Routes not equal", ImmutableSet.of("my-route-1.springsource.org", "my-route-2.springsource.org"),  ImmutableSet.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-5.yml");

		assertEquals("Routes not equal", ImmutableSet.of("my-route-1.springsource.org", "my-route-2.springsource.org",
				"my-root-route-1.springsource.org"), ImmutableSet.copyOf(props.getUris()));
	}

	@Test
	public void test_routes_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentPropertiesFile(
				"manifest-parse-data/routes-6.yml");

		assertEquals("Routes not equal", ImmutableSet.of("my-route-1.invaliddomain.org", "my-route-2.springsource.org"), ImmutableSet.copyOf(props.getUris()));
	}
}
