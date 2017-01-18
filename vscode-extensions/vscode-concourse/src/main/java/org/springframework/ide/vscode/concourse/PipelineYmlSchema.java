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
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YBeanUnionType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YTypedPropertyImpl;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public class PipelineYmlSchema implements YamlSchema {

	private final YBeanType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	public final YTypeFactory f = new YTypeFactory();
	public final YType t_string = f.yatomic("String");
	public final YType t_strings = f.yseq(t_string);
	public final YType t_pair = f.ybean("NameValuePair",
			f.yprop("name", t_string),
			f.yprop("value", t_string)
	);
	public final YType t_pair_list = f.yseq(t_pair);

	public final YAtomicType t_boolean = f.yenum("boolean", "true", "false");
	public final YType t_any = f.yany("Object");
	public final YType t_params = f.ymap(t_string, t_any);
	public final YType t_string_params = f.ymap(t_string, t_string);

	private final ResourceTypeRegistry resourceTypes = new ResourceTypeRegistry();

	public PipelineYmlSchema(ConcourseModel models) {
		TYPE_UTIL = f.TYPE_UTIL;

		// define schema types
		TOPLEVEL_TYPE = f.ybean("Pipeline");

		YAtomicType t_ne_string = f.yatomic("String");
		t_ne_string.parseWith(ValueParsers.NE_STRING);
		YAtomicType t_pos_integer = f.yatomic("Positive Integer");
		t_pos_integer.parseWith(ValueParsers.POS_INTEGER);
		YAtomicType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer");
		t_strictly_pos_integer.parseWith(ValueParsers.integerAtLeast(1));

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
		addProp(getStep, "get", resourceName);
		addProp(getStep, "resource", t_string);
		addProp(getStep, "version", t_version);
		addProp(getStep, "passed", f.yseq(jobName));
		addProp(getStep, "params", t_params);
		addProp(getStep, "trigger", t_boolean);

		YBeanType putStep = f.ybean("PutStep");
		addProp(putStep, "put", resourceName);
		addProp(putStep, "resource", jobName);
		addProp(putStep, "params", t_params);
		addProp(putStep, "get_params", t_params);

		YBeanType taskStep = f.ybean("TaskStep");
		addProp(taskStep, "task", t_ne_string);
		addProp(taskStep, "file", t_string);
		addProp(taskStep, "config", t_any);
		addProp(taskStep, "privileged", t_boolean);
		addProp(taskStep, "params", t_params);
		addProp(taskStep, "image", t_ne_string);
		addProp(taskStep, "input_mapping",  f.ymap(t_ne_string, resourceName));
		addProp(taskStep, "output_mapping", t_string_params);

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
		addProp(aggregateStep, "aggregate", f.yseq(step));
		addProp(doStep, "do", f.yseq(step));
		addProp(tryStep, "try", step);

		// shared properties applicable for any subtype of Step:
		for (YBeanType subStep : stepTypes) {
			addProp(step, subStep, "on_success", step);
			addProp(step, subStep, "on_failure", step);
			addProp(step, subStep, "ensure", step);
			addProp(step, subStep, "attempts", t_strictly_pos_integer);
			addProp(step, subStep, "tags", t_strings);
			addProp(step, subStep, "timeout", t_duration);
		}

		YType resourceSource = f.contextAware("ResourceSource", (dc) -> {
			String typeTag = getResourceTypeTag(models, dc);
			if (typeTag!=null) {
				return resourceTypes.getSourceType(typeTag);
			}
			return null;
		});

		YBeanType resource = f.ybean("Resource");
		addProp(resource, "name", resourceNameDef);
		addProp(resource, "type", t_resource_type_name);
		addProp(resource, "source", resourceSource);
		addProp(resource, "check_every", t_duration);

		YBeanType job = f.ybean("Job");
		addProp(job, "name", jobNameDef);
		addProp(job, "serial", t_boolean);
		addProp(job, "build_logs_to_retain", t_pos_integer);
		addProp(job, "serial_groups", t_strings);
		addProp(job, "max_in_flight", t_pos_integer);
		addProp(job, "public", t_boolean);
		addProp(job, "disable_manual_trigger", t_boolean);
		addProp(job, "plan", f.yseq(step));

		YBeanType resourceType = f.ybean("ResourceType");
		addProp(resourceType, "name", t_ne_string);
		addProp(resourceType, "type", t_image_type);
		addProp(resourceType, "source", t_any);

		YBeanType group = f.ybean("Group");
		addProp(group, "name", t_ne_string);
		addProp(group, "resources", f.yseq(resourceName));
		addProp(group, "jobs", f.yseq(jobName));

		addProp(TOPLEVEL_TYPE, "resources", f.yseq(resource));
		addProp(TOPLEVEL_TYPE, "jobs", f.yseq(job));
		addProp(TOPLEVEL_TYPE, "resource_types", f.yseq(resourceType));
		addProp(TOPLEVEL_TYPE, "groups", f.yseq(group));

		initializeDefaultResourceTypes();
	}

	private void initializeDefaultResourceTypes() {
		YBeanType gitSource = f.ybean("GitResourceSource");
		addProp(gitSource, "uri", t_string);
		addProp(gitSource, "branch", t_string);
		addProp(gitSource, "private_key", t_string);
		addProp(gitSource, "username", t_string);
		addProp(gitSource, "password", t_string);
		addProp(gitSource, "paths", t_strings);
		addProp(gitSource, "ignore_paths", t_strings);
		addProp(gitSource, "skip_ssl_verification", t_boolean);
		addProp(gitSource, "tag_filter", t_string);
		addProp(gitSource, "git_config", t_pair_list);
		addProp(gitSource, "disable_ci_skip", t_boolean);
		addProp(gitSource, "commit_verification_keys", t_strings);
		addProp(gitSource, "commit_verification_key_ids", t_strings);
		addProp(gitSource, "gpg_keyserver", t_string);

		resourceTypes.def("git", gitSource);
	}

	private String getResourceTypeTag(ConcourseModel models, DynamicSchemaContext dc) {
		YamlPath path = dc.getPath();
		if (path!=null) {
			YamlFileAST root = models.getSafeAst(dc.getDocument());
			if (root!=null) {
				return NodeUtil.asScalar(path.dropLast().append(YamlPathSegment.valueAt("type")).traverseToNode(root));
			}
		}
		return null;
	}

	private YTypedProperty prop(AbstractType beanType, String name, YType type) {
		YTypedPropertyImpl prop = f.yprop(name, type);
		prop.setDescriptionProvider(descriptionFor(beanType, name));
		return prop;
	}


	private void addProp(AbstractType superType, AbstractType bean, String name, YType type) {
		bean.addProperty(prop(superType, name, type));
	}

	private void addProp(AbstractType bean, String name, YType type) {
		addProp(bean, bean, name, type);
	}

	public static Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(PipelineYmlSchema.class, "/desc/"+typeName+"/"+propName);
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
