/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.actuator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansContext;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springframework.ide.eclipse.boot.dash.model.actuator.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.actuator.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.LiveEnvModel;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.Profile;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.Property;
import org.springframework.ide.eclipse.boot.dash.model.actuator.env.PropertySource;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Tests data obtained from the Actuator
 *
 * @author Alex Boyko
 * @author Nieraj Singh
 */
public class ActuatorDataTest {

	static class TestActuatorClient extends ActuatorClient {

		private String version = null;
		private String beansJson = "";
		private String requestMappingsJson = "";
		private String envJson = "";


		public TestActuatorClient(TypeLookup typeLookup) {
			super(typeLookup);
		}

		public TestActuatorClient version(String version) {
			this.version = version;
			return this;
		}

		public TestActuatorClient envJson(String json) {
			this.envJson = json;
			return this;
		}

		public TestActuatorClient beansJson(String json) {
			this.beansJson = json;
			return this;
		}

		public TestActuatorClient requestMappingJson(String json) {
			this.requestMappingsJson = json;
			return this;
		}

		@Override
		protected ImmutablePair<String, String> getRequestMappingData() throws Exception {
			return ImmutablePair.of(requestMappingsJson, version);
		}

		@Override
		protected ImmutablePair<String, String> getBeansData() throws Exception {
			return ImmutablePair.of(beansJson, version);
		}

		@Override
		protected ImmutablePair<String, String> getEnvData() throws Exception {
			return ImmutablePair.of(envJson, version);
		}

	}


	public static String getContents(String resourcePath) throws Exception {
		InputStream input = ActuatorDataTest.class.getResourceAsStream(resourcePath);
		String s = IOUtil.toString(input);
		System.out.println(s);
		return s;
	}

