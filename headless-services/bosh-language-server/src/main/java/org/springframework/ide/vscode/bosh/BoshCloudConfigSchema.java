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

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ide.vscode.bosh.models.BoshModels;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ValueParsers;
import org.springframework.ide.vscode.commons.yaml.reconcile.ASTTypeCache;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.constraints.Constraints;

import com.google.common.collect.ImmutableList;

import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

public class BoshCloudConfigSchema extends SchemaSupport implements YamlSchema {

	private final YBeanType toplevelType;
	private final YType t_az_ref;
	private final YType t_vm_type_ref;
	private final YType t_vm_type_def;
	private final YType t_network_ref;
	private Collection<YType> definitionTypes;
	private Collection<Pair<YType, YType>> defAndRefTypes;

	public BoshCloudConfigSchema(YTypeFactory f, BoshModels models) {
		super(f);

		AbstractType t_any = f.yany("Object");
		AbstractType t_ne_string = f.yatomic("String")
			.parseWith(ValueParsers.NE_STRING);
		YAtomicType t_boolean = f.yenum("boolean", "true", "false");
		AbstractType t_pos_integer = f.yatomic("Positive Integer")
				.parseWith(ValueParsers.POS_INTEGER);
		YType t_params = f.ymap(t_ne_string, t_any);

		t_az_ref = t_ne_string;
		t_vm_type_ref = t_ne_string;
		t_vm_type_def = f.yatomic("VMTypeName").parseWith(ValueParsers.NE_STRING);
		t_network_ref = t_ne_string;

		YBeanType t_compilation = f.ybean("Compilation");
		addProp(t_compilation, "workers", t_pos_integer).isRequired(true);
		addProp(t_compilation, "reuse_compilation_vms", t_boolean);
		addProp(t_compilation, "az", t_az_ref).isRequired(true);
		addProp(t_compilation, "vm_type", t_vm_type_ref).isRequired(true);
		addProp(t_compilation, "network", t_network_ref).isRequired(true);

		YBeanType t_vm_type = f.ybean("VMType");
		addProp(t_vm_type, "name", t_vm_type_def).isPrimary(true);
		addProp(t_vm_type, "cloud_properties", t_params);

		this.toplevelType = f.ybean("CloudConfig");
		addProp(toplevelType, "azs", t_any).isRequired(true);
		addProp(toplevelType, "networks", t_any).isRequired(true);
		addProp(toplevelType, "vm_types", f.yseq(t_vm_type)).isRequired(true);
		addProp(toplevelType, "vm_extensions", t_any);
		addProp(toplevelType, "disk_types", t_any).isRequired(true);
		addProp(toplevelType, "compilation", t_compilation).isRequired(true);

		ASTTypeCache astTypes = models.astTypes;
		for (YType defType : getDefinitionTypes()) {
			toplevelType.require(Constraints.uniqueDefinition(astTypes, defType, YamlSchemaProblems.problemType("BOSH_CC_DUPLICATE_"+defType)));
		}

	}

	/**
	 * @return Pairs of types. Each pair contains a 'def' type and a 'ref' type. Nodes with the ref-type
	 * shall be interpreted as reference to a corresponding node with the 'def' type if the def and ref node
	 * contain the same scalar value.
	 */
	public Collection<Pair<YType, YType>> getDefAndRefTypes() {
		if (defAndRefTypes==null) {
			defAndRefTypes = ImmutableList.of(
					Pair.of(t_vm_type_def, t_vm_type_ref)
			);
		}
		return defAndRefTypes;
	}

	public Collection<YType> getDefinitionTypes() {
		if (definitionTypes==null) {
			definitionTypes = getDefAndRefTypes().stream()
					.map(pair -> pair.getLeft())
					.collect(CollectorUtil.toImmutableList());
		}
		return definitionTypes;
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
