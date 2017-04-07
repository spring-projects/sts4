/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import java.util.Collection;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.util.FuzzyMap;

public class SpringPropertyIndex extends FuzzyMap<ConfigurationMetadataProperty> {
	
	public SpringPropertyIndex(IClasspath projectPath) {
		if (projectPath!=null) {
			PropertiesLoader loader = new PropertiesLoader();
			ConfigurationMetadataRepository metadata = loader.load(projectPath);
			Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
			for (ConfigurationMetadataProperty item : allEntries) {
				add(item);
			}
		}
	}

	@Override
	protected String getKey(ConfigurationMetadataProperty entry) {
		return entry.getId();
	}
	
}