	@Test public void testModelEquality() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(getContents("beans-sample.json")).version("1");
		LiveBeansModel liveBeans = client.getBeans();
		assertEquals(liveBeans, client.getBeans());
	}

	@Test public void testModelIneuality_1() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(getContents("beans-sample.json")).version("1");
		TestActuatorClient otherClient = new TestActuatorClient(null).beansJson(getContents("beans-sample-diff1.json")).version("1");
		assertNotEquals(client.getBeans(), otherClient.getBeans());
	}

	@Test public void testModelContent() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(getContents("beans-sample.json")).version("1");
		LiveBeansModel liveBeans = client.getBeans();
		assertEquals(1, liveBeans.getBeansByContext().size());
		LiveBeansContext context = liveBeans.getBeansByContext().get(0);
		assertEquals(2, context.getElements().size());
		assertEquals(2, liveBeans.getBeans().size());
		assertEquals(2, liveBeans.getBeansByResource().size());

		assertEquals(context.getElements(), liveBeans.getBeans());

		LiveBean bean1 = liveBeans.getBeans().get(0);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$Jackson2ObjectMapperBuilderCustomizerConfiguration$StandardJackson2ObjectMapperBuilderCustomizer",
				bean1.getBeanType());
		assertEquals("standardJacksonObjectMapperBuilderCustomizer", bean1.getId());

		LiveBean bean2 = liveBeans.getBeans().get(1);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonProperties",
				bean2.getBeanType());
		assertEquals("spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties", bean2.getId());
	}

	@Test public void testModelContentBoot2() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(getContents("beans-sample-boot2-1.json")).version("2");
		LiveBeansModel liveBeans = client.getBeans();
		assertEquals(1, liveBeans.getBeansByContext().size());
		LiveBeansContext context = liveBeans.getBeansByContext().get(0);
		assertEquals(2, context.getElements().size());
		assertEquals(2, liveBeans.getBeans().size());
		assertEquals(2, liveBeans.getBeansByResource().size());

		assertEquals(context.getElements(), liveBeans.getBeans());

		// Boot 2 order of beans is arbitrary due to JSONObject implementation.

		LiveBean bean1 = liveBeans.getBeans().stream().filter(b -> "standardJacksonObjectMapperBuilderCustomizer".equals(b.getId())).findFirst().orElse(null);
		assertNotNull(bean1);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$Jackson2ObjectMapperBuilderCustomizerConfiguration$StandardJackson2ObjectMapperBuilderCustomizer",
				bean1.getBeanType());
		assertEquals("standardJacksonObjectMapperBuilderCustomizer", bean1.getId());

		LiveBean bean2 = liveBeans.getBeans().stream().filter(b -> "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties".equals(b.getId())).findFirst().orElse(null);
		assertNotNull(bean2);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonProperties",
				bean2.getBeanType());
		assertEquals("spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties", bean2.getId());
	}

	@Test public void testRequestMappingsBoot2() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).version("2").requestMappingJson(getContents("requestmappings-sample-boot2.json"));
		List<RequestMapping> mappings = client.getRequestMappings();
		System.out.println(mappings);
		ImmutableSet<String> expected = ImmutableSet.of(
				"/**/favicon.ico",
				"/actuator/health",
				"/actuator/info",
				"/actuator",
				"/hello",
				"/greeting",
				"/error",
				"/webjars/**",
				"/**"
		);
		assertEquals(expected,
				mappings.stream()
				.map(RequestMapping::getPath)
				.collect(Collectors.toSet())
		);
	}

	@Test public void testRequestMappingsBoot2WebFlux() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).version("2").requestMappingJson(getContents("requestmappings-boot2-webflux.json"));
		List<RequestMapping> mappings = client.getRequestMappings();
		System.out.println(mappings);
		ImmutableSet<String> expected = ImmutableSet.of(
				"/actuator",
				"/actuator/health",
				"/actuator/info",
				"/webjars/**",
				"/hello",
				"/pp",
				"/qq",
				"/**"
		);
		assertEquals(expected,
				mappings.stream()
				.map(RequestMapping::getPath)
				.collect(Collectors.toSet())
		);
	}

	@Test public void testRequestMappingsBoot2WebFluxFunctional() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).version("2").requestMappingJson(getContents("requestmappings-boot2-webflux-functional.json"));
		List<RequestMapping> mappings = client.getRequestMappings();
		System.out.println(mappings);
		ImmutableSet<String> expected = ImmutableSet.of(
				"/actuator",
				"/actuator/health",
				"/actuator/info",
				"/webjars/**",
				"/hello",
				"/**"
		);
		assertEquals(expected,
				mappings.stream()
				.map(RequestMapping::getPath)
				.collect(Collectors.toSet())
		);
	}

	@Test public void testRequestMappingsWithManualServletRegistration() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).version("2").requestMappingJson(getContents("requestmappings-with-manual-servlet-registration.json"));
		List<RequestMapping> mappings = client.getRequestMappings();
		System.out.println(mappings);
		ImmutableSet<String> expected = ImmutableSet.of(
				"/actuator/beans",
				"/error",
				"/webjars/**",
				"/**",
				"/**",
				"/service/*",
				"/services/*",
				"/hello"
		);
		assertEquals(expected,
				mappings.stream()
				.map(RequestMapping::getPath)
				.collect(Collectors.toSet())
		);
	}

	@Test public void testEnvModelEquality() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).envJson(getContents("env-sample-boot2.json")).version("2");
		LiveEnvModel env = client.getEnv();
		assertEquals(env, client.getEnv());
	}

	@Test public void testEnvModelInequality() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null)
				.envJson(getContents("env-sample-boot2.json")).version("2");
		TestActuatorClient otherClient = new TestActuatorClient(null)
				.envJson(getContents("env-sample-boot2-diff.json")).version("2");

		LiveEnvModel env = client.getEnv();
		LiveEnvModel otherEnv = otherClient.getEnv();
		assertNotEquals(env, otherEnv);

		assertNotEquals(env.getActiveProfiles().getProfiles(), otherEnv.getActiveProfiles().getProfiles());
		assertNotEquals(env.getPropertySources().getPropertySources(),
				otherEnv.getPropertySources().getPropertySources());
	}

	@Test public void testEnvModelContent() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null)
				.envJson(getContents("env-sample-boot2.json")).version("2");
		LiveEnvModel env = client.getEnv();
		assertEquals(ImmutableList.of(new Profile("production"), new Profile("staging")),
				env.getActiveProfiles().getProfiles());

		// Check a property source with no origin
		PropertySource noOriginPropertySource = env.getPropertySources().getPropertySources().stream()
				.filter(propertysource -> "server.ports".equals(propertysource.getDisplayName())).findFirst()
				.orElse(null);
		assertNotNull(noOriginPropertySource);
		assertEquals(1, noOriginPropertySource.getProperties().size());
		assertEquals("local.server.port", noOriginPropertySource.getProperties().get(0).getName());
		assertEquals("8080", noOriginPropertySource.getProperties().get(0).getValue());
		assertNull(noOriginPropertySource.getProperties().get(0).getOrigin());

		// Check a property source with origin
		PropertySource propertySource = env.getPropertySources().getPropertySources().stream()
				.filter(propertysource -> "systemEnvironment".equals(propertysource.getDisplayName())).findFirst()
				.orElse(null);
		assertNotNull(propertySource);
		Property property = propertySource.getProperties().stream()
				.filter(prop -> "JAVA_MAIN_CLASS_43370".equals(prop.getName())).findFirst().orElse(null);
		assertNotNull(property);
		assertEquals("hello.Application", property.getValue());
		assertEquals("System Environment Property \"JAVA_MAIN_CLASS_43370\"", property.getOrigin().getOrigin());
	}

	@Test public void testEnvModelEqualityBoot1x() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).envJson(getContents("env-sample-boot1.json")).version("1");
		LiveEnvModel env = client.getEnv();
		assertEquals(env, client.getEnv());
	}

	@Test public void testEnvModelContentBoot1x() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null)
				.envJson(getContents("env-sample-boot1.json")).version("1");
		LiveEnvModel env = client.getEnv();

		PropertySource propertySource = env.getPropertySources().getPropertySources().stream()
				.filter(propertysource -> "server.ports".equals(propertysource.getDisplayName())).findFirst()
				.orElse(null);
		assertNotNull(propertySource);
		assertEquals(1, propertySource.getProperties().size());
		assertEquals("local.server.port", propertySource.getProperties().get(0).getName());
		assertEquals("8090", propertySource.getProperties().get(0).getValue());

		propertySource = env.getPropertySources().getPropertySources().stream()
				.filter(propertysource -> "commandLineArgs".equals(propertysource.getDisplayName())).findFirst()
				.orElse(null);
		assertNotNull(propertySource);
		assertEquals(1, propertySource.getProperties().size());
		assertEquals("spring.output.ansi.enabled", propertySource.getProperties().get(0).getName());
		assertEquals("always", propertySource.getProperties().get(0).getValue());

		propertySource = env.getPropertySources().getPropertySources().stream()
				.filter(propertysource -> "systemProperties".equals(propertysource.getDisplayName())).findFirst()
				.orElse(null);
		assertNotNull(propertySource);

		Property property = propertySource.getProperties().stream()
				.filter(prop -> "file.encoding.pkg".equals(prop.getName())).findFirst().orElse(null);
		assertNotNull(property);
		assertEquals("sun.io", property.getValue());

	}
}
