/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.ide.vscode.commons.util.MemoizingProxy;

public class RemoteSpringBootApp extends AbstractSpringBootApp {
	
	private static MemoizingProxy.Builder<RemoteSpringBootApp> memoizingProxyBuilder = MemoizingProxy.builder(RemoteSpringBootApp.class,  Duration.ofMillis(4900), 
			String.class, String.class, String.class, String.class, boolean.class
	);

	private final String jmxUrl;
	private final String host;
	private final String port;
	private final String urlScheme;
	private boolean keepChecking;

	public static SpringBootApp create(String jmxUrl, String host, String port, String urlScheme, boolean keepChecking) {
		return memoizingProxyBuilder.newInstance(jmxUrl, host, port, urlScheme, keepChecking);
	}

	protected RemoteSpringBootApp(String jmxUrl, String host, String port, String urlScheme, boolean keepChecking) {
		this.jmxUrl = jmxUrl;
		this.host = host;
		this.port = port;
		this.urlScheme = urlScheme;
		this.keepChecking = keepChecking;
	}

	@Override
	protected String getJmxUrl() {
		return jmxUrl;
	}

	@Override
	public String getPort() throws Exception {
		return port != null ? port : super.getPort();
	}

	@Override
	public String getHost() throws Exception {
		if (host != null) {
			return host;
		}
		return super.getHost();
	}

	@Override
	public Properties getSystemProperties() throws Exception {
		return withPlatformMxBean(RuntimeMXBean.class, runtime -> {
			Properties props = new Properties();
			for (Entry<String, String> e : runtime.getSystemProperties().entrySet()) {
				props.put(e.getKey(), e.getValue());
			}
			return props;
		});
	}

	@Override
	public String getProcessID() {
		try {
			return withPlatformMxBean(RuntimeMXBean.class, runtime -> runtime.getName());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getProcessName() throws Exception {
		try {
			String command = getJavaCommand();
			if (command != null) {
				int space = command.indexOf(' ');
				if (space >= 0) {
					command = command.substring(0, space);
				}
				command = command.trim();
				if (!"".equals(command)) {
					return command;
				}
			}
		} catch (IOException e) {
			logger.error("", e);
		}
		return "Unknown";
	}

	@Override
	public String getUrlScheme() {
		return urlScheme;
	}

	@Override
	public boolean hasUsefulJmxBeans() {
		if (keepChecking) {
			try {
				logger.info("checking for spring jmx beans, continuously trying -- " + this.toString());
				return super.containsSpringJmxBeans();
			}
			catch (Exception e) {
				logger.info("no spring jmx beans found, continuously trying -- " + this.toString());
				return false;
			}
		}
		else {
			return super.hasUsefulJmxBeans();
		}
	}

}
