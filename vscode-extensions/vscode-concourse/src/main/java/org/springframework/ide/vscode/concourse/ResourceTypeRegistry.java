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

	private static class ResourceTypeInfo {
		private final YBeanType source;
		private final YBeanType in;
		private final YBeanType out;

		public ResourceTypeInfo(YBeanType source, YBeanType in, YBeanType out) {
			super();
			this.source = source;
			this.in = in;
			this.out = out;
		}

		public YBeanType getSource() {
			return source;
		}

		public YBeanType getIn() {
			return in;
		}

		public YBeanType getOut() {
			return out;
		}
	}

	private Map<String, ResourceTypeInfo> resourceTypes = new HashMap<>();


	public ResourceTypeRegistry() {
	}

	public void def(String resourceTypeName, YBeanType source, YBeanType in, YBeanType out) {
		Assert.isLegal(!resourceTypes.containsKey(resourceTypeName), "Multiple definitions for '"+resourceTypeName+"'");
		resourceTypes.put(resourceTypeName, new ResourceTypeInfo(source, in, out));
	}

	public YType getSourceType(String typeTag) {
		if (typeTag!=null) {
			ResourceTypeInfo v = resourceTypes.get(typeTag);
			if (v!=null) {
				return v.getSource();
			}
		}
		return null;
	}

	public YType getInParamsType(String typeTag) {
		if (typeTag!=null) {
			ResourceTypeInfo v = resourceTypes.get(typeTag);
			if (v!=null) {
				return v.getIn();
			}
		}
		return null;
	}

}
