/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

/**
 * @author Martin Lippert
 */
public class NGROKTunnel {

	private String name;
	private String proto;
	private String public_url;
	private String addr;

	public NGROKTunnel(String name, String proto, String public_url, String addr) {
		super();
		this.name = name;
		this.proto = proto;
		this.public_url = public_url;
		this.addr = addr;
	}

	public String getName() {
		return name;
	}

	public String getProto() {
		return proto;
	}

	public String getPublic_url() {
		return public_url;
	}

	public String getAddr() {
		return addr;
	}

	@Override
	public String toString() {
		return "NGROKTunnel [name=" + name + ", proto=" + proto + ", public_url=" + public_url + ", addr=" + addr + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((proto == null) ? 0 : proto.hashCode());
		result = prime * result + ((public_url == null) ? 0 : public_url.hashCode());
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
		NGROKTunnel other = (NGROKTunnel) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (proto == null) {
			if (other.proto != null)
				return false;
		} else if (!proto.equals(other.proto))
			return false;
		if (public_url == null) {
			if (other.public_url != null)
				return false;
		} else if (!public_url.equals(other.public_url))
			return false;
		return true;
	}

}
