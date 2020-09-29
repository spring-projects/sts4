/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.client.v2;

import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class CFRoute {

	public static final int NO_PORT = -1;
	public static final String EMPTY_ROUTE = "";

	final private String domain;
	final private String host;
	final private String path;
	final private int port;
	final private String fullRoute;

	CFRoute(String domain, String host, String path, int port, String fullRoute)  {
		super();
		this.domain = domain;
		this.host = host;
		this.path = path;
		this.port = port;
		this.fullRoute = fullRoute;
	}

	public String getDomain() {
		return domain;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}

	public int getPort() {
		return port;
	}

	public String getRoute() {
		return fullRoute;
	}

	public void validate() throws Exception {
		if (!StringUtil.hasText(getDomain())) {
			throw ExceptionUtil.coreException("Invalid route: No domain set.");
		}
	}

	@Override
	public String toString() {
		return "CFRoute [domain=" + domain + ", host=" + host + ", path=" + path + ", port=" + port  +"]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((fullRoute == null) ? 0 : fullRoute.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CFRoute other = (CFRoute) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (fullRoute == null) {
			if (other.fullRoute != null)
				return false;
		} else if (!fullRoute.equals(other.fullRoute))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	public static CFRouteBuilder builder() {
		return new CFRouteBuilder();
	}
}
