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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
public class SpringBootApp {
	
	private VirtualMachine vm;
	private VirtualMachineDescriptor vmd;

	public SpringBootApp(VirtualMachineDescriptor vmd) throws Exception {
		this.vmd = vmd;
		this.vm = VirtualMachine.attach(vmd);
	}
	
	public String getProcessID() {
		return vmd.id();
	}
	
	public String getProcessName() {
		return vmd.displayName();
	}
	
	public boolean isSpringBootApp() throws Exception {
		Properties props = this.vm.getSystemProperties();

		String classpath = (String) props.get("java.class.path");
		String[] cpElements = getClasspath(classpath);
		return contains(cpElements, "spring-boot");
	}
	
	public String getPort() throws Exception {
		String jmxConnect = this.vm.startLocalManagementAgent();

		JMXConnector jmxConnector = null;
		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(jmxConnect);
			jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
			return getPort(jmxConnector);
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
	}
	
	public String getEnvironment() throws Exception {
		Object result = getActuatorData("org.springframework.boot:type=Endpoint,name=environmentEndpoint", "Data");
		if (result != null) {
			String environment = new ObjectMapper().writeValueAsString(result);
			return environment;
			
		}
		return null;
	}
	
	public String getBeans() throws Exception {
		Object result = getActuatorData("org.springframework.boot:type=Endpoint,name=beansEndpoint", "Data");
		if (result != null) {
			String beans = new ObjectMapper().writeValueAsString(result);
			return beans;
			
		}
		return null;
	}
	
	public String getRequestMappings() throws Exception {
		Object result = getActuatorData("org.springframework.boot:type=Endpoint,name=requestMappingEndpoint", "Data");
		if (result != null) {
			String mappings = new ObjectMapper().writeValueAsString(result);
			return mappings;
			
		}
		return null;
	}
	
	public String getAutoConfigReport() throws Exception {
		Object result = getActuatorData("org.springframework.boot:type=Endpoint,name=autoConfigurationReportEndpoint", "Data");
		if (result != null) {
			String report = new ObjectMapper().writeValueAsString(result);
			return report;
			
		}
		return null;
	}
	
	protected Object getActuatorData(String actuatorID, String attribute) throws Exception {
		String jmxConnect = this.vm.startLocalManagementAgent();

		JMXConnector jmxConnector = null;
		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(jmxConnect);
			jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
			MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
			
			try {
				ObjectName objectName = new ObjectName(actuatorID);
				Object result = connection.getAttribute(objectName, "Data");
				return result;
			}
			catch (InstanceNotFoundException e) {
			}
			return null;
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
	}

	protected boolean contains(String[] cpElements, String element) {
		for (String cpElement : cpElements) {
			if (cpElement.contains(element)) {
				return true;
			}
		}
		return false;
	}

	protected String[] getClasspath(String classpath) {
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

	protected String getPort(JMXConnector jmxConnector) throws Exception {
		MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
		
		String port = getPortViaAdmin(connection);
		if (port != null) {
			return port;
		}
		
		port = getPortViaActuator(connection);
		if (port != null) {
			return port;
		}
		
		port = getPortViaTomcatBean(connection);
		return port;
	}
	
	protected String getPortViaAdmin(MBeanServerConnection connection) throws Exception {
		try {
			String DEFAULT_OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";
			ObjectName objectName = new ObjectName(DEFAULT_OBJECT_NAME);
	
			Object o = connection.invoke(objectName,"getProperty", new String[] {"local.server.port"}, new String[] {String.class.getName()});
			return o.toString();
		}
		catch (InstanceNotFoundException e) {
			return null;
		}
	}

	protected String getPortViaActuator(MBeanServerConnection connection) throws Exception {
		String environment = getEnvironment();
		if (environment != null) {
			JSONObject env = new JSONObject(environment);
			if (env != null) {
				JSONObject portsObject = env.getJSONObject("server.ports");
				if (portsObject != null) {
					String portValue = portsObject.getString("local.server.port");
					return portValue;
				}
			}
		}
		return null;
	}

	protected String getPortViaTomcatBean(MBeanServerConnection connection) throws Exception {
		try {
			Set<ObjectName> queryNames = connection.queryNames(null, null);
			
			for (ObjectName objectName : queryNames) {
				if (objectName.toString().startsWith("Tomcat:type=Connector")) {
					Object result = connection.getAttribute(objectName, "localPort");
					if (result != null) {
						return result.toString();
					}
				}
			}
		}
		catch (InstanceNotFoundException e) {
		}
		
		return null;
	}

}
