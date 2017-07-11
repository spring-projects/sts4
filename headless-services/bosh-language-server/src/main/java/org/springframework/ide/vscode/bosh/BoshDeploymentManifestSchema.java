/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import java.util.UUID;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.ValueParser;
import org.springframework.ide.vscode.commons.util.ValueParsers;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YTypedPropertyImpl;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public class BoshDeploymentManifestSchema implements YamlSchema {

	private final AbstractType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	public final YTypeFactory f = new YTypeFactory()
			.enableTieredProposals(false);
	public final YType t_string = f.yatomic("String");
	public final YType t_ne_string = f.yatomic("String")
			.parseWith(ValueParsers.NE_STRING);

	public final YType t_strings = f.yseq(t_string);

	public final YAtomicType t_boolean = f.yenum("boolean", "true", "false");
	public final YType t_any = f.yany("Object");
	public final YType t_params = f.ymap(t_string, t_any);
	public final YType t_string_params = f.ymap(t_string, t_string);
	public final YType t_pos_integer = f.yatomic("Positive Integer")
			.parseWith(ValueParsers.POS_INTEGER);
	public final YType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer")
			.parseWith(ValueParsers.integerAtLeast(1));
	public final YType t_uuid = f.yatomic("UUID").parseWith(UUID::fromString);

	public BoshDeploymentManifestSchema() {
		TYPE_UTIL = f.TYPE_UTIL;
		
		TOPLEVEL_TYPE = f.ybean("BoshDeploymentManifest");
		addProp(TOPLEVEL_TYPE, "name", t_ne_string).isPrimary(true);
		addProp(TOPLEVEL_TYPE, "director_uuid", t_uuid).isDeprecated(
				"bosh v2 CLI no longer checks or requires director_uuid in the deployment manifest. " + 
				"To achieve similar safety make sure to give unique deployment names across environments."
		);

		YAtomicType t_network_name = f.yatomic("NetworkName"); //TODO: resolve from 'cloud config' https://www.pivotaltracker.com/story/show/148712155
		t_network_name.parseWith(ValueParsers.NE_STRING);
		
		YAtomicType t_disk_type = f.yatomic("DiskType"); //TODO: resolve from 'cloud config' https://www.pivotaltracker.com/story/show/148704001
		t_disk_type.parseWith(ValueParsers.NE_STRING);

		YAtomicType t_stemcell_alias = f.yatomic("StemcellAlias"); //TODO: resolve from 'stemcells block' https://www.pivotaltracker.com/story/show/148706041
		t_stemcell_alias.parseWith(ValueParsers.NE_STRING);

		YAtomicType t_vm_extension = f.yatomic("VMExtension"); //TODO: resolve dynamically from 'cloud config' ? https://www.pivotaltracker.com/story/show/148703877
		t_vm_extension.parseWith(ValueParsers.NE_STRING);

		YAtomicType t_vm_type = f.yatomic("VMType"); //TODO: resolve dynamically from 'cloud config' ? https://www.pivotaltracker.com/story/show/148686169
		t_vm_type.parseWith(ValueParsers.NE_STRING);

		YAtomicType t_az = f.yatomic("AvailabilityZone"); //TODO: resolve dynamically from 'cloud config': https://www.pivotaltracker.com/story/show/148704481
		t_az.parseWith(ValueParsers.NE_STRING);
		
		YBeanType t_network = f.ybean("Network");
		addProp(t_network, "name", t_network_name).isRequired(true);
		
		YBeanType t_instance_group_env = f.ybean("InstanceGroupEnv");
		addProp(t_instance_group_env, "npsh", t_params);
		addProp(t_instance_group_env, "password", t_ne_string);

		YAtomicType t_version = f.yatomic("Version");
		t_version.addHints("latest");
		t_version.parseWith(ValueParsers.NE_STRING);

		YBeanType t_release = f.ybean("Release");
		addProp(t_release, "name", t_ne_string).isPrimary(true);
		addProp(t_release, "version", t_version).isRequired(true);
		addProp(TOPLEVEL_TYPE, "releases", f.yseq(t_release)).isRequired(true);
		
		YBeanType t_stemcell = f.ybean("Stemcell");
		addProp(t_stemcell, "alias", t_ne_string).isRequired(true);
		addProp(t_stemcell, "version", t_ne_string).isRequired(true);
		addProp(t_stemcell, "name", t_ne_string);
		addProp(t_stemcell, "os", t_ne_string);
		t_stemcell.requireOneOf("name", "os");
		addProp(TOPLEVEL_TYPE, "stemcells", f.yseq(t_stemcell)).isRequired(true);

		YType t_update = t_params; //TODO: https://www.pivotaltracker.com/story/show/148627121
		addProp(TOPLEVEL_TYPE, "update", t_update).isRequired(true);

		YBeanType t_job = f.ybean("Job");
		addProp(t_job, "name", t_ne_string).isPrimary(true);
		addProp(t_job, "release", t_ne_string).isRequired(true);
		addProp(t_job, "consumes", t_params);
		addProp(t_job, "provides", t_params);
		addProp(t_job, "properties", t_params).isRequired(true);

		YBeanType t_instance_group = f.ybean("InstanceGroup");
		addProp(t_instance_group, "name", t_ne_string).isPrimary(true);
		addProp(t_instance_group, "azs", f.yseq(t_az)).isRequired(true);
		addProp(t_instance_group, "instances", t_pos_integer).isRequired(true); //Strictly positive? Or zero is okay?
		addProp(t_instance_group, "jobs", f.yseq(t_job)).isRequired(true);
		addProp(t_instance_group, "vm_type", t_vm_type).isRequired(true);
		addProp(t_instance_group, "vm_extensions", f.yseq(t_vm_extension));
		addProp(t_instance_group, "stemcell", t_stemcell_alias).isRequired(true);
		addProp(t_instance_group, "persistent_disk_type", t_disk_type);
		addProp(t_instance_group, "networks", f.yseq(t_network));
		addProp(t_instance_group, "update", t_update);
		YType t_migration = t_params; //TODO: https://www.pivotaltracker.com/story/show/148712595
		addProp(t_instance_group, "migrated_from", f.yseq(t_migration));
		addProp(t_instance_group, "lifecycle", f.yenum("WorkloadType", "service", "errand"));
		addProp(t_instance_group, "properties", t_params).isDeprecated("Deprecated in favor of job level properties and links");
		addProp(t_instance_group, "env", t_instance_group_env);
		
		addProp(TOPLEVEL_TYPE, "instance_groups", f.yseq(t_instance_group)).isRequired(true);
		
		addProp(TOPLEVEL_TYPE, "properties", t_params).isDeprecated("Deprecated in favor of job level properties and links");
		
		YType t_variable = t_params; //TODO: https://www.pivotaltracker.com/story/show/148627441
		addProp(TOPLEVEL_TYPE, "variables", f.yseq(t_variable));

		addProp(TOPLEVEL_TYPE, "tags", t_params);
		
	}

	@Override
	public YType getTopLevelType() {
		return TOPLEVEL_TYPE;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return TYPE_UTIL;
	}
	
	private YTypedPropertyImpl prop(AbstractType beanType, String name, YType type) {
		YTypedPropertyImpl prop = f.yprop(name, type);
		prop.setDescriptionProvider(descriptionFor(beanType, name));
		return prop;
	}
	
	public static Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(BoshDeploymentManifestSchema.class, "/desc/"+typeName+"/"+propName);
	}

	private YTypedPropertyImpl addProp(AbstractType bean, String name, YType type) {
		return addProp(bean, bean, name, type);
	}

	private YTypedPropertyImpl addProp(AbstractType superType, AbstractType bean, String name, YType type) {
		YTypedPropertyImpl p = prop(superType, name, type);
		bean.addProperty(p);
		return p;
	}

}
