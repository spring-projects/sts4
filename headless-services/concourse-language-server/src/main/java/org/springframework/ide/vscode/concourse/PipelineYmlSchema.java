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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.util.MimeTypes;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.ValueParseException;
import org.springframework.ide.vscode.commons.util.ValueParser;
import org.springframework.ide.vscode.commons.util.ValueParsers;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.commons.yaml.schema.BasicYValueHint;
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
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.schema.constraints.Constraints;
import org.springframework.ide.vscode.concourse.ConcourseModel.ResourceModel;
import org.springframework.ide.vscode.concourse.ConcourseModel.StepModel;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class PipelineYmlSchema implements YamlSchema {

	//TODO: the infos for composing this should probably be integrated somehow in the ResourceTypeRegistry so
	// we only have a list of built-in resource types in a single place.
	public static final YValueHint[] BUILT_IN_RESOURCE_TYPES = {
			hint("git", "The 'git' resource can pull and push to git repositories."),
			hint("hg", "The 'hg' resource can pull and push to Mercurial repositories."),
			hint("time",  "The 'time' resource can start jobs on a schedule or timestamp outputs."),
			hint("s3", "The 's3' resource can fetch from and upload to S3 buckets."),
			hint("archive", "The 'archive' resource can fetch and extract .tar.gz archives."),
			hint("semver", "The 'semver' resource can set or bump version numbers."),
			hint("github-release", "The 'github-release' resource can fetch and publish versioned GitHub resources."),
			hint("docker-image", "The 'docker-image' resource can fetch, build, and push Docker images."),
			hint("tracker", "The 'tracker' resource can deliver stories and bugs on Pivotal Tracker."),
			hint("pool", "The 'pool' resource allows you to configure how to serialize use of an external system. "
					+ "This lets you prevent test interference or overwork on shared systems."),
			hint("cf", "The cf resource can deploy an application to Cloud Foundry."),
			hint("bosh-io-release", "The bosh-io-release resource can track and fetch new BOSH releases from bosh.io."),
			hint("bosh-io-stemcell", "The bosh-io-stemcell resource can track and fetch new BOSH stemcells from bosh.io."),
			hint("bosh-deployment", "The bosh-deployment resource can deploy BOSH stemcells and releases."),
			hint("vagrant-cloud", "The vagrant-cloud resource can fetch and publish Vagrant boxes to Atlas.")
	};

	private final AbstractType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	public final YTypeFactory f = new YTypeFactory();
	public final YType t_string = f.yatomic("String");
	public final YType t_ne_string = f.yatomic("String")
			.parseWith(ValueParsers.NE_STRING);

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
	public final YType t_pos_integer = f.yatomic("Positive Integer")
			.parseWith(ValueParsers.POS_INTEGER);
	public final YType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer")
			.parseWith(ValueParsers.integerAtLeast(1));

	public final AbstractType t_resource_name;
	public final AbstractType t_job_name;
	public final YAtomicType t_resource_type_name;
	public final YType t_mime_type = f.yatomic("MimeType")
			.parseWith(ValueParsers.NE_STRING)
			.addHints(MimeTypes.getKnownMimeTypes());

	public final YType t_duration = f.yatomic("Duration")
			.parseWith(ConcourseValueParsers.DURATION);
	public final YType t_time_of_day = f.yatomic("TimeOfDay")
			.parseWith(ConcourseValueParsers.TIME_OF_DAY);
	public final YType t_location = f.yatomic("Location")
			//Note: we could have used f.yenum here too. But it saves memory if we don't keep the large set of ValueHints in memory.
			// That's why we attach custom hint provider and parser here that do essentially the same thing.
			.addHintProvider(() -> {
				return ZoneId.getAvailableZoneIds().stream()
				.map(BasicYValueHint::new)
				.collect(Collectors.toList());
			})
			.parseWith(ValueParser.of((zoneId) -> {
				if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
					throw new ValueParseException("Unknown 'Location'. See https://en.wikipedia.org/wiki/List_of_tz_database_time_zones");
				}
				return zoneId;
			}));

	public final AbstractType task;

	private final ResourceTypeRegistry resourceTypes = new ResourceTypeRegistry();

	private final ConcourseModel models;

	public final YType t_semver = f.yatomic("Semver")
			.parseWith(ValueParsers.NE_STRING); //TODO: use real semver parser.

	public final YType t_s3_region = f.yenum("S3Region",
			//See: http://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUT.html
			 "us-west-1", "us-west-2",
			 "ca-central-1", "EU", "eu-west-1",
			 "eu-west-2", "eu-central-1",
			 "ap-south-1", "ap-southeast-1", "ap-southeast-2", "ap-northeast-1", "ap-northeast-2",
			 "sa-east-1",
			 "us-east-2"
	);

	public final YType t_day = f.yenum("Day",
			//See https://github.com/concourse/time-resource#source-configuration
			"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	);

	private List<YType> definitionTypes = new ArrayList<>();

	public PipelineYmlSchema(ConcourseModel models) {
		this.models = models;
		models.setResourceTypeRegistry(resourceTypes);
		TYPE_UTIL = f.TYPE_UTIL;

		// define schema types
		TOPLEVEL_TYPE = f.ybean("Pipeline");

		YAtomicType t_version = f.yatomic("Version");
		t_version.addHints("latest", "every");

		t_resource_type_name = f.yenumFromHints("ResourceType Name",
				(parseString, validValues) ->  {
					return "The '"+parseString+"' Resource Type does not exist. Existing types: "+validValues;
				},
				(DynamicSchemaContext dc) -> {
					return models.getResourceTypeNameHints(dc);
				}
		);

		t_resource_name = f.yenum("Resource Name",
				(parseString, validValues) ->  {
					return "The '"+parseString+"' resource does not exist. Existing resources: "+validValues;
				},
				(DynamicSchemaContext dc) -> {
					return (models.getResourceNames(dc));
				}
		);

		t_job_name = f.yenum("Job Name",
				(parseString, validValues) ->  {
					return "The '"+parseString+"' Job does not exist. Existing jobs: "+validValues;
				},
				(DynamicSchemaContext dc) -> {
					return models.getJobNames(dc);
				}
		).require(models::passedJobHasInteractionWithResource);

		YAtomicType t_resource_name_def = f.yatomic("Resource Name");
		t_resource_name_def.parseWith(ConcourseValueParsers.resourceNameDef(models));
		t_resource_name_def.require(models.isUsed(t_resource_name, "Resource"));
		YAtomicType jobNameDef = f.yatomic("Job Name");
		jobNameDef.parseWith(ConcourseValueParsers.jobNameDef(models));
		YAtomicType resourceTypeNameDef = f.yatomic("ResourceType Name");
		resourceTypeNameDef.parseWith(ConcourseValueParsers.resourceTypeNameDef(models));

		YType resourceSource = f.contextAware("ResourceSource", (dc) ->
			resourceTypes.getSourceType(getResourceTypeTag(models, dc))
		);

		AbstractType t_resource = f.ybean("Resource");
		addProp(t_resource, "name", t_resource_name_def).isPrimary(true);
		addProp(t_resource, "type", t_resource_type_name).isRequired(true);
		addProp(t_resource, "source", resourceSource);
		addProp(t_resource, "check_every", t_duration);

		AbstractType t_image_resource = f.ybean("ImageResource");
		for (YTypedProperty p : t_resource.getProperties()) {
			if (!"name".equals(p.getName())) {
				t_image_resource.addProperty(p);
			}
		}

		YAtomicType t_platform = f.yenum("Platform", "windows", "linux", "darwin");
		t_platform.parseWith(ValueParsers.NE_STRING); //no errors because in theory platform are just strings.

		AbstractType t_input = f.ybean("TaskInput");
		addProp(t_input, "name", t_ne_string).isPrimary(true);
		addProp(t_input, "path", t_ne_string);

		AbstractType t_output = f.ybean("TaskOutput");
		addProp(t_output, "name", t_ne_string).isPrimary(true);
		addProp(t_output, "path", t_ne_string);

		AbstractType t_command = f.ybean("Command");
		addProp(t_command, "path", t_ne_string).isRequired(true);
		addProp(t_command, "args", t_strings);
		addProp(t_command, "dir", t_ne_string);
		addProp(t_command, "user", t_string);

		task = f.ybean("TaskConfig");
		addProp(task, "platform", t_platform).isRequired(true);
		addProp(task, "image_resource", t_image_resource);
		addProp(task, "image", t_ne_string);
		addProp(task, "inputs", f.yseq(t_input));
		addProp(task, "outputs", f.yseq(t_output));
		addProp(task, "run", t_command).isRequired(true);
		addProp(task, "params", t_string_params);
		task.require(Constraints.schemaContextAware((DynamicSchemaContext dc) -> {
			LanguageId languageId = dc.getDocument().getLanguageId();
			if (LanguageId.CONCOURSE_PIPELINE.equals(languageId)) {
				Node parentImageDef = models.getParentPropertyNode("image", dc);
				if (parentImageDef==null) {
					return Constraints.requireOneOf("image_resource", "image");
				} else {
					return Constraints.deprecated((name) ->
								"Deprecated: This attribute in the task config will be ignored! "+
								"The 'image' attribute on the task itself takes precedence.",
							"image_resource", "image"
					);
				}
			} else {
				return Constraints.requireAtMostOneOf("image_resource", "image");
			}
		}));

		AbstractType t_put_get_name = f.contextAware("Name", (dc) -> {
			if (models.getParentPropertyNode("resource", dc)!=null) {
				return null;
			} else {
				return t_resource_name;
			}
		})
		.treatAsAtomic()
		.parseWith(ValueParsers.NE_STRING);

		YBeanType getStep = f.ybean("GetStep");
		addProp(getStep, "get", t_put_get_name);
		addProp(getStep, "resource", t_resource_name);
		addProp(getStep, "version", t_version);
		addProp(getStep, "passed", f.yseq(t_job_name));
		addProp(getStep, "params", f.contextAware("GetParams", (dc) ->
			resourceTypes.getInParamsType(getResourceType("get", models, dc))
		));
		addProp(getStep, "trigger", t_boolean);

		YBeanType putStep = f.ybean("PutStep");
		addProp(putStep, "put", t_put_get_name);
		addProp(putStep, "resource", t_resource_name);
		addProp(putStep, "params", f.contextAware("PutParams", (dc) ->
			resourceTypes.getOutParamsType(getResourceType("put", models, dc))
		));
		addProp(putStep, "get_params", f.contextAware("GetParams", (dc) ->
			resourceTypes.getInParamsType(getResourceType("put", models, dc))
		));
		putStep.require((DynamicSchemaContext dc, Node parent, Node _map, YType type, IProblemCollector problems) -> {
			if (_map instanceof MappingNode) {
				MappingNode map = (MappingNode) _map;
				StepModel step = models.newStep(map);
				String resourceName = step.getResourceName();
				if (resourceName!=null) {
					ResourceModel resource = models.getResource(dc.getDocument(), resourceName);
					if (resource!=null) {
						if ("git".equals(resource.getType()) && !resource.hasSourceProperty("branch")) {
							problems.accept(YamlSchemaProblems.schemaProblem(
									"Resource of type 'git' is used in a 'put' step, so it should define 'branch' attribute in its 'source', but it doesn't.",
									step.getResourceNameNode()
							));
						}
					}
				}
			}
		});
		YBeanType taskStep = f.ybean("TaskStep");
		addProp(taskStep, "task", t_ne_string);
		addProp(taskStep, "file", t_string);
		addProp(taskStep, "config", task);
		addProp(taskStep, "privileged", t_boolean);
		addProp(taskStep, "params", t_params);
		addProp(taskStep, "image", t_resource_name);
		addProp(taskStep, "input_mapping",  f.ymap(t_ne_string, t_resource_name));
		addProp(taskStep, "output_mapping", t_string_params);
		taskStep.requireOneOf("config", "file");

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
		for (AbstractType subStep : stepTypes) {
			addProp(step, subStep, "on_success", step);
			addProp(step, subStep, "on_failure", step);
			addProp(step, subStep, "ensure", step);
			addProp(step, subStep, "attempts", t_strictly_pos_integer);
			addProp(step, subStep, "tags", t_strings);
			addProp(step, subStep, "timeout", t_duration);
		}
		models.setStepType(step);

		AbstractType job = f.ybean("Job");
		addProp(job, "name", jobNameDef).isPrimary(true);
		addProp(job, "plan", f.yseq(step)).isRequired(true);
		addProp(job, "serial", t_boolean);
		addProp(job, "build_logs_to_retain", t_pos_integer);
		addProp(job, "serial_groups", t_strings);
		addProp(job, "max_in_flight", t_pos_integer);
		addProp(job, "public", t_boolean);
		addProp(job, "disable_manual_trigger", t_boolean);

		AbstractType resourceType = f.ybean("ResourceType");
		addProp(resourceType, "name", resourceTypeNameDef).isPrimary(true);
		addProp(resourceType, "type", t_resource_type_name).isRequired(true);
		addProp(resourceType, "source", resourceSource);

		AbstractType group = f.ybean("Group");
		addProp(group, "name", t_ne_string).isPrimary(true);
		addProp(group, "resources", f.yseq(t_resource_name));
		addProp(group, "jobs", f.yseq(t_job_name));

		addProp(TOPLEVEL_TYPE, "resources", f.yseq(t_resource));
		addProp(TOPLEVEL_TYPE, "jobs", f.yseq(job));
		addProp(TOPLEVEL_TYPE, "resource_types", f.yseq(resourceType));
		addProp(TOPLEVEL_TYPE, "groups", f.yseq(group));

		definitionTypes = ImmutableList.of(
				jobNameDef,
				resourceTypeNameDef,
				t_resource_name_def
		);

		initializeDefaultResourceTypes();
	}

	private static YValueHint hint(String value, String description) {
		return YTypeFactory.hint(value, value + " - " + description);
	}

	private void initializeDefaultResourceTypes() {
		// git :
		{
			AbstractType source = f.ybean("GitSource");
			addProp(source, "uri", t_ne_string).isPrimary(true);
			addProp(source, "branch", t_ne_string); //It's more complicated than that! Its only required in 'put' step. So we'll check this as a contrain in put steps!
			addProp(source, "private_key", t_ne_string);
			addProp(source, "username", t_ne_string);
			addProp(source, "password", t_string);
			addProp(source, "paths", t_strings);
			addProp(source, "ignore_paths", t_strings);
			addProp(source, "skip_ssl_verification", t_boolean);
			addProp(source, "tag_filter", t_string);
			addProp(source, "git_config", t_pair_list);
			addProp(source, "disable_ci_skip", t_boolean);
			addProp(source, "commit_verification_keys", t_strings);
			addProp(source, "commit_verification_key_ids", t_strings);
			addProp(source, "gpg_keyserver", t_string);

			AbstractType get = f.ybean("GitGetParams");
			addProp(get, "depth", t_pos_integer);
			addProp(get, "submodules", f.yany("GitSubmodules").addHints("all", "none"));
			addProp(get, "disable_git_lfs", t_boolean);

			AbstractType put = f.ybean("GitPutParams");
			addProp(put, "repository", t_ne_string).isPrimary(true);
			addProp(put, "rebase", t_boolean);
			addProp(put, "tag", t_ne_string);
			addProp(put, "only_tag", t_boolean);
			addProp(put, "tag_prefix", t_string);
			addProp(put, "force", t_boolean);
			addProp(put, "annotate", t_ne_string);

			resourceTypes.def("git", source, get, put);
		}
		//docker-image:
		{
			AbstractType source = f.ybean("DockerImageSource");
			addProp(source, "repository", t_ne_string).isPrimary(true);
			addProp(source, "tag", t_ne_string);
			addProp(source, "username", t_ne_string);
			addProp(source, "password", t_ne_string);
			addProp(source, "aws_access_key_id", t_ne_string);
			addProp(source, "aws_secret_access_key", t_ne_string);
			addProp(source, "insecure_registries", t_strings);
			addProp(source, "registry_mirror", t_ne_string);
			addProp(source, "ca_certs", f.yseq(f.ybean("CaCertsEntry",
					f.yprop("domain", t_ne_string),
					f.yprop("cert", t_ne_string)
			)));
			addProp(source, "client_certs", f.yseq(f.ybean("ClientCertsEntry",
					f.yprop("domain", t_ne_string),
					f.yprop("key", t_ne_string),
					f.yprop("cert", t_ne_string)
			)));

			AbstractType get = f.ybean("DockerImageGetParams");
			addProp(get, "save", t_boolean);
			addProp(get, "rootfs", t_boolean);
			addProp(get, "skip_download", t_boolean);

			AbstractType put = f.ybean("DockerImagePutParams");
			addProp(put, "build", t_ne_string);
			addProp(put, "load", t_ne_string);
			addProp(put, "dockerfile", t_ne_string);
			addProp(put, "cache", t_boolean);
			addProp(put, "cache_tag", t_ne_string);
			addProp(put, "load_base", t_ne_string);
			addProp(put, "load_file", t_ne_string);
			addProp(put, "load_repository", t_ne_string);
			addProp(put, "load_tag", t_ne_string);
			addProp(put, "import_file", t_ne_string);
			addProp(put, "pull_repository", t_ne_string).isDeprecated(true);
			addProp(put, "pull_tag", t_ne_string).isDeprecated(true);
			addProp(put, "tag", t_ne_string);
			addProp(put, "tag_prefix", t_ne_string);
			addProp(put, "tag_as_latest", t_boolean);
			addProp(put, "build_args", t_string_params);
			addProp(put, "build_args_file", t_ne_string);

			resourceTypes.def("docker-image", source, get, put);
		}
		//s3
		{
			YType t_canned_acl = f.yenum("S3CannedAcl",
					//See http://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#canned-acl
					"private", "public-read", "public-read-write", "aws-exec-read",
					"authenticated-read", "bucket-owner-read", "bucket-owner-full-control",
					"log-delivery-write"
			);

			AbstractType source = f.ybean("S3Source");
			addProp(source, "bucket", t_ne_string).isPrimary(true);
			addProp(source, "access_key_id", t_ne_string);
			addProp(source, "secret_access_key", t_ne_string);
			addProp(source, "region_name", t_s3_region);
			addProp(source, "private", t_boolean);
			addProp(source, "cloudfront_url", t_ne_string);
			addProp(source, "endpoint", t_ne_string);
			addProp(source, "disable_ssl", t_boolean);
			addProp(source, "server_side_encryption", t_ne_string);
			addProp(source, "sse_kms_key_id", t_ne_string);
			addProp(source, "use_v2_signing", t_boolean);
			addProp(source, "regexp", t_ne_string);
			addProp(source, "versioned_file", t_ne_string);
			source.requireOneOf("regexp", "versioned_file");

			AbstractType get = f.ybean("S3GetParams");
			//Note: S3GetParams intentionally has no properties since no params are expected according to the docs.

			AbstractType put = f.ybean("S3PutParams");
			addProp(put, "file", t_ne_string).isPrimary(true);
			addProp(put, "acl", t_canned_acl);
			addProp(put, "content_type", t_mime_type);

			resourceTypes.def("s3", source, get, put);
		}
		//pool
		{
			AbstractType source = f.ybean("PoolSource");
			addProp(source, "uri", t_ne_string).isRequired(true);
			addProp(source, "branch", t_ne_string).isRequired(true);
			addProp(source, "pool", t_ne_string).isRequired(true);
			addProp(source, "private_key", t_ne_string);
			addProp(source, "username", t_ne_string);
			addProp(source, "password", t_string);
			addProp(source, "retry_delay", t_duration);

			AbstractType get = f.ybean("PoolGetParams");
			//get params deliberately left empty

			AbstractType put = f.ybean("PoolPutParams");
			addProp(put, "acquire", t_boolean);
			addProp(put, "claim", t_ne_string);
			addProp(put, "release", t_ne_string);
			addProp(put, "add", t_ne_string);
			addProp(put, "add_claimed", t_ne_string);
			addProp(put, "remove", t_ne_string);
			put.requireOneOf(put.getPropertyNames());

			resourceTypes.def("pool", source, get, put);
		}
		//semver
		{
			AbstractType git_source = f.ybean("GitSemverSource");
			addProp(git_source, "uri", t_ne_string).isPrimary(true);
			addProp(git_source, "branch", t_ne_string).isRequired(true);
			addProp(git_source, "file", t_ne_string).isRequired(true);
			addProp(git_source, "private_key", t_ne_string);
			addProp(git_source, "username", t_ne_string);
			addProp(git_source, "password", t_ne_string);
			addProp(git_source, "git_user", t_ne_string);

			AbstractType s3_source = f.ybean("S3SemverSource");
			addProp(s3_source, "bucket", t_ne_string).isPrimary(true);
			addProp(s3_source, "key", t_ne_string).isRequired(true);
			addProp(s3_source, "access_key_id", t_ne_string).isRequired(true);
			addProp(s3_source, "secret_access_key", t_ne_string).isRequired(true);
			addProp(s3_source, "region_name", t_s3_region);
			addProp(s3_source, "endpoint", t_ne_string);
			addProp(s3_source, "disable_ssl", t_boolean);

			AbstractType swift_source = f.ybean("SwiftSemverSource");
			addProp(swift_source, "openstack", t_any).isPrimary(true);

			AbstractType[] driverSpecificSources = {
					git_source, s3_source, swift_source
			};

			AbstractType source = f.contextAware("SemverSource", (dc) -> {
				switch (getSemverDriverName(dc)) {
				case "git":
					return git_source;
				case "s3":
					return s3_source;
				case "swift":
					return swift_source;
				default:
					return null;
				}
			}).treatAsBean();
			addProp(source, "initial_version", t_semver);
			addProp(source, "driver", f.yenum("SemverDriver", "git", "s3", "swift"));
			for (AbstractType s : driverSpecificSources) {
				for (YTypedProperty p : source.getProperties()) {
					s.addProperty(p);
				}
			}

			AbstractType get = f.ybean("SemverGetParams");
			addProp(get, "bump", f.yenum("SemverBump", "major", "minor", "patch", "final"));
			addProp(get, "pre", t_ne_string);

			AbstractType put = f.ybean("SemverPutParams");
			for (YTypedProperty p : get.getProperties()) {
				put.addProperty(p);
			}
			addProp(put, "file", t_ne_string);

			resourceTypes.def("semver", source, get, put);
		}
		//time:
		{
			AbstractType source = f.ybean("TimeSource");
			addProp(source, "interval", t_duration);
			addProp(source, "location", t_location);
			addProp(source, "start", t_time_of_day);
			addProp(source, "stop", t_time_of_day);
			addProp(source, "days", f.yseq(t_day));

			AbstractType get = f.ybean("TimeGetParams");
			//get params deliberately left empty

			AbstractType put = f.ybean("TimePutParams");
			//put params deliberately left empty

			resourceTypes.def("time", source, get, put);

		}
	}

	private String getSemverDriverName(DynamicSchemaContext dc) {
		String driver = getSiblingPropertyValue(dc, "driver");
		return driver!=null ? driver : "s3";
	}

	private Node getResourceNameNode(String resourceNameProp, DynamicSchemaContext dc) {
		Node resourceName = models.getParentPropertyNode("resource", dc);
		if (resourceName==null) {
			resourceName = models.getParentPropertyNode(resourceNameProp, dc);
		}
		return resourceName;
	}

	private String getResourceName(String resourceNameProp, DynamicSchemaContext dc) {
		Node resourceName = getResourceNameNode(resourceNameProp, dc);
		return NodeUtil.asScalar(resourceName);
	}

	private String getResourceType(String resourceNameProp, ConcourseModel models, DynamicSchemaContext dc) {
		String resourceName = getResourceName(resourceNameProp, dc);
		if (resourceName!=null) {
			return models.getResourceType(dc.getDocument(), resourceName);
		}
		return null;
	}

	private String getResourceTypeTag(ConcourseModel models, DynamicSchemaContext dc) {
		return getParentPropertyValue("type", models, dc);
	}

	private String getParentPropertyValue(String propName, ConcourseModel models, DynamicSchemaContext dc) {
		return NodeUtil.asScalar(models.getParentPropertyNode(propName, dc));
	}

	private String getSiblingPropertyValue(DynamicSchemaContext dc, String propName) {
		YamlPath path = dc.getPath();
		if (path!=null) {
			YamlFileAST root = models.getSafeAst(dc.getDocument());
			if (root!=null) {
				return NodeUtil.asScalar(path.append(YamlPathSegment.valueAt(propName)).traverseToNode(root));
			}
		}
		return null;
	}

	private YTypedPropertyImpl prop(AbstractType beanType, String name, YType type) {
		YTypedPropertyImpl prop = f.yprop(name, type);
		prop.setDescriptionProvider(descriptionFor(beanType, name));
		return prop;
	}

	private YTypedPropertyImpl addProp(AbstractType superType, AbstractType bean, String name, YType type) {
		YTypedPropertyImpl p = prop(superType, name, type);
		bean.addProperty(p);
		return p;
	}

	private YTypedPropertyImpl addProp(AbstractType bean, String name, YType type) {
		return addProp(bean, bean, name, type);
	}

	public static Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(PipelineYmlSchema.class, "/desc/"+typeName+"/"+propName);
	}

	@Override
	public AbstractType getTopLevelType() {
		return TOPLEVEL_TYPE;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return TYPE_UTIL;
	}

	public YamlSchema getTaskSchema() {
		return new YamlSchema() {

			@Override
			public YTypeUtil getTypeUtil() {
				return TYPE_UTIL;
			}

			@Override
			public YType getTopLevelType() {
				return task;
			}

			@Override
			public String toString() {
				return "TaskYamlSchema";
			}
		};
	}

	public List<YType> getDefinitionTypes() {
		return definitionTypes;
	}
}
