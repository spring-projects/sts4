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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockSshTunnel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class JmxSupportTest {

	CloudAppDashElement cde = mock(CloudAppDashElement.class);
	JmxSshTunnelManager tunnels = new JmxSshTunnelManager();
	SshTunnelFactory tunnelFactory = MockSshTunnel::new;

	@Test
	public void setupEnvVars() throws Exception {
		int testPort = 1234;
		when(cde.getBaseRunStateExp()).thenReturn(LiveExpression.constant(RunState.INACTIVE));
		JmxSupport jmx = new JmxSupport(cde, tunnels, tunnelFactory) {
			public int getPort() {return testPort; }
		};

		Map<String, String> env = new HashMap<>();
		jmx.setupEnvVars(env);

		assertEquals(
				"-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false "
				+ "-Dspring.jmx.enabled=true"
				+ "-Dmanagement.endpoints.jmx.exposure.include=*",
				env.get("JAVA_OPTS"));

		jmx.setupEnvVars(env); // should erase old and recreate
		assertEquals(
				"-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false "
				+ "-Dspring.jmx.enabled=true"
				+ "-Dmanagement.endpoints.jmx.exposure.include=*",
				env.get("JAVA_OPTS"));
	}

	@Test
	public void setupEnvVars_preserve_unrelated_java_opts() throws Exception {
		int testPort = 1234;
		when(cde.getBaseRunStateExp()).thenReturn(LiveExpression.constant(RunState.INACTIVE));
		JmxSupport jmx = new JmxSupport(cde, tunnels, tunnelFactory) {
			public int getPort() {return testPort; }
		};

		Map<String, String> env = new HashMap<>();
		env.put("JAVA_OPTS", "-Dsomething.already=here");
		jmx.setupEnvVars(env);

		assertEquals("-Dsomething.already=here "
				+ "-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false "
				+ "-Dspring.jmx.enabled=true"
				+ "-Dmanagement.endpoints.jmx.exposure.include=*",
				env.get("JAVA_OPTS"));

		jmx.setupEnvVars(env); // should erase old and recreate
		assertEquals("-Dsomething.already=here "
				+ "-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false "
				+ "-Dspring.jmx.enabled=true"
				+ "-Dmanagement.endpoints.jmx.exposure.include=*",
				env.get("JAVA_OPTS"));

	}

}
