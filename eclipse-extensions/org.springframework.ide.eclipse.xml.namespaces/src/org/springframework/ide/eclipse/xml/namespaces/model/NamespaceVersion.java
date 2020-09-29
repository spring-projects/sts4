/*******************************************************************************
 * Copyright (c) 2011, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.model;

import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;

public class NamespaceVersion implements Comparable<NamespaceVersion> {

	private static final String MINIMUM_VERSION_STRING = "0";

	public static final NamespaceVersion MINIMUM_VERSION = new NamespaceVersion(MINIMUM_VERSION_STRING);

	private final org.osgi.framework.Version version;

	public NamespaceVersion(String v) {
		org.osgi.framework.Version tempVersion = null;
		try {
			tempVersion = org.osgi.framework.Version.parseVersion(v);
		}
		catch (Exception e) {
			// make sure that we don't crash on any new version numbers format that we don't support
			SpringXmlNamespacesPlugin.log("Cannot convert schema vesion", e);
			tempVersion = org.osgi.framework.Version.parseVersion(MINIMUM_VERSION_STRING);
		}
		this.version = tempVersion;
	}

	public int compareTo(NamespaceVersion v2) {
		return this.version.compareTo(v2.version);
	}
}