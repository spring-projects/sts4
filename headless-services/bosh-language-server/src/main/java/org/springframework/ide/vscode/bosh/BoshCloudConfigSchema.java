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
import org.springframework.ide.vscode.commons.util.ValueParsers;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

public class BoshCloudConfigSchema extends SchemaSupport implements YamlSchema {

	private final YBeanType toplevelType;
	private final YType t_az_ref;
	private final YType t_vm_type_ref;
	private final YType t_network_ref;

	public BoshCloudConfigSchema(YTypeFactory f, BoshModels models) {
		super(f);

		AbstractType t_any = f.yany("Object");
		AbstractType t_ne_string = f.yatomic("String")
			.parseWith(ValueParsers.NE_STRING);
		YAtomicType t_boolean = f.yenum("boolean", "true", "false");

		t_az_ref = t_ne_string;
		t_vm_type_ref = t_ne_string;
		t_network_ref = t_ne_string;

		AbstractType t_pos_integer = f.yatomic("Positive Integer")
				.parseWith(ValueParsers.POS_INTEGER);

		YBeanType t_compilation = f.ybean("Compilation");
		addProp(t_compilation, "workers", t_pos_integer).isRequired(true);
		addProp(t_compilation, "reuse_compilation_vms", t_boolean);
		addProp(t_compilation, "az", t_az_ref).isRequired(true);
		addProp(t_compilation, "vm_type", t_vm_type_ref).isRequired(true);
		addProp(t_compilation, "network", t_network_ref).isRequired(true);

		this.toplevelType = f.ybean("CloudConfig");
		addProp(toplevelType, "azs", t_any).isRequired(true);
		addProp(toplevelType, "networks", t_any).isRequired(true);
		addProp(toplevelType, "vm_types", t_any).isRequired(true);
		addProp(toplevelType, "vm_extensions", t_any);
		addProp(toplevelType, "disk_types", t_any).isRequired(true);
		addProp(toplevelType, "compilation", t_compilation).isRequired(true);
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
