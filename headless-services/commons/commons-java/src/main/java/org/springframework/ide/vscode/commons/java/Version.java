/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.util.Objects;

public final class Version implements Comparable<Version> {
	
	private int major;
	private int minor;
	private int patch;
	private String qualifier;
	
	public Version(int major, int minor, int patch, String qualifier) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.qualifier = qualifier;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	public String getQualifier() {
		return qualifier;
	}
	
	public String toMajorMinorVersionStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.');
		sb.append(minor);
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.');
		sb.append(minor);
		sb.append('.');
		sb.append(patch);
		if (qualifier != null) {
			sb.append('.');
			sb.append(qualifier);
		}
		return sb.toString();
	}

	@Override
	public int compareTo(Version o) {
		if (major == o.major) {
			if (minor == o.minor) {
				return patch - o.patch;
			} else {
				return minor - o.minor;
			}
		} else {
			return major - o.major;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch, qualifier);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		return major == other.major && minor == other.minor && patch == other.patch
				&& Objects.equals(qualifier, other.qualifier);
	}

}
