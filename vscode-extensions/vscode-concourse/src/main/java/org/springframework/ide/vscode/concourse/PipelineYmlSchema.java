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
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanUnionType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public class PipelineYmlSchema implements YamlSchema {

	private final YBeanType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	private final YTypeFactory f = new YTypeFactory();

	public PipelineYmlSchema(ConcourseModel models) {
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
		YAtomicType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer");
		t_strictly_pos_integer.parseWith(ValueParsers.integerAtLeast(1));

		YType t_any = f.yany("Object");
		YType t_params = f.ymap(t_string, t_any);
		YType t_string_params = f.ymap(t_string, t_string);

		YAtomicType t_duration = f.yatomic("Duration");
		t_duration.parseWith(ValueParsers.DURATION);

		YAtomicType t_version = f.yatomic("Version");
		t_version.addHints("latest", "every");
		
		YAtomicType t_image_type = f.yatomic("ImageType");
		t_image_type.addHints("docker_image");

		YAtomicType t_resource_type_name = f.yatomic("ResourceType Name");
		t_resource_type_name.addHints(
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
		
		YType resourceName = f.yenum("Resource Name", 
				(parseString, validValues) ->  {
					return "The '"+parseString+"' resource does not exist. Existing resources: "+validValues;
				},
				(DynamicSchemaContext dc) -> {
					return models.getResourceNames(dc.getDocument());
				}
		);

		YType jobName = f.yenum("Job Name", 
				(parseString, validValues) ->  {
					return "The '"+parseString+"' resource does not exist. Existing resources: "+validValues;
				},
				(DynamicSchemaContext dc) -> {
					return models.getJobNames(dc.getDocument());
				}
		);
		
		YAtomicType resourceNameDef = f.yatomic("Resource Name");
		resourceNameDef.parseWith(ValueParsers.resourceNameDef(models));
		YAtomicType jobNameDef = f.yatomic("Job Name");
		jobNameDef.parseWith(ValueParsers.jobNameDef(models));

		YBeanType getStep = f.ybean("GetStep");
		prop(getStep, "get", resourceName);
		prop(getStep, "resource", t_string);
		prop(getStep, "version", t_version);
		prop(getStep, "passed", f.yseq(jobName));
		prop(getStep, "params", t_params);
		prop(getStep, "trigger", t_boolean);

		YBeanType putStep = f.ybean("PutStep");
		prop(putStep, "put", resourceName);
		prop(putStep, "resource", jobName);
		prop(putStep, "params", t_params);
		prop(putStep, "get_params", t_params);

		YBeanType taskStep = f.ybean("TaskStep");
		prop(taskStep, "task", t_ne_string);
		prop(taskStep, "file", t_string);
		prop(taskStep, "config", t_any);
		prop(taskStep, "privileged", t_boolean);
		prop(taskStep, "params", t_params);
		prop(taskStep, "image", t_ne_string);
		prop(taskStep, "input_mapping",  f.ymap(t_ne_string, resourceName));
		prop(taskStep, "output_mapping", t_string_params);

		YBeanType aggregateStep = f.ybean("AggregateStep");
		YBeanType doStep = f.ybean("DoStep");
		YBeanType tryStep = f.ybean("TryStep");
		
		YBeanType[] stepTypes = {
				getStep,
				putStep,
				taskStep,
				aggregateStep,
				doStep,
				tryStep
		};
		YBeanUnionType step = f.yunion("Step", stepTypes);
		prop(aggregateStep, "aggregate", f.yseq(step));
		prop(doStep, "do", f.yseq(step));
		prop(tryStep, "try", step);

		// shared properties applicable for any type of Step:
		prop(step, "on_success", step);
		prop(step, "on_failure", step);
		prop(step, "ensure", step);
		prop(step, "attempts", t_strictly_pos_integer);
		prop(step, "tags", t_strings);
		prop(step, "timeout", t_duration);
		
		YBeanType resource = f.ybean("Resource");
		prop(resource, "name", resourceNameDef);
		prop(resource, "type", t_resource_type_name);
		prop(resource, "source", t_any);

		YBeanType job = f.ybean("Job");
		prop(job, "name", jobNameDef);
		prop(job, "serial", t_boolean);
		prop(job, "build_logs_to_retain", t_pos_integer);
		prop(job, "serial_groups", t_strings);
		prop(job, "max_in_flight", t_pos_integer);
		prop(job, "public", t_boolean);
		prop(job, "disable_manual_trigger", t_boolean);
		prop(job, "plan", f.yseq(step));
		
		YBeanType resourceType = f.ybean("ResourceType");
		prop(resourceType, "name", t_ne_string);
		prop(resourceType, "type", t_image_type);
		prop(resourceType, "source", t_any);
		
		YBeanType group = f.ybean("Group");
		prop(group, "name", t_ne_string);
		prop(group, "resources", f.yseq(resourceName));
		prop(group, "jobs", f.yseq(jobName));

		prop(TOPLEVEL_TYPE, "resources", f.yseq(resource));
		prop(TOPLEVEL_TYPE, "jobs", f.yseq(job));
		prop(TOPLEVEL_TYPE, "resource_types", f.yseq(resourceType));
		prop(TOPLEVEL_TYPE, "groups", f.yseq(group));

	}

	private void prop(AbstractType bean, String name, YType type) {
		bean.addProperty(name, type, descriptionFor(bean, name));
	}

	private Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(this.getClass(), "/desc/"+typeName+"/"+propName);
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
