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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.FuctionWithException;
import org.springframework.ide.vscode.commons.util.FunctionWithException;
import org.springframework.ide.vscode.commons.util.MemoizingDisposableSupplier;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A abstract base class which attempts to capture commonalities between
 * Local and Remote connections to SpringBootApp using JMX.
 */
public abstract class AbstractSpringBootApp implements SpringBootApp {

	private static final String SPRINGFRAMEWORK_BOOT_DOMAIN = "org.springframework.boot";
	protected static Logger logger = LoggerFactory.getLogger(SpringBootApp.class);

	private String jmxMbeanActuatorDomain;

	// NOTE: Gson-based serialisation replaces the old Jackson ObjectMapper. Not sure if this makes a difference in the long run, but to retain the same output that Jackson Object Mapper
	// was generating during serialisation, some configuration in Gson is required, as the default behaviour of Gson is different than Object Mapper.
	// Namely: Object Mapper does not escape Html, whereas Gson does by default (for example
	// '=' in Gson appears as '\u003d')
	protected final Gson gson = new GsonBuilder()
							.disableHtmlEscaping()
							.create();

	protected abstract String getJmxUrl();

	@Override
	public abstract Properties getSystemProperties() throws Exception;

	@Override
	public abstract String getProcessID();
	@Override
	public abstract String getProcessName() throws Exception;
	@Override
	public abstract boolean isSpringBootApp();

	private final MemoizingDisposableSupplier<JMXConnector> jmxConnector = new MemoizingDisposableSupplier<JMXConnector>(
		//creating jmx connector:
		() -> {
			String url = getJmxUrl();
			logger.info("Creating JMX connector: "+url);
			try {
				JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(url), null);
				logger.info("Created JMX connector: {}", connector);
				return connector;
			} catch (Exception e) {
				logger.info("Creating JMX connector failed: {}", ExceptionUtil.getMessage(e));
				throw e;
			}
		},
		//disposing jmx connector:
		(connector) -> {
			try {
				logger.info("Disposing JMX connector: "+connector);
				connector.close();
			} catch (IOException e) {
				//ignore
			}
		}
	);

	protected <T> T withJmxConnector(FunctionWithException<JMXConnector, T> doit) throws Exception {
		try {
			return doit.apply(jmxConnector.get());
		} catch (Exception e) {
			logger.info("Evicting JMX connector {} because of error: {}", getJmxUrl(), ExceptionUtil.getMessage(e));
			jmxConnector.evict();
			throw e;
		}
	}

	@Override
	public void dispose() {
		jmxConnector.dispose();
	}

	@Override
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

	@Override
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

	@Override
	public LiveBeansModel getBeans() {
		try {
			String domain = getDomainForActuator();
			String json = getBeansFromActuator(domain);
			LiveBeansModel beans = LiveBeansModel.parse(json);
			logger.debug("Got {} beans for {}", beans.getBeanNames().size(), this);
			return beans;
		} catch (Exception e) {
			logger.error("Error parsing beans", e);
			return LiveBeansModel.builder().build();
		}
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
			this.jmxMbeanActuatorDomain = withJmxConnector(jmxConnector -> {
				String jmxMbeanActuatorDomain = null;
				MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
				// To be more efficient in finding the domain containing actuator information,
				// and avoid many JMX connections
				// first check the default springframework boot domain:
				String beansJson = getBeansFromActuator(SPRINGFRAMEWORK_BOOT_DOMAIN);
				if (StringUtil.hasText(beansJson)) {
					jmxMbeanActuatorDomain = SPRINGFRAMEWORK_BOOT_DOMAIN;
				}

				if (jmxMbeanActuatorDomain == null) {
					String[] domains = connection.getDomains();
					if (domains != null) {
						for (String domain : domains) {
							// we already checked default boot domain, no need to check it again
							// Note that default spring boot domain may still appear even if another
							// domain contains actuator Beans (for example, "Admin" will be under default spring framework domain)
							if (!SPRINGFRAMEWORK_BOOT_DOMAIN.equals(domain)) {
								beansJson = getBeansFromActuator(domain);
								if (StringUtil.hasText(beansJson)) {
									jmxMbeanActuatorDomain = domain;
									break;
								}
							}
						}
					}
				}
				return jmxMbeanActuatorDomain;
			});
		}
		return this.jmxMbeanActuatorDomain;
	}

	protected <T extends PlatformManagedObject,R> R withPlatformMxBean(Class<T> mbeanType, FuctionWithException<T,R> doit) throws Exception {
		return withJmxConnector(jmxConnector -> {
			MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
			T proxy = ManagementFactory.getPlatformMXBean(connection, mbeanType);
			return doit.apply(proxy);
		});
	}

	protected Object getActuatorDataFromAttribute(ObjectName objectName, String attribute) throws Exception {
		if (objectName != null) {
			return withJmxConnector(jmxConnector -> {
				try {
					MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
					return connection.getAttribute(objectName, "Data");
				} catch (InstanceNotFoundException e) {
					return null;
				}
			});
		}
		return null;
	}

	protected Object getActuatorDataFromOperation(ObjectName objectName, String operation) throws Exception {
		if (objectName != null) {
			return withJmxConnector(jmxConnector -> {
				try {
					MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
					return connection.invoke(objectName, operation, null, null);
				} catch (InstanceNotFoundException e) {
					return null;
				}
			});
		}
		return null;
	}

	@Override
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

	@Override
	public String[] getClasspath() throws Exception {
		Properties props = getSystemProperties();
		String classpath = (String) props.get("java.class.path");
		String[] cpElements = splitClasspath(classpath);
		return cpElements;
	}

	private String[] splitClasspath(String classpath) {
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

	@Override
	public String getJavaCommand() throws Exception {
		Properties props = getSystemProperties();
		return (String) props.get("sun.java.command");
	}

	@Override
	public String getHost() throws Exception {
		//TODO: different implementation for cf apps with locally tunnelled
		// jmx connection?
		try {
			JMXServiceURL serviceUrl = new JMXServiceURL(getJmxUrl());
			return serviceUrl.getHost();
		} catch (Exception e) {
			return "Unknown host";
		}
	}

	@Override
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

	@Override
	public String getPort() throws Exception {
		return withJmxConnector(jmxConnector -> {
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
		});
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

}
