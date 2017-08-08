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
package org.springframework.ide.vscode.bosh;

import org.springframework.ide.vscode.bosh.models.BoshModels;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

public class BoshCloudConfigSchema extends SchemaSupport implements YamlSchema {

	private YBeanType toplevelType;
	private YType t_any;

	public BoshCloudConfigSchema(YTypeFactory f, BoshModels models) {
		super(f);
		t_any = f.yany("Object");

		this.toplevelType = f.ybean("CloudConfig");
		addProp(toplevelType, "azs", t_any).isRequired(true);
		addProp(toplevelType, "networks", t_any).isRequired(true);
		addProp(toplevelType, "vm_types", t_any).isRequired(true);
		addProp(toplevelType, "vm_extensions", t_any);
		addProp(toplevelType, "disk_types", t_any).isRequired(true);
		addProp(toplevelType, "compilation", t_any).isRequired(true);
	}

	@Override
	public YType getTopLevelType() {
		return toplevelType;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return f.TYPE_UTIL;
	}

	@Override
	protected String getResourcePathPrefix() {
		return "/cloud-config/";
	}
}
