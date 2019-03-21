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
package org.springframework.ide.vscode.boot.java.utils;

/**
 * @author Martin Lippert
 */
public class SymbolCacheKey {

	private static final String SEPARATOR = "-";

	private final String primaryIdentifier;
	private final String version;

	public SymbolCacheKey(String primaryIdentifier, String version) {
		this.primaryIdentifier = primaryIdentifier;
		this.version = version;
	}

	public String getPrimaryIdentifier() {
		return primaryIdentifier;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return primaryIdentifier + SEPARATOR + version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((primaryIdentifier == null) ? 0 : primaryIdentifier.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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

		SymbolCacheKey other = (SymbolCacheKey) obj;
		return this.toString().equals(other.toString());
	}

	public static SymbolCacheKey parse(String fileName) {
		if (fileName != null && fileName.length() > 0) {
			int separatorIndex = fileName.lastIndexOf(SEPARATOR);
			if (separatorIndex > 0) {
				String primary = fileName.substring(0, separatorIndex);
				String version = fileName.substring(separatorIndex + 1);

				int fileextensionIndex = version.lastIndexOf(".");
				if (fileextensionIndex > 0) {
					version = version.substring(0, fileextensionIndex);
				}

				return new SymbolCacheKey(primary, version);
			}
		}
		return null;
	}

}
