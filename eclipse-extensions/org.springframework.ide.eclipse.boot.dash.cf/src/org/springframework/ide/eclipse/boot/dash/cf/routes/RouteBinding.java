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

public class RouteBinding {

	private Randomized<String> host;
	private String domain;
	private Randomized<Integer> port;
	private String path;

	public Randomized<String> getHost() {
		return host;
	}

	public void setHost(String host) {
		setHost(host==null ? null : Randomized.value(host));
	}
	public void setHost(Randomized<String> host) {
		this.host = host;
	}

	public Randomized<Integer> getPort() {
		return port;
	}
	public void setPort(Randomized<Integer> port) {
		this.port = port;
	}
	public void setPort(Integer port) {
		setPort(port == null ? null : Randomized.value(port));
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPath() {
		return path;
	}

	/**
	 * This toString returns something that is not quite a uri. The '.' separating host from
	 * domain is replaced by '@'. This is so we can actually see where the host ends and the
	 * domain name starts (which you can't in a uri, since host and domains may both contain
	 * dots.
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (host!=null) {
			s.append(getHost());
			s.append('@');
		}
		s.append(getDomain());
		if (port!=null) {
			s.append(':');
			s.append(getPort());
		}
		if (path!=null) {
			s.append(getPath());
		}
		return s.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		RouteBinding other = (RouteBinding) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	public String toUri() {
		StringBuilder s = new StringBuilder();
		if (host!=null) {
			s.append(host);
			s.append('.');
		}
		s.append(domain);
		if (port!=null) {
			s.append(':');
			s.append(port);
		}
		if (path!=null) {
			if (!path.startsWith("/")) {
				s.append('/');
			}
			s.append(path);
		}
		return s.toString();
	}

	public void setPath(String path) {
		this.path = path;
	}
}
