/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.remote.JMXServiceURL;

public class RemoteSpringBootApp extends AbstractSpringBootApp {

	private String jmxUrl;

	private RemoteSpringBootApp(String jmxUrl) {
		this.jmxUrl = jmxUrl;
	}

	@Override
	protected JMXServiceURL getJmxUrl() throws MalformedURLException {
		return new JMXServiceURL(jmxUrl);
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
	public boolean isSpringBootApp() {
		//For now, let's assume that, if its not a boot app, then we won't create
		//a RemoteSpringBootApp instance for it.
		return true;
	}

	@Override
	public String getProcessID() {
		try {
			return withPlatformMxBean(RuntimeMXBean.class, runtime -> runtime.getName());
		} catch (Exception e) {
			return "Unknown-PID";
		}
	}

	@Override
	public String getProcessName() throws Exception {
		try {
			String command = getJavaCommand();
			if (command!=null) {
				int space = command.indexOf(' ');
				if (space>=0) {
					command = command.substring(0, space);
				}
				command = command.trim();
				if (!"".equals(command)) {
					return command;
				}
			}
		} catch (IOException e) {
			logger.equals(e);
		}
		return "Unknown";
	}

	public static SpringBootApp create(String jmxUrl) {
		RemoteSpringBootApp delegate = new RemoteSpringBootApp(jmxUrl);
		return (SpringBootApp) Proxy.newProxyInstance(RemoteSpringBootApp.class.getClassLoader(), new Class[] {SpringBootApp.class}, new NoArgumentsCacheHandler(delegate, Duration.ofMillis(4900)));
	}

}
