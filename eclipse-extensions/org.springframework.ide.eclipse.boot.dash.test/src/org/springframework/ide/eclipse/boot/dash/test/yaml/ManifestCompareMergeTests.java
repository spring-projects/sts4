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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudData;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.test.AllBootDashTests;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import com.google.common.collect.ImmutableList;

/**
 * Manifest YAML file and Deployment properties compare and merge tests.
 *
 * @author Alex Boyko
 *
 */
public class ManifestCompareMergeTests {

	public static final String DEFAULT_BUILDPACK = "java_buildpack_offline";

	public static final List<CFCloudDomain> SPRING_CLOUD_DOMAINS = Arrays.<CFCloudDomain>asList(
			new CFCloudDomainData("springsource.org"),
			new CFCloudDomainData("spring.io"),
			new CFCloudDomainData("spring.framework"));

	public static final List<CFStack> SPRING_CLOUD_STACKS = Arrays.asList(new CFStack[] { new CFStack() {
		@Override
		public String getName() {
			return "stack1";
		}
	}});

	public static CloudData createCloudData() {
		return new CloudData(SPRING_CLOUD_DOMAINS, DEFAULT_BUILDPACK, SPRING_CLOUD_STACKS);
	}

	private static void performMergeTest(File manifest, DeploymentProperties props, File expected) throws Exception {
		String yamlContents = IOUtil.toString(new FileInputStream(manifest));
		String expectText = expected == null ? null : IOUtil.toString(new FileInputStream(expected));
		//Note: You don't need to close the FileInputStreams because IOUtil does that already.
		performMergeTest(props, yamlContents, expectText);
	}

	private static void performMergeTest(DeploymentProperties props, String manifest, String expectText) throws Exception {
		YamlGraphDeploymentProperties yamlGraphProps = new YamlGraphDeploymentProperties(manifest, props.getAppName(), createCloudData());
		TextEdit edit = yamlGraphProps.getDifferences(props);
		if (expectText == null) {
			assertEquals(null, edit);
		} else {
			Document doc = new Document(manifest);
			if (edit!=null) {
				edit.apply(doc);
			}
			assertEquals(expectText.trim(), doc.get().trim());
		}
	}

	public static File getTestFile(String path) throws IOException {
		Bundle bundle = Platform.getBundle(AllBootDashTests.PLUGIN_ID);
		File bundleFile = FileLocator.getBundleFile(bundle);
		Assert.assertNotNull(bundleFile);
		Assert.assertTrue("The bundle "+bundle.getBundleId()+" must be unpacked to allow using the embedded test resources", bundleFile.isDirectory());
		return new File(bundleFile, path);
	}

	@Test
	public void anchorReferenceAndExtend() throws Exception {
		String manifest =
				"defaults: &defaults\n" +
				"  services:\n" +
				"  - my-rabbit\n" +
				"  memory: 1G\n" +
				"  instances: 3\n" +
				"applications:\n" +
				"- name: chatter-web-ui\n" +
				"  <<: *defaults\n" +
				"  buildpack: java_buildpack";

		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("chatter-web-ui");
		props.setServices(ImmutableList.of("my-rabbit"));
		props.setMemory(1024);
		props.setInstances(3);
		props.setMemory(1024);
		props.setBuildpack("java_buildpack");
		props.setUris(ImmutableList.of("chatter-web-ui.springsource.org"));
		performMergeTest(props, manifest, manifest);

		props.setInstances(2);
		performMergeTest(props, manifest,
				"defaults: &defaults\n" +
				"  services:\n" +
				"  - my-rabbit\n" +
				"  memory: 1G\n" +
				"  instances: 2\n" +
				"applications:\n" +
				"- name: chatter-web-ui\n" +
				"  <<: *defaults\n" +
				"  buildpack: java_buildpack"
		);
	}

