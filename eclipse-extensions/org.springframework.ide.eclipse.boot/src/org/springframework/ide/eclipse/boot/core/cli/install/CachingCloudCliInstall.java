/*******************************************************************************
 *  Copyright (c) 2017, 2020 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

import org.springframework.ide.eclipse.boot.util.version.Version;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Cloud CLI install that caches the services names and version. Good to use for
 * STS managed installation of Spring Boot/Spring Cloud CLI
 *
 * @author Alex Boyko
 *
 */
class CachingCloudCliInstall extends CloudCliInstall {

	private Supplier<Version> version;
	private Supplier<String[]> services;

	CachingCloudCliInstall(IBootInstall bootInstall) {
		super(bootInstall);
		this.version = Suppliers.memoize(() -> super.getVersion());
		this.services = Suppliers.memoize(() -> super.getCloudServices());
	}

	@Override
	public String[] getCloudServices() {
		return services.get();
	}

	@Override
	public Version getVersion() {
		return version.get();
	}

}
