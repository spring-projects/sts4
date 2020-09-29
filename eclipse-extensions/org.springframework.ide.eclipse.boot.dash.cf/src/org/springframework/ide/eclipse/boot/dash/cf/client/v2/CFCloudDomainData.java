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
package org.springframework.ide.eclipse.boot.dash.cf.client.v2;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;

public final class CFCloudDomainData implements CFCloudDomain {
	private final String name;
	private final CFDomainType type;
	private final CFDomainStatus status;

	public CFCloudDomainData(String name, CFDomainType type, CFDomainStatus status) {
		super();
		this.name = name;
		this.type = type;
		this.status = status;
	}

	public CFCloudDomainData(String name) {
		this(name, CFDomainType.HTTP, CFDomainStatus.SHARED);
	}

	@Override
	public CFDomainType getType() {
		return type;
	}

	@Override
	public CFDomainStatus getStatus() {
		return status;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "CFCloudDomainData [name=" + name + ", type=" + type + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CFCloudDomainData other = (CFCloudDomainData) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (status != other.status)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}