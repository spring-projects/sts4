/*******************************************************************************
 * Copyright (c) 2013-2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.util;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * A JMX client for the {@code SpringApplicationLifecycle} mbean. Permits to obtain
 * information about the lifecycle of a given Spring application.
 *
 * @author Stephane Nicoll
 */
public class SpringApplicationLifecycleClient {

	// Note: see SpringApplicationLifecycleAutoConfiguration
	static final String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";

	private final MBeanServerConnection connection;

	private final ObjectName objectName;

	public SpringApplicationLifecycleClient(MBeanServerConnection connection,
			String jmxName) {
		this.connection = connection;
		this.objectName = toObjectName(jmxName);
	}

	/**
	 * Check if the spring application managed by this instance is ready. Returns
	 * {@code false} if the mbean is not yet deployed so this method should be repeatedly
	 * called until a timeout is reached.
	 * @return {@code true} if the application is ready to service requests
	 * @throws JmxClientException if the JMX service could not be contacted
	 */
	public boolean isReady() throws Exception {
		try {
			return (Boolean) this.connection.getAttribute(this.objectName, "Ready");
		}
		catch (InstanceNotFoundException ex) {
			return false; // Instance not available yet
		}
		catch (AttributeNotFoundException ex) {
			throw new IllegalStateException(
					"Unexpected: attribute 'Ready' not available", ex);
		}
		catch (ReflectionException ex) {
			throw new Exception("Failed to retrieve Ready attribute",
					ex.getCause());
		}
		catch (MBeanException ex) {
			throw new Exception(ex.getMessage(), ex);
		}
		catch (IOException ex) {
			throw new Exception(ex.getMessage(), ex);
		}
	}

	public int getProperty(String prop, int defaultValue) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		Object o = this.connection.invoke(this.objectName,"getProperty",
				new String[] {prop},
				new String[] {String.class.getName()});
		if (o instanceof Integer) {
			return (Integer)o;
		} else if (o instanceof String) {
			return Integer.parseInt((String) o);
		}
		return defaultValue;
	}

	/**
	 * Stop the application managed by this instance.
	 * @throws JmxClientException if the JMX service could not be contacted
	 * @throws IOException if an I/O error occurs
	 * @throws InstanceNotFoundException if the lifecycle mbean cannot be found
	 */
	public void stop() throws Exception {
		try {
			this.connection.invoke(this.objectName, "shutdown", null, null);
		}
		catch (ReflectionException ex) {
			throw new Exception("Shutdown failed", ex.getCause());
		}
		catch (MBeanException ex) {
			throw new Exception("Could not invoke shutdown operation", ex);
		}
	}

	public static ObjectName toObjectName(String name) {
		try {
			return new ObjectName(name);
		}
		catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("Invalid jmx name '" + name + "'");
		}
	}

}
