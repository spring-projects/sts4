package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.management.remote.JMXConnector;

import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;

public interface SpringBootApp {

	String[] getClasspath() throws Exception;
	String getJavaCommand() throws Exception;
	String getProcessName() throws Exception;
	String getProcessID();
	String getHost() throws Exception;
	String getPort() throws Exception;
	boolean isSpringBootApp();

	String getEnvironment() throws Exception;
	Collection<RequestMapping> getRequestMappings() throws Exception;
	LiveBeansModel getBeans();
	List<String> getActiveProfiles();
	Optional<List<LiveConditional>> getLiveConditionals() throws Exception;
	Properties getSystemProperties() throws Exception;
	JMXConnector getJmxConnector() throws MalformedURLException, IOException;

}