	@Test
	public void test_health_check_port() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setHealthCheckType("port");

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n"
			, // ==>
			null
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: port\n"
			, // ==>
			null
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: none\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n"
		);
	}

	@Test
	public void test_health_check_none() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setHealthCheckType("none");
		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  health-check-type: none\n" +
			"  no-route: true\n"
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: port\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: none\n"
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: none\n"
			, // ==>
			null
		);
	}

	@Test
	public void test_health_check_process() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setHealthCheckType("process");
		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  health-check-type: process\n" +
			"  no-route: true\n"
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: port\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: process\n"
		);

		// Test switch from http and http endpoint to process
		performMergeTest(props,
				"applications:\n" +
				"- name: app\n" +
				"  no-route: true\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /testhealth\n"
				, // ==>
				"applications:\n" +
				"- name: app\n" +
				"  no-route: true\n" +
				"  health-check-type: process\n"
			);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: process\n"
			, // ==>
			null
		);
	}

	@Test
	public void test_health_check_http_endpoint() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		// test both "http" type and "http endpoint", as both are used together
		props.setHealthCheckType("http");
		props.setHealthCheckHttpEndpoint("/testhealth");
		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  health-check-type: http\n" +
			"  health-check-http-endpoint: /testhealth\n" +
			"  no-route: true\n"
		);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: port\n"
			, // ==>
			"applications:\n" +
			"- name: app\n" +
			"  health-check-http-endpoint: /testhealth\n" +
			"  no-route: true\n" +
			"  health-check-type: http\n"
		);

		// Test that old endpoint is replaced with new one
		performMergeTest(props,
				"applications:\n" +
				"- name: app\n" +
				"  no-route: true\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /oldtesthealth\n"
				, // ==>
				"applications:\n" +
				"- name: app\n" +
				"  no-route: true\n" +
				"  health-check-type: http\n" +
				"  health-check-http-endpoint: /testhealth\n"
			);

		performMergeTest(props,
			"applications:\n" +
			"- name: app\n" +
			"  no-route: true\n" +
			"  health-check-type: http\n" +
			"  health-check-http-endpoint: /testhealth\n"
			, // ==>
			null
		);
	}


	@Test
	public void test_memory_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/memory-1.yml"), props, getTestFile("mergeTestsData/memory-1-expected.yml"));
	}

	@Test
	public void test_memory_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/memory-2.yml"), props, getTestFile("mergeTestsData/memory-2-expected.yml"));
	}

	@Test
	public void test_memory_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/memory-3.yml"), props, getTestFile("mergeTestsData/memory-3-expected.yml"));
	}

	@Test
	public void test_memory_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/memory-4.yml"), props, null);
	}

	@Test
	public void test_memory_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/memory-5.yml"), props, null);
	}

	@Test
	public void test_memory_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(3000);
		performMergeTest(getTestFile("mergeTestsData/memory-5.yml"), props, getTestFile("mergeTestsData/memory-6-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/appNameNoMatch-1.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-1-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app-1.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/appNameNoMatch-2.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-2-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app-1.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/appNameNoMatch-3.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-3-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("app-1.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/appNameNoMatch-4.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-4-expected.yml"));
	}

	@Test
	public void test_noAppsNode_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/noAppsNode-1.yml"), props, getTestFile("mergeTestsData/noAppsNode-1-expected.yml"));
	}

	@Test
	public void test_noAppsNode_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/noAppsNode-2.yml"), props, getTestFile("mergeTestsData/noAppsNode-2-expected.yml"));
	}

	@Test
	public void test_noManifest_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/noManifest-1.yml"), props, getTestFile("mergeTestsData/noManifest-1-expected.yml"));
	}

	@Test
	public void test_noManifest_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/noManifest-2.yml"), props, getTestFile("mergeTestsData/noManifest-2-expected.yml"));
	}

	@Test
	public void test_map_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-1.yml"), props, getTestFile("mergeTestsData/map-1-expected.yml"));
	}

	@Test
	public void test_map_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-2.yml"), props, getTestFile("mergeTestsData/map-2-expected.yml"));
	}

	@Test
	public void test_map_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/map-3.yml"), props, getTestFile("mergeTestsData/map-3-expected.yml"));
	}

	@Test
	public void test_map_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/map-4.yml"), props, getTestFile("mergeTestsData/map-4-expected.yml"));
	}

	@Test
	public void test_map_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-5.yml"), props, getTestFile("mergeTestsData/map-5-expected.yml"));
	}

	@Test
	public void test_map_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-6.yml"), props, getTestFile("mergeTestsData/map-6-expected.yml"));
	}

	@Test
	public void test_map_7() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-7.yml"), props, getTestFile("mergeTestsData/map-7-expected.yml"));
	}

	@Test
	public void test_map_8() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/map-8.yml"), props, getTestFile("mergeTestsData/map-8-expected.yml"));
	}

	@Test
	public void test_map_9() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-9.yml"), props, getTestFile("mergeTestsData/map-9-expected.yml"));
	}

	@Test
	public void test_map_10() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-10.yml"), props, getTestFile("mergeTestsData/map-10-expected.yml"));
	}

	@Test
	public void test_map_11() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/map-11.yml"), props, getTestFile("mergeTestsData/map-11-expected.yml"));
	}

	@Test
	public void test_health_check_http() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setHealthCheckType("http");
		props.setHealthCheckHttpEndpoint("/testhealth");
		props.setUris(Collections.singletonList("app-1.springsource.org"));

		performMergeTest(getTestFile("mergeTestsData/health-check-http.yml"), props, getTestFile("mergeTestsData/health-check-http-expected.yml"));
	}

	@Test
	public void test_health_check_process_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("app-1.springsource.org"));
		props.setMemory(2048);
		props.setHealthCheckType("process");
		performMergeTest(getTestFile("mergeTestsData/health-check-process.yml"), props, getTestFile("mergeTestsData/health-check-process-expected.yml"));
	}

	@Test
	public void test_health_check_port_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("app-1.springsource.org"));
		props.setMemory(2048);
		// NOTE: port is a "default" value, so when setting port, the resulting manifest will have no health check type
		// as no health check type is equivalent to having "port" type
		props.setHealthCheckType("port");
		performMergeTest(getTestFile("mergeTestsData/health-check-port.yml"), props, getTestFile("mergeTestsData/health-check-port-expected.yml"));
	}

	@Test
	public void test_instances_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/instances-1.yml"), props, getTestFile("mergeTestsData/instances-1-expected.yml"));
	}

	@Test
	public void test_instances_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/instances-2.yml"), props, getTestFile("mergeTestsData/instances-2-expected.yml"));
	}

	@Test
	public void test_instances_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/instances-3.yml"), props, getTestFile("mergeTestsData/instances-3-expected.yml"));
	}

	@Test
	public void test_instances_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/instances-4.yml"), props, null);
	}

	@Test
	public void test_instances_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/instances-5.yml"), props, getTestFile("mergeTestsData/instances-5-expected.yml"));
	}

	@Test
	public void test_root_comment_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		performMergeTest(getTestFile("mergeTestsData/root-comment-1.yml"), props, getTestFile("mergeTestsData/root-comment-1-expected.yml"));
	}

	@Test
	public void test_root_comment_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		performMergeTest(getTestFile("mergeTestsData/root-comment-2.yml"), props, getTestFile("mergeTestsData/root-comment-2-expected.yml"));
	}

	@Test
	public void test_root_comment_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		props.setInstances(2);
		performMergeTest(getTestFile("mergeTestsData/root-comment-3.yml"), props, getTestFile("mergeTestsData/root-comment-3-expected.yml"));
	}

	@Test
	public void test_root_comment_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		performMergeTest(getTestFile("mergeTestsData/root-comment-4.yml"), props, getTestFile("mergeTestsData/root-comment-4-expected.yml"));
	}

	@Test
	public void test_no_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.<String>emptyList());
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-route-1.yml"), props, getTestFile("mergeTestsData/no-route-1-expected.yml"));
	}

	@Test
	public void test_no_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.<String>emptyList());
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-route-2.yml"), props, getTestFile("mergeTestsData/no-route-2-expected.yml"));
	}

	@Test
	public void test_no_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.<String>emptyList());
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-route-3.yml"), props, null);
	}

	@Test
	public void test_no_hostname_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("my-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-hostname-1.yml"), props, getTestFile("mergeTestsData/no-hostname-1-expected.yml"));
	}

	@Test
	public void test_no_hostname_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-hostname-2.yml"), props, getTestFile("mergeTestsData/no-hostname-2-expected.yml"));
	}

	@Test
	public void test_no_hostname_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-hostname-3.yml"), props, null);
	}

	@Test
	public void test_random_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/random-route-1.yml"), props, null);
	}

	@Test
	public void test_random_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("my-app1.springsource.org", "my-app2.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/random-route-2.yml"), props, getTestFile("mergeTestsData/random-route-2-expected.yml"));
	}

	@Test
	public void test_random_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app2.springsource.org"));
		props.setMemory(1024);
		props.setBuildpack("java_buildpack_offline");
		performMergeTest(getTestFile("mergeTestsData/random-route-3.yml"), props, getTestFile("mergeTestsData/random-route-3-expected.yml"));
	}

	@Test
	public void test_hosts_domains_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("my-app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-1.yml"), props, getTestFile("mergeTestsData/hosts-domains-1-expected.yml"));
	}

	@Test
	public void test_hosts_domains_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app1.springsource.org", "app2.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-2.yml"), props, getTestFile("mergeTestsData/hosts-domains-2-expected.yml"));
	}

	@Test
	public void test_hosts_domains_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app1.springsource.org", "app2.springsource.org", "app1.spring.io", "app2.spring.io"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-3.yml"), props, null);
	}

	@Test
	public void test_hosts_domains_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app1.springsource.org", "app2.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-4.yml"), props, getTestFile("mergeTestsData/hosts-domains-4-expected.yml"));
	}

	@Test
	public void test_hosts_domains_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-5.yml"), props, getTestFile("mergeTestsData/hosts-domains-5-expected.yml"));
	}

	@Test
	public void test_hosts_domains_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("spring.framework"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-6.yml"), props, getTestFile("mergeTestsData/hosts-domains-6-expected.yml"));
	}

	@Test
	public void test_hosts_domains_7() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app1.springsource.org", "app2.springsource.org", "app3.springsource.org",
				"app1.spring.io", "app2.spring.io", "app3.spring.io", "app1.spring.framework", "app2.spring.framework",
				"app3.spring.framework"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-7.yml"), props, null);
	}

	@Test
	public void test_hosts_domains_8() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app1.spring.io", "app2.spring.io"));
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/hosts-domains-8.yml"), props, getTestFile("mergeTestsData/hosts-domains-8-expected.yml"));
	}

	@Test
	public void test_root_node_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(3);
		props.setUris(Arrays.asList("app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-node-1.yml"), props, getTestFile("mergeTestsData/root-node-1-expected.yml"));
	}

	@Test
	public void test_root_node_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-node-2.yml"), props, getTestFile("mergeTestsData/root-node-2-expected.yml"));
	}

	@Test
	public void test_root_list_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setServices(Arrays.asList("s1", "s2", "s3"));
		performMergeTest(getTestFile("mergeTestsData/root-list-1.yml"), props, getTestFile("mergeTestsData/root-list-1-expected.yml"));
	}

	@Test
	public void test_root_list_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setServices(Arrays.asList("s1", "s2", "s3", "s4", "s5"));
		performMergeTest(getTestFile("mergeTestsData/root-list-2.yml"), props, getTestFile("mergeTestsData/root-list-2-expected.yml"));
	}

	@Test
	public void test_root_list_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setServices(Arrays.asList("s1", "s3", "s4", "s5"));
		performMergeTest(getTestFile("mergeTestsData/root-list-3.yml"), props, getTestFile("mergeTestsData/root-list-3-expected.yml"));
	}

	@Test
	public void test_root_map_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		Map<String, String> env = new HashMap<>();
		env.put("k1", "v1");
		env.put("k2", "v2");
		env.put("k3", "v3");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/root-map-1.yml"), props, getTestFile("mergeTestsData/root-map-1-expected.yml"));
	}

	@Test
	public void test_root_map_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		Map<String, String> env = new HashMap<>();
		env.put("k1", "v1");
		env.put("k2", "v2-alt");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/root-map-2.yml"), props, getTestFile("mergeTestsData/root-map-2-expected.yml"));
	}

	@Test
	public void test_root_map_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-map-3.yml"), props, getTestFile("mergeTestsData/root-map-3-expected.yml"));
	}

	@Test
	public void test_root_map_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		Map<String, String> env = new HashMap<>();
		env.put("k2", "v2");
		props.setEnvironmentVariables(env);
		performMergeTest(getTestFile("mergeTestsData/root-map-4.yml"), props, getTestFile("mergeTestsData/root-map-4-expected.yml"));
	}

	@Test
	public void test_root_noroute_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-noroute-1.yml"), props, getTestFile("mergeTestsData/root-noroute-1-expected.yml"));
	}

	@Test
	public void test_root_noroute_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		performMergeTest(getTestFile("mergeTestsData/root-noroute-2.yml"), props, getTestFile("mergeTestsData/root-noroute-2-expected.yml"));
	}

	@Test
	public void test_root_randomroute_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-randomroute-1.yml"), props, getTestFile("mergeTestsData/root-randomroute-1-expected.yml"));
	}

	@Test
	public void test_root_randomroute_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-randomroute-2.yml"), props, null);
	}

	@Test
	public void test_root_nohost_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("my-app.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-nohost-1.yml"), props, getTestFile("mergeTestsData/root-nohost-1-expected.yml"));
	}

	@Test
	public void test_root_nohost_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("springsource.org", "spring.io"));
		performMergeTest(getTestFile("mergeTestsData/root-nohost-2.yml"), props, getTestFile("mergeTestsData/root-nohost-2-expected.yml"));
	}

	@Test
	public void test_root_nohost_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-nohost-3.yml"), props, null);
	}

	@Test
	public void test_root_hosts_domains_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app1.springsource.org", "app2.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-hosts-domains-1.yml"), props, getTestFile("mergeTestsData/root-hosts-domains-1-expected.yml"));
	}

	@Test
	public void test_root_hosts_domains_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("my-app.springsource.org", "test-app1.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-hosts-domains-2.yml"), props, getTestFile("mergeTestsData/root-hosts-domains-2-expected.yml"));
	}

	@Test
	public void test_root_hosts_domains_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("test-app-1.springsource.org", "test-app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/root-hosts-domains-3.yml"), props, getTestFile("mergeTestsData/root-hosts-domains-3-expected.yml"));
	}

	@Test
	public void test_routes_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-1.yml"), props, getTestFile("mergeTestsData/routes-1-expected.yml"));
	}

	@Test
	public void test_routes_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-2.yml"), props, getTestFile("mergeTestsData/routes-2-expected.yml"));
	}

	@Test
	public void test_routes_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-3.yml"), props, getTestFile("mergeTestsData/routes-3-expected.yml"));
	}

	@Test
	public void test_routes_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-4.yml"), props, getTestFile("mergeTestsData/routes-4-expected.yml"));
	}

	@Test
	public void test_routes_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-5.yml"), props, getTestFile("mergeTestsData/routes-5-expected.yml"));
	}

	@Test
	public void test_routes_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("demo-app");
		props.setMemory(1024);
		props.setInstances(1);
		props.setUris(Arrays.asList("route-2.springsource.org"));
		performMergeTest(getTestFile("mergeTestsData/routes-6.yml"), props, getTestFile("mergeTestsData/routes-6-expected.yml"));
	}

	@Test
	public void test_routes_paths_ports() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(2048);
		props.setInstances(1);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org", "app-4.springsource.org/myappPath/moresegments", "tcp.springsource.org:9003"));
		performMergeTest(getTestFile("mergeTestsData/routes-paths-ports.yml"), props, getTestFile("mergeTestsData/routes-paths-ports-expected.yml"));
	}

	@Test
	public void test_no_route_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.<String>emptyList());
		props.setMemory(2048);
		performMergeTest(getTestFile("mergeTestsData/no-route-5.yml"), props, getTestFile("mergeTestsData/no-route-5-expected.yml"));
	}

	@Test
	public void test_random_route_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org", "app-3.springsource.org"));
		props.setMemory(512);
		performMergeTest(getTestFile("mergeTestsData/random-route-4.yml"), props, null);
	}

	@Test
	public void test_random_route_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Arrays.asList("app.springsource.org"));
		props.setMemory(512);
		performMergeTest(getTestFile("mergeTestsData/random-route-5.yml"), props, getTestFile("mergeTestsData/random-route-5-expected.yml"));
	}
}

