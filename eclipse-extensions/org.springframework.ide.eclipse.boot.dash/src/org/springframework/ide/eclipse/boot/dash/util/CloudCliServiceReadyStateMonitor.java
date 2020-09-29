/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

import org.eclipse.debug.core.ILaunch;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifecycleClient;
import org.springsource.ide.eclipse.commons.core.util.ProcessUtils;

/**
 * Spring Cloud CLI 1.3.X Service ready state monitor implementation. Based on
 * JMX Connection and examining bean's attribute value.
 *
 * @author Alex Boyko
 *
 */
public class CloudCliServiceReadyStateMonitor extends AbstractPollingAppReadyStateMonitor {

	private Supplier<JMXConnector> jmxConnectionProvider;
	private JMXConnector connector;
	private String serviceId;

	public CloudCliServiceReadyStateMonitor(Supplier<JMXConnector> jmxConnectionProvider, String serviceId) {
		super();
		this.jmxConnectionProvider = jmxConnectionProvider;
		this.serviceId = serviceId;
	}

	public CloudCliServiceReadyStateMonitor(ILaunch launch, String id) {
		this(() -> createConnector(launch), id);
	}

	private static JMXConnector createConnector(ILaunch l) {
		String pid = l.getAttribute(BootLaunchConfigurationDelegate.PROCESS_ID);
		if (pid == null) {
			return null;
		} else {
			if (Long.valueOf(pid) < 0) {
				throw new IllegalStateException("Invalid PID");
			} else {
				return ProcessUtils.createJMXConnector(pid);
			}
		}
	}

	@Override
	public void dispose() {
		if (connector != null) {
			try {
				connector.close();
			} catch (IOException e) {
				// Ignore - process might be dead already
			}
			connector = null;
		}
		super.dispose();
	}

	@Override
	protected boolean checkReady() {
		try {
			if (connector == null) {
				try {
					connector = jmxConnectionProvider.get();
				} catch (IllegalStateException e) {
					// Invalid PID exception. Means that attempt to calculate PID has failed, hence fall back to no JMX connection case -> show ready state as ready
					e.printStackTrace();
					return true;
				}
			}
			if (connector != null) {
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				try {
					Set<ObjectName> queryNames = connection.queryNames(SpringApplicationLifecycleClient.toObjectName("launcher." + serviceId + ":type=Endpoint,name=Health"), null);
					// Cloud CLI service 2.x
					if (!queryNames.isEmpty()) {
						if (queryNames.size() == 1) {
							Object o = connection.invoke(queryNames.iterator().next(),"health",
									new Object[0],
									new String[0]);
							if (o instanceof Map) {
								return "UP".equals(((Map<?,?>)o).get("status"));
							}
//							return dataStr.contains("status=UP");
						} else if (queryNames.size() > 1) {
							throw new Exception("Too many beans matching search criteria: " + queryNames);
						}
					}
					// Legacy Clod CLI service 1.x support
					queryNames = connection.queryNames(SpringApplicationLifecycleClient.toObjectName("launcher." + serviceId + ":type=Endpoint,name=healthEndpoint,identity=*"), null);
					if (queryNames.size() == 1) {
						Object o = connection.invoke(queryNames.iterator().next(),"getData",
								new Object[0],
								new String[0]);
						if (o instanceof Map) {
							return "UP".equals(((Map<?,?>)o).get("status"));
						}
//						return dataStr.contains("status=UP");
					} else if (queryNames.size() > 1) {
						throw new Exception("Too many beans matching search criteria: " + queryNames);
					}
				} catch (AttributeNotFoundException e) {
					throw new IllegalStateException(
							"Unexpected: attribute 'Data' not available", e);
				} catch (InstanceNotFoundException e) {
					return false; // Instance not available yet
				} catch (MBeanException e) {
					throw new Exception(e.getCause());
				} catch (ReflectionException e) {
					throw new Exception("Failed to retrieve Data attribute",
							e.getCause());
				}
			}
		} catch (Exception e) {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException ex) {
					// Ignore - process might be dead already
				}
				connector = null;
			}
		}
		return false;
	}

}
