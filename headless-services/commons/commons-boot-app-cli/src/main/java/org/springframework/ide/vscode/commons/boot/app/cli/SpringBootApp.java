/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.Boot1xRequestMapping;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMappingsParser20;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
public class SpringBootApp {

	private static final String SPRINGFRAMEWORK_BOOT_DOMAIN = "org.springframework.boot";

	private Logger logger = LoggerFactory.getLogger(SpringBootApp.class);

	private VirtualMachine vm;
	private VirtualMachineDescriptor vmd;

	private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private Boolean isSpringBootApp;
	private String jmxMbeanActuatorDomain;

	// NOTE: Gson-based serialisation replaces the old Jackson ObjectMapper. Not sure if this makes a difference in the long run, but to retain the same output that Jackson Object Mapper
	// was generating during serialisatino, some configuration in Gson is required, as the default behaviour of Gson is different than Object Mapper.
	// Namely: Object Mapper does not escape Html, whereas Gson does by default (for example
	// '=' in Gson appears as '\u003d')
	private Gson gson = new GsonBuilder()
							.disableHtmlEscaping()
							.create();


	private final Supplier<String> jmxConnect = Suppliers.memoize(() -> {
		String address = null;
		try {
			address = vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS);
		} catch (Exception e) {
			//ignore
		}
		if (address==null) {
			try {
				address = vm.startLocalManagementAgent();
			} catch (IOException e) {
				logger.error("Error starting local management agent", e);
			}
		}
		return address;
	});

	private static SpringBootAppCache cache = new SpringBootAppCache();

	public static Collection<SpringBootApp> getAllRunningJavaApps() throws Exception {
		return cache.getAllRunningJavaApps();
	}
	/**
	 * @return Map that contains the boot apps, mapping the process ID -> boot app accessor object
	 */
	public static Collection<SpringBootApp> getAllRunningSpringApps() throws Exception {
		return getAllRunningJavaApps().stream().filter(SpringBootApp::isSpringBootApp).collect(CollectorUtil.toImmutableList());
	}

	public SpringBootApp(VirtualMachineDescriptor vmd) throws AttachNotSupportedException, IOException {
		this.vmd = vmd;
		this.vm = VirtualMachine.attach(vmd);
		logger.info("SpringBootApp created: "+this);
	}

	public String getProcessID() {
		return vmd.id();
	}

	public String getProcessName() {
		return vmd.displayName();
	}

	public String getHost() throws Exception {
		JMXServiceURL serviceUrl = new JMXServiceURL(jmxConnect.get());
		return serviceUrl.getHost();
	}

	public boolean isSpringBootApp() {
		if (isSpringBootApp==null) {
			try {
				isSpringBootApp = !containsSystemProperty("sts4.languageserver.name")
					&& (
							isSpringBootAppClasspath() ||
							isSpringBootAppSysprops()
					);
			} catch (Exception e) {
				//Couldn't determine if the VM is a spring boot app. Could be it already died. Or could be its not accessible (yet).
				// We will ignore the exception, pretend its not a boot app (most likely isn't) but DO NOT CACHE this result
				// so it will be retried again on the next polling loop.
				return false;
			}
		}
		return isSpringBootApp;
	}

	private boolean isSpringBootAppSysprops() throws IOException {
		Properties sysprops = this.vm.getSystemProperties();
		return "org.springframework.boot.loader".equals(sysprops.getProperty("java.protocol.handler.pkgs"));
	}

	private boolean isSpringBootAppClasspath() throws IOException {
		return contains(getClasspath(), "spring-boot");
	}

	public String[] getClasspath() throws IOException {
		Properties props = this.vm.getSystemProperties();
		String classpath = (String) props.get("java.class.path");
		String[] cpElements = splitClasspath(classpath);
		return cpElements;
	}

	public String getJavaCommand() throws IOException {
		Properties props = this.vm.getSystemProperties();
		if (props.contains("sun.java.command")) {
			return props.getProperty("sun.java.command");
		}
		else {
			return null;
		}
	}

	public boolean containsSystemProperty(Object key) throws IOException {
		Properties props = this.vm.getSystemProperties();
		return props.containsKey(key);
	}

	public String getPort() throws Exception {
		JMXConnector jmxConnector = null;
		try {
			jmxConnector = getJmxConnector();
			return getPort(jmxConnector);
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
	}

	public String getEnvironment() throws Exception {
		Object result = getActuatorDataFromAttribute(getObjectName("type=Endpoint,name=environmentEndpoint"), "Data");
		if (result != null) {
			String environment = gson.toJson(result);
			return environment;
		}

		result = getActuatorDataFromOperation(getObjectName("type=Endpoint,name=Env"), "environment");
		if (result != null) {
			String environment = gson.toJson(result);
			return environment;
		}

		return null;
	}

	private String getBeansFromActuator(String domain) throws Exception {
		Object result = getActuatorDataFromAttribute(getObjectName(domain, "type=Endpoint,name=beansEndpoint"), "Data");
		if (result != null) {
			String beans = gson.toJson(result);
			return beans;
		}

		result = getActuatorDataFromOperation(getObjectName(domain, "type=Endpoint,name=Beans"), "beans");
		if (result != null) {
			String beans = gson.toJson(result);
			return beans;
		}

		return null;
	}

	public LiveBeansModel getBeans() {
		try {
			String domain = getDomainForActuator();
			String json = getBeansFromActuator(domain);
			return LiveBeansModel.parse(json);
		} catch (Exception e) {
			logger.error("Error parsing beans", e);
			return LiveBeansModel.builder().build();
		}
	}

	public static Collection<RequestMapping> parseRequestMappingsJson(String json, String bootVersion) {
		JSONObject obj = new JSONObject(json);
		if (bootVersion.equals("2.x")) {
			return RequestMappingsParser20.parse(obj);
		} else { //1.x
			List<RequestMapping> result = new ArrayList<>();
			Iterator<String> keys = obj.keys();
			while (keys.hasNext()) {
				String rawKey = keys.next();
				JSONObject value = obj.getJSONObject(rawKey);
				result.add(new Boot1xRequestMapping(rawKey, value));
			}
			return result;
		}
	}

	public Collection<RequestMapping> getRequestMappings() throws Exception {
		//Boot 1.x
		Object result = getActuatorDataFromAttribute(getObjectName("type=Endpoint,name=requestMappingEndpoint"), "Data");
		if (result != null) {
			String mappings = gson.toJson(result);
			return parseRequestMappingsJson(mappings, "1.x");
		}

		//Boot 2.x
		result = getActuatorDataFromOperation(getObjectName("type=Endpoint,name=Mappings"), "mappings");
		if (result != null) {
			String mappings = gson.toJson(result);
			return parseRequestMappingsJson(mappings, "2.x");
		}

		return null;
	}

	public Optional<List<LiveConditional>> getLiveConditionals() throws Exception {
		return getLiveConditionals(getAutoConfigReport(), getProcessID(), getProcessName());
	}

	/**
	 * Publicly visible so that it can be tested via a mock app
	 *
	 * @param autoConfigReport
	 * @param processId
	 * @param processName
	 * @return
	 * @throws Exception
	 */
	public static Optional<List<LiveConditional>> getLiveConditionals(String autoConfigReport, String processId,
			String processName) {
		return LiveConditionalParser.parse(autoConfigReport, processId, processName);
	}

	private String getAutoConfigReport() throws Exception {
		//Boot 1.x
		Object result = getActuatorDataFromAttribute(getObjectName("type=Endpoint,name=autoConfigurationReportEndpoint"), "Data");
		if (result != null) {
			String report = gson.toJson(result);
			return report;
		}

		//Boot 2.x
		result = getActuatorDataFromOperation(getObjectName("type=Endpoint,name=Conditions"), "applicationConditionEvaluation");
		if (result != null) {
			String report = gson.toJson(result);
			return report;
		}

		return null;
	}

	protected Object getActuatorDataFromAttribute(ObjectName objectName, String attribute) throws Exception {
		JMXConnector jmxConnector = null;
		try {
			if (objectName != null) {
				jmxConnector = getJmxConnector();
				MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

				try {
					Object result = connection.getAttribute(objectName, "Data");
					return result;
				}
				catch (InstanceNotFoundException e) {
				}
			}
			return null;
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
	}

	protected Object getActuatorDataFromOperation(ObjectName objectName, String operation) throws Exception {
		JMXConnector jmxConnector = null;
		try {
			if (objectName != null) {
				jmxConnector = getJmxConnector();
				MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

				try {
					Object result = connection.invoke(objectName, operation, null, null);
					return result;
				}
				catch (InstanceNotFoundException e) {
				}
			}
			return null;
		}
		finally {
			if (jmxConnector != null) jmxConnector.close();
		}
	}
	protected JMXConnector getJmxConnector() throws MalformedURLException, IOException {
		JMXServiceURL serviceUrl = new JMXServiceURL(jmxConnect.get());
		JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
		return jmxConnector;
	}

	protected boolean contains(String[] cpElements, String element) {
		for (String cpElement : cpElements) {
			if (cpElement.contains(element)) {
				return true;
			}
		}
		return false;
	}

	protected String[] splitClasspath(String classpath) {
		List<String> classpathElements = new ArrayList<>();
		if (classpath != null) {
			StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
			while (tokenizer.hasMoreTokens()) {
				String classpathElement = tokenizer.nextToken();
				classpathElements.add(classpathElement);
			}
		}
		return classpathElements.toArray(new String[classpathElements.size()]);
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
				JSONObject portsObject = env.optJSONObject("server.ports");
				if (portsObject != null) {
					String portValue = portsObject.optString("local.server.port");
					if (portValue!=null) {
						return portValue;
					}
				}
				//Not found as direct property value... in Boot 2.0 we must look inside the 'propertySources'.
				//Similar... but structure is more complex.
				JSONArray propertySources = env.optJSONArray("propertySources");
				if (propertySources!=null) {
					for (Object _source : propertySources) {
						if (_source instanceof JSONObject) {
							JSONObject source = (JSONObject) _source;
							String sourceName = source.optString("name");
							if ("server.ports".equals(sourceName)) {
								JSONObject props = source.optJSONObject("properties");
								JSONObject valueObject = props.optJSONObject("local.server.port");
								if (valueObject!=null) {
									String portValue = valueObject.optString("value");
									if (portValue!=null) {
										return portValue;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	protected String getPortViaTomcatBean(MBeanServerConnection connection) throws Exception {
		try {
			Set<ObjectName> queryNames = connection.queryNames(null, null);

			for (ObjectName objectName : queryNames) {
				if (objectName.toString().startsWith("Tomcat") && objectName.toString().contains("type=Connector")) {
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

	protected ObjectName getObjectName(String keyProperties) throws Exception {
		String domain = getDomainForActuator();
		return getObjectName(domain, keyProperties);
	}

	protected ObjectName getObjectName(String domain, String keyProperties) throws Exception {
		if (StringUtil.hasText(domain)  && StringUtil.hasText(keyProperties)) {
			String fullName = domain + ":" + keyProperties;
			return ObjectName.getInstance(fullName);
		}
		return null;
	}

	/**
	 * PT 156072399: Actuator information can be defined using a different JMX MBean domain.
	 * By default, Spring Boot exposes management endpoints as JMX MBeans under the 'org.springframework.boot' domain.
	 * Users can however define another domain in the app's application.properties, for example using this property:
	 * management.endpoints.jmx.domain=com.example.myapp
	 * <b/>
	 * Therefore we need to support other domains than just: 'org.springframework.boot'
	 * @return JMX MBean domain containing actuator information, or null if not resolved.
	 * @throws Exception when resolving domain from JMX
	 */
	protected String getDomainForActuator() throws Exception {
		if (this.jmxMbeanActuatorDomain == null) {
			JMXConnector jmxConnector = null;
			try {
				jmxConnector = getJmxConnector();
				MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
				// To be more efficient in finding the domain containing actuator information,
				// and avoid many JMX connections
				// first check the default springframework boot domain:
				String beansJson = getBeansFromActuator(SPRINGFRAMEWORK_BOOT_DOMAIN);
				if (StringUtil.hasText(beansJson)) {
					this.jmxMbeanActuatorDomain = SPRINGFRAMEWORK_BOOT_DOMAIN;
				}

				if (this.jmxMbeanActuatorDomain == null) {
					String[] domains = connection.getDomains();
					if (domains != null) {
						for (String domain : domains) {
							// we already checked default boot domain, no need to check it again
							// Note that default spring boot domain may still appear even if another
							// domain contains actuator Beans (for example, "Admin" will be under default spring framework domain)
							if (!SPRINGFRAMEWORK_BOOT_DOMAIN.equals(domain)) {
								beansJson = getBeansFromActuator(domain);
								if (StringUtil.hasText(beansJson)) {
									this.jmxMbeanActuatorDomain = domain;
									break;
								}
							}
						}
					}
				}
			} finally {
				if (jmxConnector != null) {
					jmxConnector.close();
				}
			}
		}
		return this.jmxMbeanActuatorDomain;
	}

	@Override
	public String toString() {
		return "Process [id=" +getProcessID() + ", name=`"+getProcessName()+"`]";
	}

	/**
	 * For testing / investigation purposes. Dumps out as much information as possible
	 * that can be onbtained from the jvm, without accessing JMX.
	 */
	public void dumpJvmInfo() throws IOException {
		System.out.println("--- vm infos ----");
		System.out.println("id = "+vm.id());
		System.out.println("displayName = "+vmd.displayName());
		dump("agentProperties", vm.getAgentProperties());
		dump("systemProps", vm.getSystemProperties());
		System.out.println("-----------------");
	}

	private void dump(String name, Properties props) {
		System.out.println(name + " = {");
		for (Entry<Object, Object> prop : props.entrySet()) {
			System.out.println("  "+prop.getKey()+" = "+prop.getValue());
		}
		System.out.println("}");
	}

	public List<String> getActiveProfiles() {
		try {
			String _env = getEnvironment();
			if (_env != null) {
				JSONObject env = new JSONObject(_env);
				Object _profiles = env.opt("activeProfiles"); //Boot 2.0
				if (_profiles==null) {
					_profiles = env.opt("profiles"); //Boot 1.5
				}
				if (_profiles instanceof JSONArray) {
					JSONArray profiles = (JSONArray) _profiles;
					ImmutableList.Builder<String> list = ImmutableList.builder();
					for (Object object : profiles) {
						if (object instanceof String) {
							list.add((String) object);
						}
					}
					return list.build();
				}
			}
		} catch (Exception e) {
			logger.error("error resolving profiles from env", e);
		}
		return null;
	}

	public void dispose() {
		if (vm!=null) {
			logger.info("SpringBootApp disposed: "+this);
			try {
				vm.detach();
			} catch (Exception e) {
			}
			vm = null;
		}
		if (vmd!=null) {
			vmd = null;
		}
	}


}
