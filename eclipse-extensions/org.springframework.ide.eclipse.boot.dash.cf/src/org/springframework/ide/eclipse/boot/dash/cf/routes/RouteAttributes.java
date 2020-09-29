/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.routes;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.cf.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;

/**
 * Data object containing attributes from a manifest.yml related to route bindings.
 * (i.e. any attribute in the manifest.yml file that potentially affects the routes
 * that will be bound to an app on push, is included here.
 * <p>
 * This is intented to be a 'dumb' data object. It should have no smarts around interpreting the
 * meanings of attributes. Its only purpose is to be a container for
 * exactly the values as found in the manifest.mf file.
 *
 * @author Kris De Volder
 */
public class RouteAttributes {

	private final String appName;
	private final List<String> routes;
	private final String host;
	private final List<String> hosts;
	private final String domain;
	private final List<String> domains;
	private final boolean noHost;
	private final boolean noRoute;
	private final boolean randomRoute;

	public RouteAttributes(YamlGraphDeploymentProperties manifest) {
		this.appName = manifest.getAppName();
		this.routes = manifest.getRoutes();
		this.domain = manifest.getRawDomain();
		this.domains = manifest.getRawDomains();
		this.host = manifest.getRawHost();
		this.hosts = manifest.getRawHosts();
		this.noRoute = manifest.getRawNoRoute();
		this.randomRoute = manifest.getRawRandomRoute();
		this.noHost = manifest.getRawNoHost();
	}

	public List<String> getRoutes() {
		return routes;
	}
	public String getHost() {
		return host;
	}
	public List<String> getHosts() {
		return hosts;
	}
	public String getDomain() {
		return domain;
	}
	public List<String> getDomains() {
		return domains;
	}
	public boolean isNoHost() {
		return noHost;
	}
	public boolean isNoRoute() {
		return noRoute;
	}
	public boolean isRandomRoute() {
		return randomRoute;
	}
	public String getAppName() {
		return appName;
	}
}
