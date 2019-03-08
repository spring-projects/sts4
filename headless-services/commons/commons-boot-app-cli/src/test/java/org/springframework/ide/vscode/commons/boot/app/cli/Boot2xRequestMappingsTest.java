/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMappingsParser20;

import com.google.common.collect.ImmutableSet;

public class Boot2xRequestMappingsTest {

	@Test
	public void testWebRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-web.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));
		assertEquals(10, rms.size());

		ImmutableSet<String> expected = ImmutableSet.of(
				"/error",
				"/**/favicon.ico",
				"/actuator",
				"/actuator/health",
				"/actuator/info",
				"/hello",
				"/qq",
				"/pp",
				"/webjars/**",
				"/**"
		);
		assertEquals(expected,
				rms.stream()
				.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
				.collect(Collectors.toSet())
		);

	}

	@Test
	public void testWebFluxAnnotationRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-webflux.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));

		assertEquals(7, rms.size());

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
				rms.stream()
				.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
				.collect(Collectors.toSet())
		);
	}

	@Test
	public void testWebFluxAnnotationRmsFunctional() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-webflux-functional.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));

		assertEquals(6, rms.size());

		ImmutableSet<String> expected = ImmutableSet.of(
				"/actuator",
				"/actuator/health",
				"/actuator/info",
				"/webjars/**",
				"/hello",
				"/**"
		);
		assertEquals(expected,
				rms.stream()
				.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
				.collect(Collectors.toSet())
		);
	}

	@Test
	public void testWebEurekaRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-web-eureka.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));
		assertEquals(9, rms.size());

		ImmutableSet<String> expected = ImmutableSet.of(
				"/error",
				"/**/favicon.ico",
				"/actuator",
				"/welcome",
				"/actuator/health",
				"/actuator/info",
				"/webjars/**",
				"/**"
		);
		assertEquals(expected,
				rms.stream()
				.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
				.collect(Collectors.toSet())
		);

	}

	@Test
	public void testWebFluxEurekaRms() throws Exception {
		String json = IOUtils.toString(Boot2xRequestMappingsTest.class.getResourceAsStream("/live-rm-beans/rms-boot2-webflux-eureka.json"));
		Collection<RequestMapping> rms = RequestMappingsParser20.parse(new JSONObject(json));
		assertEquals(6, rms.size());

		ImmutableSet<String> expected = ImmutableSet.of(
				"/actuator",
				"/actuator/health",
				"/actuator/info",
				"/welcome",
				"/webjars/**",
				"/**"
		);
		assertEquals(expected,
				rms.stream()
				.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
				.collect(Collectors.toSet())
		);

	}
}
