/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;

/**
 * Keeps track of known resource types. For now this only keeps track of the resource-types that
 * are built-in to concourse.
 * 
 * @author Kris De Volder
 */
public class ResourceTypeRegistry {

	private Map<String, YType> sourceTypes = new HashMap<>();

	public ResourceTypeRegistry() {
	}

	public void def(String resourceTypeName, YBeanType sourceType) {
		Assert.isLegal(!sourceTypes.containsKey(resourceTypeName), "Multiple definitions for '"+resourceTypeName+"'");
		sourceTypes.put(resourceTypeName, sourceType);
	}

	public YType getSourceType(String typeTag) {
		return sourceTypes.get(typeTag);
	}

}
