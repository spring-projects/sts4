/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.util;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.management.remote.JMXConnector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunch;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

/**
 * Creates and manages an instance of {@link SpringApplicationLifecycleClient}.
 *
 * @author Kris De Volder
 */
public class SpringApplicationLifeCycleClientManager {

	private Callable<JMXConnector> connectionProvider;
	private JMXConnector connector;
	private SpringApplicationLifecycleClient client;

	public SpringApplicationLifeCycleClientManager(Callable<JMXConnector> connectionProvider) {
		Assert.isNotNull(connectionProvider);
		this.connectionProvider = connectionProvider;
	}

	/**
	 * Convenenience method, use ILaunch as the jmxPort provider.
	 */
	public SpringApplicationLifeCycleClientManager(ILaunch l) {
		this(() -> createJMXConnection(BootLaunchConfigurationDelegate.getJMXPortAsInt(l)));
	}

	private static JMXConnector createJMXConnection(int port) {
		if (port <=0) {
			throw new IllegalStateException("JMX port not specified");
		}
		try {
			return JMXClient.createLocalJmxConnector(port);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Convenenience method, use a given fixed port.
	 */
	public SpringApplicationLifeCycleClientManager(int resolvedPort) {
		this(() -> createJMXConnection(resolvedPort));
	}

	/**
	 * Dispose of current client and JMX connection. This does not
	 * make the manager itself unusable, as an attempt will be made to
	 * re-establish the connection the next time it is needed.
	 */
	public synchronized void disposeClient() {
		try {
			if (connector!=null) {
				connector.close();
			}
		} catch (Exception e) {
			//ignore
		}
		client = null;
		connector = null;
	}

	/**
	 * Try to obtain a client, may return null if a connection could not be established.
	 */
	public SpringApplicationLifecycleClient getLifeCycleClient() throws Exception {
		try {
			if (client==null) {
				connector = connectionProvider.call();
				if (connector!=null) {
					client = new SpringApplicationLifecycleClient(
							connector.getMBeanServerConnection(),
							SpringApplicationLifecycleClient.DEFAULT_OBJECT_NAME
					);
				}
			}
			return client;
		} catch (Exception e) {
			//e.printStackTrace();
			//Someting went wrong creating client (most likely process we are trying to connect
			// doesn't exist yet or has been terminated.
			disposeClient();
			throw e;
		}
	}

}
