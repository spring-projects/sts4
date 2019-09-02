/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

/**
 * @author Martin Lippert
 */
public class SpringProcessLiveData {
	
	private final String processName;
	private final String processID;
	
	private final String contextPath;
	private final String urlScheme;
	private final String port;
	private final String host;

	private final LiveBeansModel beansModel;
	private final String[] activeProfiles;
	private final LiveRequestMapping[] requestMappings;
	private final LiveConditional[] conditionals;
	private final LiveProperties properties;
	
	public SpringProcessLiveData(String processName, String processID, String contextPath, String urlScheme,
			String port, String host, LiveBeansModel beansModel, String[] activeProfiles,
			LiveRequestMapping[] requestMappings, LiveConditional[] conditionals, LiveProperties properties) {
		super();
		this.processName = processName;
		this.processID = processID;
		this.contextPath = contextPath;
		this.urlScheme = urlScheme;
		this.port = port;
		this.host = host;
		this.beansModel = beansModel;
		this.activeProfiles = activeProfiles;
		this.requestMappings = requestMappings;
		this.conditionals = conditionals;
		this.properties = properties;
	}

	public String getProcessName() {
		return this.processName;
	}

	public String getProcessID() {
		return this.processID;
	}

	public String getContextPath() {
		return this.contextPath;
	}

	public String getUrlScheme() {
		return this.urlScheme;
	}

	public String getPort() {
		return this.port;
	}

	public String getHost() {
		return this.host;
	}

	public LiveBeansModel getBeans() {
		return this.beansModel;
	}

	public String[] getActiveProfiles() {
		return this.activeProfiles;
	}

	public LiveRequestMapping[] getRequestMappings() {
		return this.requestMappings;
	}

	public LiveConditional[] getLiveConditionals() {
		return this.conditionals;
	}

	public LiveProperties getLiveProperties() {
		return this.properties;
	}

}
