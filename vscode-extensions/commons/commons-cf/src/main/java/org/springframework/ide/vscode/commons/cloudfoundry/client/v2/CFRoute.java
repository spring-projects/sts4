/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import org.cloudfoundry.operations.routes.Route;

public class CFRoute {

	final private String domain;
	final private String host;
	final private String path;

	public CFRoute(Route route) {
		this.domain = route.getDomain();
		this.host = route.getHost();
		this.path = route.getPath();
	}

	public CFRoute(String domain, String host, String path) {
		super();
		this.domain = domain;
		this.host = host;
		this.path = path;
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

	@Override
	public String toString() {
		return "CFRoute [domain=" + domain + ", host=" + host + ", path=" + path + "]";
	}

	public static CFRoute.Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private String domain;
		private String host;
		private String path = "";

		public CFRoute build() {
			return new CFRoute(this.domain, this.host, this.path);
		}

		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}
	}
}
