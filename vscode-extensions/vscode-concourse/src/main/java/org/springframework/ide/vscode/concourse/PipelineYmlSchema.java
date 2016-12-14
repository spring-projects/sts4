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
package org.springframework.ide.vscode.concourse;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public class PipelineYmlSchema implements YamlSchema {

	private final YBeanType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	private final YTypeFactory f = new YTypeFactory();

	public PipelineYmlSchema() {
		TYPE_UTIL = f.TYPE_UTIL;

		// define schema types
		TOPLEVEL_TYPE = f.ybean("Pipeline");

		YType t_string = f.yatomic("String");
		YAtomicType t_ne_string = f.yatomic("String");
		t_ne_string.parseWith(ValueParsers.NE_STRING);
		YType t_strings = f.yseq(t_string);
		YAtomicType t_boolean = f.yenum("boolean", "true", "false");
		YAtomicType t_pos_integer = f.yatomic("Positive Integer");
		t_pos_integer.parseWith(ValueParsers.POS_INTEGER);
		YType t_any = f.yany("Object");
		YType t_params = f.ymap(t_string, t_any);

		YAtomicType t_version = f.yatomic("Version");
		t_version.addHints("latest", "every");
		
		YAtomicType t_image_type = f.yatomic("ImageType");
		t_image_type.addHints("docker_image");

		YAtomicType t_resource_type = f.yatomic("ResourceType");
		t_resource_type.addHints(
				f.hint("archive", "archive - The 'archive' resource can fetch and extract .tar.gz archives."),
				f.hint("git", "git - The 'git' resource can pull and push to git repositories"),
				f.hint("s3", "s3 - The 's3' resource can fetch from and upload to S3 buckets."),
				f.hint("semver", "semver - The 'semver' resource can set or bump version numbers."),
				f.hint("time",  "time - The 'time' resource can start jobs on a schedule or timestamp outputs."),
				f.hint("docker-image", "docker-image - The 'docker-image' resource can fetch, build, and push Docker images")
				//TODO: add more resource types and descriptions.
//
//				 The github-release resource can fetch and publish versioned GitHub resources.
//
//
//				 The tracker resource can deliver stories and bugs on Pivotal Tracker
//
//				 The pool resource allows you to configure how to serialize use of an external system. This lets you prevent test interference or overwork on shared systems.
//
//				 The cf resource can deploy an application to Cloud Foundry.
//
//				 The bosh-io-release resource can track and fetch new BOSH releases from bosh.io.
//
//				 The bosh-io-stemcell resource can track and fetch new BOSH stemcells from bosh.io.
//
//				 The bosh-deployment resource can deploy BOSH stemcells and releases.
//
//				 The vagrant-cloud r
		);

		YBeanType getStep = f.ybean("GetStep");
		prop(getStep, "get", t_ne_string);
		prop(getStep, "resource", t_string);
		prop(getStep, "version", t_version);
		prop(getStep, "passed", t_strings);
		prop(getStep, "params", t_params);
		prop(getStep, "trigger", t_boolean);

		YBeanType putStep = f.ybean("PutStep");
		prop(putStep, "put", t_ne_string);
		prop(putStep, "resource", t_string);
		prop(putStep, "params", t_params);

		YBeanType taskStep = f.ybean("TaskStep");
		prop(taskStep, "task", t_ne_string);
		prop(taskStep, "file", t_string);
		prop(taskStep, "config", t_any);
		prop(taskStep, "privileged", t_boolean);
		prop(taskStep, "params", t_params);

		YType step = f.yunion("Step",
				getStep,
				putStep,
				taskStep
		);

		YBeanType resource = f.ybean("Resource");
		prop(resource, "name", t_ne_string);
		prop(resource, "type", t_resource_type);
		prop(resource, "source", t_any);

		YBeanType job = f.ybean("Job");
		prop(job, "name", t_ne_string);
		prop(job, "serial", t_boolean);
		prop(job, "build_logs_to_retain", t_pos_integer);
		prop(job, "serial_groups", t_strings);
		prop(job, "max_in_flight", t_pos_integer);
		prop(job, "public", t_boolean);
		prop(job, "disable_manual_trigger", t_boolean);
		prop(job, "plan", f.yseq(step));
		
		YType resource_type_def = f.ybean("ResourceTypeDef", 
			f.yprop("name", t_ne_string),
			f.yprop("type", t_image_type),
			f.yprop("source", t_any)
		);

		prop(TOPLEVEL_TYPE, "resources", f.yseq(resource));
		prop(TOPLEVEL_TYPE, "jobs", f.yseq(job));
		prop(TOPLEVEL_TYPE, "resource_types", f.yseq(resource_type_def));

	}

	private void prop(YBeanType bean, String name, YType type) {
		bean.addProperty(name, type, descriptionFor(bean, name));
	}

	private Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(this.getClass(), "/desc/"+typeName+"/"+propName+".html");
	}

	@Override
	public YBeanType getTopLevelType() {
		return TOPLEVEL_TYPE;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return TYPE_UTIL;
	}
}
