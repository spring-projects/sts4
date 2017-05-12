/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootAppCLI {

	public static void main(String[] args) throws Exception {
		List<VirtualMachineDescriptor> list = VirtualMachine.list();
		for (VirtualMachineDescriptor vmd : list) {
			VirtualMachine vm = VirtualMachine.attach(vmd);
			Properties props = vm.getSystemProperties();

			String classpath = (String) props.get("java.class.path");
			String[] cpElements = getClasspath(classpath);
			boolean isSpringBoot = contains(cpElements, "spring-boot");
			boolean isSpringBootActuator = contains(cpElements, "spring-boot-actuator");

			if (isSpringBoot) {
				printBootAppDetails(vmd, vm, isSpringBootActuator);
			}
		}
	}

	private static void printBootAppDetails(VirtualMachineDescriptor vmd, VirtualMachine vm,
			boolean isSpringBootActuator) throws IOException {
		
		System.out.println("Spring Boot App: " + vmd.id());
		System.out.println("Name: " + vmd.displayName());
		System.out.println("Actuators available: " + isSpringBootActuator);

		String jmxConnect = vm.startLocalManagementAgent();
		System.out.println("JMX Connection: " + jmxConnect);
		
		JMXConnector jmxConnector = null;
		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(jmxConnect);
			jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

			try {
				String port = getPort(jmxConnector);
				System.out.println("Runs on port: " + port);
			}
			catch (InstanceNotFoundException e) {
				System.out.println("Port not available");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
		
		System.out.println();
	}

	private static boolean contains(String[] cpElements, String element) {
		for (String cpElement : cpElements) {
			if (cpElement.contains(element)) {
				return true;
			}
		}
		return false;
	}

	private static String[] getClasspath(String classpath) {
		List<String> classpathElements = new ArrayList<>();
		if (classpath != null) {
			StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
			while (tokenizer.hasMoreTokens()) {
				String classpathElement = tokenizer.nextToken();
				classpathElements.add(classpathElement);
			}
		}
		return (String[]) classpathElements.toArray(new String[classpathElements.size()]);
	}

	private static String getPort(JMXConnector jmxConnector) throws Exception {
		MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
		
		String port = getPortViaAdmin(connection);
		if (port != null) {
			return port;
		}
		else {
			return getPortViaActuator(connection);
		}
	}
	
	private static String getPortViaAdmin(MBeanServerConnection connection) throws Exception {
		try {
			String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";
			ObjectName objectName = new ObjectName(DEFAULT_OBJECT_NAME);
	
			Object o = connection.invoke(objectName,"getProperty", new String[] {"local.server.port"}, new String[] {String.class.getName()});
			return o.toString();
		}
		catch (InstanceNotFoundException e) {
			System.out.println("Admin management bean not available");
			return null;
		}
	}

	private static String getPortViaActuator(MBeanServerConnection connection) throws Exception {
		try {
			String environment = getEnvironment(connection);
			System.out.println("Environment: " + environment);
			
			JSONObject env = new JSONObject(environment);
			if (env != null) {
				JSONObject portsObject = env.getJSONObject("server.ports");
				if (portsObject != null) {
					String portValue = portsObject.getString("local.server.port");
					return portValue;
				}
			}
		}
		catch (InstanceNotFoundException e) {
			System.out.println("Actuator not available: Spring Boot actuators not enabled");
		}
		return null;
	}

	private static String getEnvironment(MBeanServerConnection connection) throws Exception {
		String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Endpoint,name=environmentEndpoint";
		ObjectName objectName = new ObjectName(DEFAULT_OBJECT_NAME);
		
		Object result = connection.getAttribute(objectName, "Data");
		return new ObjectMapper().writeValueAsString(result);
	}

}
