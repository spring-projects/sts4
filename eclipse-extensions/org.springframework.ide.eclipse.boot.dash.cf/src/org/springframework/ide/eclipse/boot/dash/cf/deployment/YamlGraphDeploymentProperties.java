/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFRoute;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeMergeSupport;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Deployment properties based on YAML Graph. Instance of this class has ability
 * to compute text differences between this instance and deployment properties
 * passed as parameter
 *
 * @author Alex Boyko
 *
 */
public class YamlGraphDeploymentProperties implements DeploymentProperties {

	private String content;
	private MappingNode appNode;
	private Node root;
	private SequenceNode applicationsValueNode;
	private Yaml yaml;
	private CloudData cloudData;

	public YamlGraphDeploymentProperties(String content, String appName, CloudData cloudData) {
		super();
		this.appNode = null;
		this.applicationsValueNode = null;
		this.root = null;
		this.cloudData = cloudData;
		this.content = content;
		initializeYaml(appName);
	}

	private void initializeYaml(String appName) {
		Composer composer = new Composer(new ParserImpl(new StreamReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes())))), new Resolver());
		root = composer.getSingleNode();

		NodeMergeSupport mergeSupport = new NodeMergeSupport(IProblemCollector.NULL);
		mergeSupport.mergeAll(root);

		Node apps = YamlGraphDeploymentProperties.findValueNode(root, "applications");
		if (apps instanceof SequenceNode) {
			applicationsValueNode = (SequenceNode) apps;
			appNode = findAppNode(applicationsValueNode, appName);
		} else if (root instanceof MappingNode) {
			appNode = (MappingNode) root;
		}

		this.yaml = new Yaml(createDumperOptions());
	}

	private static MappingNode findAppNode(SequenceNode seq, String name) {
		if (name != null) {
			for (Node n : seq.getValue()) {
				Node nameValue = findValueNode(n, ApplicationManifestHandler.NAME_PROP);
				if (nameValue instanceof ScalarNode && ((ScalarNode)nameValue).getValue().equals(name)) {
					return (MappingNode) n;
				}
			}
		}
		return null;
	}

	@Override
	public String getYamlContent() {
		return content;
	}

	public static DumperOptions createDumperOptions() {
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(false);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setLineBreak(LineBreak.getPlatformLineBreak());
		return options;
	}

	@SuppressWarnings("unchecked")
	static public <T extends Node> T getNode(Node node, String key, Class<T> type) {
		Node n = findValueNode(node, key);
		if (n != null && type.isAssignableFrom(n.getClass())) {
			return (T) n;
		}
		return null;
	}

	@Override
	public String getAppName() {
		/*
		 * Name must be located in the app node!
		 */
		return getPropertyValue(appNode, ApplicationManifestHandler.NAME_PROP, String.class);
	}

	@Override
	public int getMemory() {
		String memoryStringValue = getAbsoluteValue(ApplicationManifestHandler.MEMORY_PROP, String.class);
		if (memoryStringValue != null) {
			try {
				return ApplicationManifestHandler.convertMemory(memoryStringValue);
			} catch (CoreException e) {
				Log.log(e);
			}
		}
		return DeploymentProperties.DEFAULT_MEMORY;
	}

	public String getInheritFilePath() {
		return getPropertyValue(root, ApplicationManifestHandler.INHERIT_PROP, String.class);
	}

	@Override
	public String getBuildpack() {
		return getAbsoluteValue(ApplicationManifestHandler.BUILDPACK_PROP, String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getEnvironmentVariables() {
		Map<String, String> map = getAbsoluteValue(ApplicationManifestHandler.ENV_PROP, Map.class);
		return map == null ? Collections.<String, String>emptyMap() : map;
	}

	@Override
	public int getInstances() {
		Integer n = getAbsoluteValue(ApplicationManifestHandler.INSTANCES_PROP, Integer.class);
		return n == null ? DeploymentProperties.DEFAULT_INSTANCES : n.intValue();
	}

	@Override
	public Integer getTimeout() {
		return getAbsoluteValue(ApplicationManifestHandler.TIMEOUT_PROP, Integer.class);
	}

	@Override
	public String getCommand() {
		return getAbsoluteValue(ApplicationManifestHandler.COMMAND_PROP, String.class);
	}

	@Override
	public String getHealthCheckType() {
		return getAbsoluteValue(ApplicationManifestHandler.HEALTH_CHECK_TYPE_PROP, String.class);
	}

	@Override
	public String getHealthCheckHttpEndpoint() {
		return getAbsoluteValue(ApplicationManifestHandler.HEALTH_CHECK_HTTP_ENDPOINT_PROP, String.class);
	}

	@Override
	public String getStack() {
		return getAbsoluteValue(ApplicationManifestHandler.STACK_PROP, String.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getServices() {
		List<String> services = getAbsoluteValue(ApplicationManifestHandler.SERVICES_PROP, List.class);
		return services == null ? Collections.<String>emptyList() : services;
	}

	public static Node findValueNode(Node node, String key) {
		return NodeUtil.getProperty(node, key);
	}

	public static NodeTuple findNodeTuple(MappingNode mapping, String key) {
		if (mapping != null) {
			for (NodeTuple tuple : mapping.getValue()) {
				if (tuple.getKeyNode() instanceof ScalarNode) {
					ScalarNode scalar = (ScalarNode) tuple.getKeyNode();
					if (key.equals(scalar.getValue())) {
						return tuple;
					}
				}
			}
		}
		return null;
	}

	private ReplaceEdit addLineBreakIfMissing(int index) {
		int i = index - 1;
		for (; i >= 0 && Character.isWhitespace(content.charAt(i)) && content.charAt(i) != '\n'; i--);
		if (i > 0 && content.charAt(i) != '\n') {
			return new ReplaceEdit(index, 0, System.lineSeparator());
		}
		return null;
	}

	public MultiTextEdit getDifferences(DeploymentProperties props) {
		MultiTextEdit edits = new MultiTextEdit();
		TextEdit edit;

		if (appNode == null) {
			Map<Object, Object> obj = ApplicationManifestHandler.toYaml(props, cloudData, isLegacyHostDomainManifestYaml(root));
			if (applicationsValueNode == null) {
				DumperOptions options = new DumperOptions();
				options.setExplicitStart(true);
				options.setCanonical(false);
				options.setPrettyFlow(true);
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
				options.setLineBreak(LineBreak.getPlatformLineBreak());
				edits.addChild(new ReplaceEdit(0, content.length(), new Yaml(options).dump(obj)));
			} else {
				edit = addLineBreakIfMissing(applicationsValueNode.getEndMark().getIndex());
				if (edit != null) {
					edits.addChild(edit);
				}
				@SuppressWarnings("unchecked")
				/*
				 * Find the appropriate application Object in the list.
				 */
				List<Object> appsObj = (List<Object>) obj.get(ApplicationManifestHandler.APPLICATIONS_PROP);
				Object appObject = appsObj.get(0);
				for (Object entry : appsObj) {
					if (entry instanceof Map<?,?> && Objects.equal(props.getAppName(), ((Map<?,?>)entry).get(ApplicationManifestHandler.NAME_PROP))) {
						appObject = entry;
						break;
					}
				}
				edits.addChild(new ReplaceEdit(applicationsValueNode.getEndMark().getIndex(), 0, serializeListEntry(appObject, applicationsValueNode.getStartMark().getColumn()).toString()));
			}
		} else {
			if (!Objects.equal(getAppName(), props.getAppName())) {
				edit = createEdit(appNode, props.getAppName(), ApplicationManifestHandler.NAME_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			/*
			 * Compare value because strings may have 'G', 'M' etc post-fixes
			 */
			if (getMemory() != props.getMemory()) {
				edit = createEdit(appNode, String.valueOf(props.getMemory()) + "M", ApplicationManifestHandler.MEMORY_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (getInstances() != props.getInstances()) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.INSTANCES_PROP, props.getInstances(), DEFAULT_INSTANCES, Integer.class);
			}

			if (!Objects.equal(getTimeout(), props.getTimeout())) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.TIMEOUT_PROP, props.getTimeout(), null, Integer.class);
			}

			if (!Objects.equal(getHealthCheckType(), props.getHealthCheckType())) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.HEALTH_CHECK_TYPE_PROP, props.getHealthCheckType(),
						DeploymentProperties.DEFAULT_HEALTH_CHECK_TYPE, String.class);
			}

			if (!Objects.equal(getHealthCheckHttpEndpoint(), props.getHealthCheckHttpEndpoint())) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.HEALTH_CHECK_HTTP_ENDPOINT_PROP, props.getHealthCheckHttpEndpoint(),
						DeploymentProperties.DEFAULT_HEALTH_CHECK_HTTP_ENDPOINT, String.class);
			}

			if (!Objects.equal(getCommand(), props.getCommand())) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.COMMAND_PROP, props.getCommand(), null, String.class);
			}

			/*
			 * Only if 'stack' attribute is present in the manifest perform the comparison
			 */
			if (getStack() != null && !getStack().equals(props.getStack())) {
				getDifferenceForEntry(edits, ApplicationManifestHandler.STACK_PROP, props.getStack(), null, String.class);
			}

			if (getDiskQuota() != props.getDiskQuota()) {
				edit = createEdit(appNode, String.valueOf(props.getDiskQuota()) + "M", ApplicationManifestHandler.DISK_QUOTA_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (!Objects.equal(getBuildpack(), props.getBuildpack())) {
				edit = createEdit(appNode, props.getBuildpack(), ApplicationManifestHandler.BUILDPACK_PROP);
				if (edit != null) {
					edits.addChild(edit);
				}
			}

			if (!new HashSet<>(getServices()).equals(new HashSet<>(props.getServices()))) {
				getDifferencesForList(edits, ApplicationManifestHandler.SERVICES_PROP, props.getServices(), String.class);
			}

			if (!getEnvironmentVariables().equals(props.getEnvironmentVariables())) {
				getDifferencesForMap(edits, ApplicationManifestHandler.ENV_PROP, props.getEnvironmentVariables());
			}

			/*
			 * If any text edits are produced then there are differences in the URIs
			 */
			Set<String> currentUris = getUris();
			Set<String> otherUris = props.getUris();
			if (!isRandomRouteMatch(currentUris, otherUris) && !currentUris.equals(otherUris)) {
				if (isLegacyHostDomainManifestYaml(root)) {
					getLegacyDifferenceForUris(otherUris, edits);
				} else {
					getDifferenceForUris(otherUris, edits);
				}
			}
		}
		return edits.hasChildren() ? edits : null;
	}

	private boolean isRandomRouteMatch(Set<String> currentUris, Set<String> otherUris) {
		if (currentUris.size() == 1 && otherUris.size() == 1) {
			String uri = currentUris.iterator().next();
			String host = uri.substring(0, uri.indexOf('.'));
			return ApplicationManifestHandler.RANDOM_VAR.equals(host);
		}
		return false;
	}

	/**
	 * Creates diff text edits for entry in the map node given by the
	 * attribute's name based on its new value and type as well as the default
	 * value that is not serialized under normal circumstances
	 *
	 * @param me
	 *            container to append text edits
	 * @param key
	 *            manifest attribute name
	 * @param newValue
	 *            the new value to create diff edits for
	 * @param defaultValue
	 *            the default value for the attribute that is usually not
	 *            serialized
	 * @param type
	 *            type of the value for the attribute
	 */
	private <T> void getDifferenceForEntry(MultiTextEdit me, String key, T newValue, T defaultValue, Class<T> type) {
		TextEdit edit = null;
		if (Objects.equal(newValue, defaultValue)) {
			/*
			 * New value is the default value. Check if entry can be safely removed from YAML
			 */
			T rootValue = getPropertyValue(root, key, type);
			if (appNode != root && rootValue != null) {
				if (newValue == null) {
					me.addChild(createEdit((MappingNode)root, (Object) null, key));
					for (Node n : applicationsValueNode.getValue()) {
						if (n instanceof MappingNode) {
							MappingNode application = (MappingNode) n;
							if (application == appNode) {
								edit = createEdit(appNode, (Object) null, key);
								if (edit != null) {
									me.addChild(edit);
								}
							} else {
								T appValue = getPropertyValue(application, key, type);
								if (appValue == null) {
									me.addChild(createEdit(application, rootValue, key));
								}
							}
						}
					}
				} else {
					edit = createEdit(appNode, newValue, key);
					if (edit != null) {
						me.addChild(edit);
					}
				}
			} else {
				edit = createEdit(appNode, (T) null, key);
				if (edit != null) {
					me.addChild(edit);
				}
			}
		} else {
			/*
			 * New value is not default hence it have to be serialized and
			 * therefore would override the value in the root node for the same
			 * attribute
			 */
			edit = createEdit(appNode, newValue, key);
			if (edit != null) {
				me.addChild(edit);
			}
		}
	}

	/**
	 * Creates text edits based on differences between current manifest
	 * attribute list value and the passed new list value. The manifest
	 * attribute value is considered to be defined either on the application or
	 * the root node of the manifest YAML and text edit is calculated
	 * accordingly. It also supports multiple apps defined in the manifest
	 *
	 * @param me
	 *            multi text edit gathering all edits
	 * @param key
	 *            manifest attribute name
	 * @param newValue
	 *            the new value to set
	 */
	@SuppressWarnings("unchecked")
	private <T> void getDifferencesForList(MultiTextEdit me, String key, List<T> newValue, Class<T> type) {
		TextEdit edit;
		/*
		 * Moved new value entries in the set to avoid duplication
		 */
		LinkedHashSet<T> otherValue = new LinkedHashSet<>(newValue);
		/*
		 * Get the list value from the root node
		 */
		List<T> rootList = root != appNode ? getPropertyValue(root, key, List.class) : Collections.emptyList();
		if (rootList == null) {
			rootList = Collections.emptyList();
		}
		if (otherValue.containsAll(rootList)) {
			/*
			 * All list entries from the root are present in the new value
			 */
			otherValue.removeAll(rootList);
			/*
			 * Create an edit for a difference between the remaining list of
			 * values and current app's node list
			 */
			edit = createEdit(appNode, new ArrayList<>(otherValue), key, type);
			if (edit != null) {
				me.addChild(edit);
			}
		} else {
			/*
			 * Some list entries from the root are missing move all root values
			 * to application nodes. Applications value node must be present
			 * because rootList wasn't empty since we got here
			 */
			for (Node n : applicationsValueNode.getValue()) {
				if (n instanceof MappingNode) {
					MappingNode application = (MappingNode) n;
					if (n == appNode) {
						/*
						 * Current app node
						 */
						edit = createEdit(application, newValue, key, type);
						if (edit != null) {
							me.addChild(edit);
						}
					} else {
						/*
						 * Any other app node. Get its list value for the attribute
						 */
						List<T> currentValues = getPropertyValue(application, key, List.class);
						if (currentValues == null) {
							/*
							 * There is no value for the property so just create an edit for the list from the root
							 */
							edit = createEdit(application, rootList, key, type);
						} else {
							/*
							 * Create a joint list of values from app's node list value and root node list value
							 */
							LinkedHashSet<T> values = new LinkedHashSet<>(currentValues);
							values.addAll(rootList);
							/*
							 * Create and edit with the new value being the joint list
							 */
							edit = createEdit(application, new ArrayList<>(values), key, type);
						}
						if (edit != null) {
							me.addChild(edit);
						}
					}
				}
			}
			/*
			 * Remove the list from the root node
			 */
			edit = createEdit((MappingNode)root, (Object)null, key);
			if (edit != null) {
				me.addChild(edit);
			}
		}
	}

	/**
	 * Creates text edits based on differences between current manifest
	 * attribute map value and the passed new map value. The manifest
	 * attribute value is considered to be defined either on the application or
	 * the root node of the manifest YAML and text edit is calculated
	 * accordingly. It also supports multiple apps defined in the manifest
	 *
	 * @param me
	 *            multi text edit gathering all edits
	 * @param key
	 *            manifest attribute name
	 * @param newValue
	 *            the new value to set
	 */
	@SuppressWarnings("unchecked")
	private void getDifferencesForMap(MultiTextEdit me, String key, Map<String, String> newValue) {
		TextEdit edit;
		/*
		 * Get the map value from the root node
		 */
		Map<String, String> rootMap = root != appNode ? getPropertyValue(root, key, Map.class) : Collections.emptyMap();
		/*
		 * Copy the new value to leave it unchanged
		 */
		LinkedHashMap<String, String> otherValue = new LinkedHashMap<>(newValue);
		if (rootMap == null) {
			rootMap = Collections.emptyMap();
		}
		if (otherValue.keySet().containsAll(rootMap.keySet())) {
			/*
			 * All map entries from the root are present in the new value
			 */
			for (String k : rootMap.keySet()) {
				if (Objects.equal(otherValue.get(k), rootMap.get(k))) {
					otherValue.remove(k);
				}
			}
			/*
			 * Create an edit for a difference between the remaining map and
			 * current app's node map
			 */
			edit = createEdit(appNode, otherValue, key);
			if (edit != null) {
				me.addChild(edit);
			}
		} else {
			/*
			 * Some map entries from the root must be removed. Move root node map to applications.
			 * Applications value node must be present because rootList wasn't empty since we got here.
			 */
			for (Node n : applicationsValueNode.getValue()) {
				if (n instanceof MappingNode) {
					MappingNode application = (MappingNode) n;
					if (n == appNode) {
						/*
						 * Current app node
						 */
						edit = createEdit(application, newValue, key);
						if (edit != null) {
							me.addChild(edit);
						}
					} else {
						/*
						 * Any other app node. Get its map value for the attribute
						 */
						Map<String, String> currentValues = getPropertyValue(application, key, Map.class);
						if (currentValues == null) {
							/*
							 * There is no value for the property so just create an edit for the map from the root
							 */
							edit = createEdit(application, rootMap, key);
						} else {
							/*
							 * Create a joint map of entries from app's node map value and root node map value
							 */
							for (Map.Entry<String, String> entry : rootMap.entrySet()) {
								if (!currentValues.containsKey(entry.getKey())) {
									currentValues.put(entry.getKey(), entry.getValue());
								}
							}
							/*
							 * Create and edit with the new value being the joint map
							 */
							edit = createEdit(application, currentValues, key);
						}
						if (edit != null) {
							me.addChild(edit);
						}
					}
				}
			}
			/*
			 * Remove the list from the root node
			 */
			edit = createEdit((MappingNode)root, Collections.<String, String>emptyMap(), key);
			if (edit != null) {
				me.addChild(edit);
			}

		}
	}

	private void getDifferenceForUris(Collection<String> uris, MultiTextEdit me) {
		Boolean randomRoute = getAbsoluteValue(ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class);
		Boolean noRoute = getAbsoluteValue(ApplicationManifestHandler.NO_ROUTE_PROP, Boolean.class);
		boolean otherNoRoute = uris.isEmpty();
		boolean match = false;

		if (otherNoRoute) {
			if (!Boolean.TRUE.equals(noRoute)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.NO_ROUTE_PROP, true, false, Boolean.class);

				if (getPropertyValue(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class) != null) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
				}
			} else {
				match = true;
			}
		} else {
			if (Boolean.TRUE.equals(noRoute)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.NO_ROUTE_PROP, false, false, Boolean.class);
			}

			if (Boolean.TRUE.equals(randomRoute) && uris.size() == 1 && getAbsoluteValue(ApplicationManifestHandler.ROUTES_PROP, Map.class) == null) {
				match = true;
			} else if (getPropertyValue(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class) != null) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
			}
		}

		if (!match) {
			getDifferencesForList(me, ApplicationManifestHandler.ROUTES_PROP, uris.stream().map(uri -> {
				Map<Object, Object> routeObj = new LinkedHashMap<>();
				routeObj.put(ApplicationManifestHandler.ROUTE_PROP, uri);
				return routeObj;
			}).collect(Collectors.toList()), Map.class);
		}

	}

	private void getLegacyDifferenceForUris(Collection<String> uris, MultiTextEdit me) {
		List<CFCloudDomain> domains = cloudData.getDomains();

		LinkedHashSet<String> otherHosts = new LinkedHashSet<>();
		LinkedHashSet<String> otherDomains = new LinkedHashSet<>();
		ApplicationManifestHandler.extractHostsAndDomains(uris, domains, otherHosts, otherDomains);
		boolean otherNoRoute = otherHosts.isEmpty() && otherDomains.isEmpty();
		boolean otherNoHostname = otherHosts.isEmpty() && !otherDomains.isEmpty();

		LinkedHashSet<String> currentHosts = new LinkedHashSet<>();
		LinkedHashSet<String> currentDomains = new LinkedHashSet<>();

		/*
		 * Gather hosts from "host" and "hosts" attributes from app and root nodes
		 */
		String host = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAIN_PROP, String.class);
		if (host != null) {
			currentHosts.add(host);
		}
		List<?> hostsList = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAINS_PROP, List.class);
		if (hostsList != null) {
			for (Object o : hostsList) {
				if (o instanceof String) {
					currentHosts.add((String) o);
				}
			}
		}

		/*
		 * Gather domains from 'domain' and 'domains' attributes from app and root nodes
		 */
		String domain = getAbsoluteValue(ApplicationManifestHandler.DOMAIN_PROP, String.class);
		if (domain != null) {
			currentDomains.add(domain);
		}
		List<?> domainsList = getAbsoluteValue(ApplicationManifestHandler.DOMAINS_PROP, List.class);
		if (domainsList != null) {
			for (Object o : domainsList) {
				if (o instanceof String) {
					currentDomains.add((String) o);
				}
			}
		}

		boolean match = false;
		Boolean noHost = getAbsoluteValue(ApplicationManifestHandler.NO_HOSTNAME_PROP, Boolean.class);
		Boolean randomRoute = getAbsoluteValue(ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class);
		Boolean noRoute = getAbsoluteValue(ApplicationManifestHandler.NO_ROUTE_PROP, Boolean.class);

		if (otherNoRoute) {
			if (!Boolean.TRUE.equals(noRoute)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.NO_ROUTE_PROP, true, false, Boolean.class);

				if (getPropertyValue(appNode, ApplicationManifestHandler.NO_HOSTNAME_PROP, Boolean.class) != null) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}

				if (getPropertyValue(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class) != null) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
				}
			} else {
				match = true;
			}
		} else {
			if (Boolean.TRUE.equals(noRoute)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.NO_ROUTE_PROP, false, false, Boolean.class);
			}

			if (otherNoHostname) {
				if (!Boolean.TRUE.equals(noHost)) {
					me.addChild(createEdit(appNode, Boolean.TRUE, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}
			} else {
				/*
				 * There is at least a host in the deployment properties. Remove
				 * "no-hostname" attribute if there is one from the application
				 * node. Don't care if it's in the root or anywhere else
				 */
				if (getPropertyValue(appNode, ApplicationManifestHandler.NO_HOSTNAME_PROP, Boolean.class) != null) {
					me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.NO_HOSTNAME_PROP));
				}
			}

			if (Boolean.TRUE.equals(randomRoute) && otherHosts.size() == 1 && otherDomains.size() == 1 && currentHosts.isEmpty()) {
				match = true;
			} else if (getPropertyValue(appNode, ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class) != null) {
				me.addChild(createEdit(appNode, (String) null, ApplicationManifestHandler.RANDOM_ROUTE_PROP));
			}

			if (currentHosts.isEmpty() && !Boolean.TRUE.equals(noHost)) {
				currentHosts.add(getAppName());
			}

			if (currentDomains.isEmpty() && !domains.isEmpty()) {
				currentDomains.add(cloudData.getDefaultDomain());
			}
		}

		if (!match && (!currentHosts.equals(otherHosts) || !currentDomains.equals(otherDomains))) {
			generateEditForHostsAndDomains(me, currentHosts, currentDomains, otherHosts, otherDomains);
		}

	}

	private void generateEditForHostsAndDomains(MultiTextEdit me, Set<String> currentHosts, Set<String> currentDomains, Set<String> otherHosts, Set<String> otherDomains) {
		/*
		 * Calculate current 'host' attrbute value
		 */
		String host = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAIN_PROP, String.class);
		if (otherHosts.size() == 1) {
			/*
			 * Only one host for deployment props
			 */
			String otherHost = otherHosts.iterator().next();
			/*
			 * If calculated host is different from deployment props create edit
			 */
			if (host == null || !otherHost.equals(host)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.SUB_DOMAIN_PROP, otherHost, null, String.class);
			}
			/*
			 * Ensure the deployment props hosts are empty since the difference has been dealt with here
			 */
			otherHosts.clear();
		} else {
			/*
			 * Deployment props have more than one host.
			 * Check if current "host" attribute value is one of the hosts from deployment props
			 */
			if (host != null && !otherHosts.remove(host)) {
				/*
				 * If current 'host' attribute value is not contained in
				 * deployment props hosts then ensure "host" attribute value is
				 * cleared
				 */
				getDifferenceForEntry(me, ApplicationManifestHandler.SUB_DOMAIN_PROP, null, null, String.class);
			}
		}
		/*
		 * Calculate edit for hosts list
		 */
		getDifferencesForList(me, ApplicationManifestHandler.SUB_DOMAINS_PROP, new ArrayList<>(otherHosts), String.class);

		/*
		 * Calculate current 'domain' attribute value
		 */
		String domain = getAbsoluteValue(ApplicationManifestHandler.DOMAIN_PROP, String.class);
		if (otherDomains.size() == 1) {
			/*
			 * Only one domain for deployment props
			 */
			String otherDomain = otherDomains.iterator().next();
			/*
			 * If calculated domain is different from deployment props create edit
			 */
			if (domain == null || !otherDomain.equals(domain)) {
				getDifferenceForEntry(me, ApplicationManifestHandler.DOMAIN_PROP, otherDomain, null, String.class);
			}
			/*
			 * Ensure the deployment props domains are empty since the difference has been dealt with here
			 */
			otherDomains.clear();
		} else {
			/*
			 * Deployment props have more than one domain.
			 * Check if current "domain" attribute value is one of the domains from deployment props
			 */
			if (domain != null && !otherDomains.remove(domain)) {
				/*
				 * If current 'domain' attribute value is not contained in
				 * deployment props domains then ensure "domain" attribute value is
				 * cleared
				 */
				getDifferenceForEntry(me, ApplicationManifestHandler.DOMAIN_PROP, null, null, String.class);
			}
		}
		/*
		 * Calculate edit for domains list
		 */
		getDifferencesForList(me, ApplicationManifestHandler.DOMAINS_PROP, new ArrayList<>(otherDomains), String.class);
	}

	/**
	 * Creates text edit for mapping node tuples where property and value are
	 * scalars (i.e. value is either string or some primitive type)
	 *
	 * @param parent
	 *            the parent MappingNode
	 * @param otherValue
	 *            the new value for the tuple
	 * @param property
	 *            tuple's key
	 * @return the text edit
	 */
	private TextEdit createEdit(MappingNode parent, Object otherValue, String property) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			if (otherValue != null) {
				StringBuilder serializedValue = serialize(property, otherValue);
				boolean[] postIndent = new boolean[] { true };
				int position = positionToAppendAt(parent, postIndent);
				if (postIndent[0]) {
					postIndent(serializedValue, getDefaultOffset());
				} else {
					preIndent(serializedValue, getDefaultOffset());
				}
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			if (otherValue == null) {
				/*
				 * Delete the tuple including the line break if possible
				 */
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
				/*
				 * k1: v1
				 * k-delete: v-delete
				 * ^                 ^
				 * start index       end index
				 * k2: v2
				 *
				 * Extend end index to position of k2 and leave start index where it was with correct indent
				 *
				 * However, if it' the last tuple in the map than just delete it and leave the line with the indent in the beginning for now.
				 */
				if (parent.getValue().get(parent.getValue().size() - 1) != tuple) {
					for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end)); end++);
				}
				return new DeleteEdit(start, end - start);
			} else {
				/*
				 * Replace the current value (whether it's a scalr value or anything else without affecting the white space
				 */
				return new ReplaceEdit(tuple.getValueNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex() - tuple.getValueNode().getStartMark().getIndex(), String.valueOf(otherValue));
//				return createReplaceEditWithoutWhiteSpace(tuple.getValueNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex() - 1,
//						String.valueOf(otherValue));
			}
		}
		return null;
	}

	/**
	 * Calculates position to append entries to the map node. Also provides a
	 * hint on how to properly append entries. If entries are to be appended
	 * after the last entry in the map node then they need to be all
	 * pre-indented, and post-indented otherwise
	 *
	 * @param m the map node
	 * @param postIndent the post- or pre- indent calculated hint
	 * @return the index to append entries
	 */
	private int positionToAppendAt(MappingNode m, boolean[] postIndent) {
		/*
		 * Check if there is a name attribute in the map node (case of application node) and make the end index of tha 'name: XXX' tuple as the index to append
		 */
		for (NodeTuple tuple : m.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode
					&& ApplicationManifestHandler.NAME_PROP.equals(((ScalarNode) tuple.getKeyNode()).getValue())) {
				int index = tuple.getValueNode().getEndMark().getIndex();
				for (; index > 0 && index < content.length() && Character.isWhitespace(content.charAt(index)); index++)
					;
				postIndent[0] = m.getValue().get(m.getValue().size() - 1) != tuple;
				return index;
			}
		}
		postIndent[0] = true;
		return m.getStartMark().getIndex();
	}

	private <T> TextEdit createEdit(MappingNode parent, List<T> otherValue, String property, Class<T> type) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			if (otherValue != null && !otherValue.isEmpty()) {
				StringBuilder serializedValue = serialize(property, otherValue);
//				postIndent(serializedValue, getDefaultOffset());
//				int position = positionToAppendAt(parent);
				boolean[] postIndent = new boolean[] { true };
				int position = positionToAppendAt(parent, postIndent);
				if (postIndent[0]) {
					postIndent(serializedValue, getDefaultOffset());
				} else {
					preIndent(serializedValue, getDefaultOffset());
				}
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			if (otherValue == null || otherValue.isEmpty()) {
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
//				int index = parent.getValue().indexOf(tuple);
//				if (!(index > 0 && parent.getValue().get(index - 1).getValueNode() instanceof CollectionNode)) {
//					/*
//					 * If previous tuple is not a map or list then try to remove the preceding line break
//					 */
//					for (; start > 0 && Character.isWhitespace(content.charAt(start - 1)) && content.charAt(start - 1) != '\n'; start--);
//				}
				for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end)); end++);

				return new DeleteEdit(start, end - start);
			} else {
				Node sequence = tuple.getKeyNode();
				if (tuple.getValueNode() instanceof SequenceNode) {
					SequenceNode sequenceValue = (SequenceNode) tuple.getValueNode();
					MultiTextEdit me = new MultiTextEdit();
					Set<T> others = new LinkedHashSet<>();
					others.addAll(otherValue);

					/*
					 * Remember the ending position of the last entry that remains in the list
					 */
					int appendIndex = sequenceValue.getEndMark().getIndex();
					for (int index = 0; index < sequenceValue.getValue().size(); index++) {
						Node n = sequenceValue.getValue().get(index);
						T value  = getValue(n, type);
						if (others.contains(value)) {
							// Entry exists, do nothing, just update the end position to append the missing entries
							others.remove(value);
							appendIndex = n.getEndMark().getIndex();
						} else {
							/*
							 * skip "- " prefix for the start position
							 */
							int start = n.getStartMark().getIndex();
							for (; start > 0 && content.charAt(start) != '-' && content.charAt(start) != '\n'; start--);
							int end = n.getEndMark().getIndex();

							// If entry is object in the list don't remove the indent for the next entry in YAML (if there is a next YAML entry)
							/*
							 * - e: entry-1
							 *   ^-start
							 * - e: entry-2
							 * ^-end
							 */
							if (n instanceof MappingNode && root.getEndMark().getIndex() != end) {
								if (parent.getEndMark().getIndex() == n.getEndMark().getIndex()) {
									// last entry in the list but not the last yaml piece in the document
									end -= (parent.getStartMark().getColumn() - appNode.getStartMark().getColumn());
								} else {
									end -= sequenceValue.getStartMark().getColumn();
								}
							}
							/*
							 *  "- entry" start=2, end=7, need to include '\n' in the deletion
							 */
							DeleteEdit deleteEdit = createDeleteEditIncludingLine(start, end);
							appendIndex = deleteEdit.getOffset();
							me.addChild(deleteEdit);
						}
					}
					/*
					 * TODO: verify that further appendIndex manipulations are necessary!
					 */
					/*
					 * Offset appendIndex to leave the line break for the previous entry in place. jump over spacing and line break.
					 */
					for (; appendIndex > 0 && appendIndex < content.length() && Character.isWhitespace(content.charAt(appendIndex)) && content.charAt(appendIndex - 1) != '\n'; appendIndex++);
					/*
					 * Add a line break if append index is not starting right after line break.
					 */
					if (!others.isEmpty() && content.charAt(appendIndex - 1) != '\n') {
						me.addChild(new ReplaceEdit(appendIndex, 0, System.lineSeparator()));
					}

					/*
					 * Add missing entries
					 */
					for (T s : others) {
						me.addChild(new ReplaceEdit(appendIndex, 0, serializeListEntry(s, sequenceValue.getStartMark().getColumn()).toString()));
					}
					return me.hasChildren() ? me : null;
				} else {
					/*
					 * Sequence is expected but was something else. Replace the
					 * whole tuple. Don't touch the whitespace when replacing -
					 * it looks good
					 */
					StringBuilder s = serialize(property, otherValue);
					preIndent(s, sequence.getStartMark().getColumn());
					return createReplaceEditWithoutWhiteSpace(sequence.getStartMark().getIndex(),
							tuple.getValueNode().getEndMark().getIndex() - 1,
							s.toString().trim());
				}
			}
		}
		return null;
	}

	private TextEdit createEdit(MappingNode parent, Map<String, String> otherValue, String property) {
		NodeTuple tuple = findNodeTuple(parent, property);
		if (tuple == null) {
			/*
			 * No tuple found for the key
			 */
			if (otherValue != null && !otherValue.isEmpty()) {
				/*
				 * If other value is something that can be serialized, serialize the key and other value and put in the YAML
				 */
				StringBuilder serializedValue = serialize(property, otherValue);
//				postIndent(serializedValue, getDefaultOffset());
//				int position = positionToAppendAt(parent);
				boolean[] postIndent = new boolean[] { true };
				int position = positionToAppendAt(parent, postIndent);
				if (postIndent[0]) {
					postIndent(serializedValue, getDefaultOffset());
				} else {
					preIndent(serializedValue, getDefaultOffset());
				}
				return new ReplaceEdit(position, 0, serializedValue.toString());
			}
		} else {
			/*
			 * Tuple with the string key is found
			 */
			if (otherValue == null || otherValue.isEmpty()) {
				/*
				 * Delete the found tuple since other value is null or empty
				 */
				int start = tuple.getKeyNode().getStartMark().getIndex();
				int end = tuple.getValueNode().getEndMark().getIndex();
				return new DeleteEdit(start, end - start);
//				return createDeleteEditIncludingLine(tuple.getKeyNode().getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex());
			} else {
				/*
				 * Tuple is found, so the key node is there, check the value node
				 */
				Node map = tuple.getKeyNode();
				if (tuple.getValueNode() instanceof MappingNode) {
					/*
					 * Value node is a map node. Go over every entry in the map to calculate differences
					 */
					MappingNode mapValue = (MappingNode) tuple.getValueNode();
					MultiTextEdit e = new MultiTextEdit();
					Map<String, String> leftOver = new LinkedHashMap<>();
					leftOver.putAll(otherValue);
					int appendIndex = mapValue.getStartMark().getIndex();
					for (NodeTuple t : mapValue.getValue()) {
						if (t.getKeyNode() instanceof ScalarNode && t.getValueNode() instanceof ScalarNode) {
							ScalarNode key = (ScalarNode) t.getKeyNode();
							ScalarNode value = (ScalarNode) t.getValueNode();
							String newValue = leftOver.get(key.getValue());
							if (newValue == null) {
								/*
								 * Delete the tuple if newValue is null. Delete including the line if necessary
								 */
								e.addChild(createDeleteEditIncludingLine(key.getStartMark().getIndex(), value.getEndMark().getIndex()));
							} else if (!value.getValue().equals(newValue)) {
								/*
								 * Key is there but value is different, so edit the value
								 */
								e.addChild(new ReplaceEdit(value.getStartMark().getIndex(), value.getEndMark().getIndex() - value.getStartMark().getIndex(), newValue));
								appendIndex = value.getEndMark().getIndex();
							} else {
								appendIndex = value.getEndMark().getIndex();
							}
							leftOver.remove(key.getValue());
						}
					}
					/*
					 * Offset appendIndex to leave the line break for the previous entry in place. jump over spacing and line break.
					 */
					for (; appendIndex > 0 && appendIndex < content.length() && Character.isWhitespace(content.charAt(appendIndex)) && content.charAt(appendIndex - 1) != '\n'; appendIndex++);
					/*
					 * Add a line break if append index is not starting right after line break.
					 */
					if (!leftOver.isEmpty() && content.charAt(appendIndex - 1) != '\n') {
						e.addChild(new ReplaceEdit(appendIndex, 0, System.lineSeparator()));
					}
					/*
					 * Add remaining unmatched entries
					 */
					for (Map.Entry<String, String> entry : leftOver.entrySet()) {
						StringBuilder serializedValue = serialize(entry.getKey(), entry.getValue());
						preIndent(serializedValue, mapValue.getStartMark().getColumn());
						e.addChild(new ReplaceEdit(appendIndex, 0, serializedValue.toString()));
					}
					return e.hasChildren() ? e : null;
				} else {
					/*
					 * Map is expected but was something else. Replace the
					 * whole tuple. Don't touch the whitespace when replacing -
					 * it looks good
					 */
					StringBuilder serializedValue = serialize(property, otherValue);
					preIndent(serializedValue, map.getStartMark().getColumn());
					return createReplaceEditWithoutWhiteSpace(map.getStartMark().getIndex(), tuple.getValueNode().getEndMark().getIndex() - 1, serializedValue.toString().trim());
				}
			}
		}
		return null;
	}

	private StringBuilder serialize(String property, Object value) {
		Map<Object, Object> obj = new HashMap<>();
		obj.put(property, value);
		return new StringBuilder(yaml.dump(obj));
	}

	private StringBuilder postIndent(StringBuilder s, int offset) {
		char[] indent = new char[offset];
		for (int i = 0; i < offset; i++) {
			indent[i] = ' ';
		}
		for (int i = 0; i < s.length(); ) {
			if (s.charAt(i) == '\n') {
				s.insert(i + 1, indent);
				i += indent.length;
			}
			i++;
		}
		return s;
	}

	private StringBuilder serializeListEntry(Object obj, int offset) {
		StringBuilder s = new StringBuilder(yaml.dump(Collections.singletonList(obj)));
		if (offset > 0) {
			preIndent(s, offset);
		}
		return s;
	}

	private StringBuilder preIndent(StringBuilder s, int offset) {
		char[] indent = new char[offset];
		for (int i = 0; i < offset; i++) {
			indent[i] = ' ';
		}
		int lineLength = 0;
		for (int i = 0; i < s.length(); ) {
			if (s.charAt(i) == '\n') {
				if (lineLength > 0) {
					s.insert(i - lineLength, indent);
					i += indent.length;
					lineLength = 0;
				}
			} else {
				lineLength++;
			}
			i++;
		}
		if (lineLength > 0) {
			s.insert(s.length() - lineLength, indent);
			lineLength = 0;
		}
		return s;
	}

	private DeleteEdit createDeleteEditIncludingLine(int start, int end) {
		if (content != null) {
			for (; start > 0 && Character.isWhitespace(content.charAt(start - 1)) && content.charAt(start - 1) != '\n'; start--);
			for (; end > 0 && end < content.length() && Character.isWhitespace(content.charAt(end)) && content.charAt(end - 1) != '\n'; end++);
		}
		return new DeleteEdit(start, end - start);
	}

	private ReplaceEdit createReplaceEditWithoutWhiteSpace(int start, int end, String text) {
		for (; start < content.length() && Character.isWhitespace(content.charAt(start)); start++);
		for (; end >= start && Character.isWhitespace(content.charAt(end)); end--);
		return new ReplaceEdit(start, end - start + 1, text);
	}

	private int getDefaultOffset() {
		if (appNode == null) {
			if (applicationsValueNode == null) {
				return 0;
			} else {
				return applicationsValueNode.getStartMark().getColumn();
			}
		} else {
			return appNode.getStartMark().getColumn();
		}
	}

	public List<String> getRoutes() {
		List<Map<?,?>> routes = getAbsoluteValue(ApplicationManifestHandler.ROUTES_PROP, List.class);
		if (routes != null) {
			return routes.stream()
					.map(routeObj -> routeObj.get(ApplicationManifestHandler.ROUTE_PROP))
					.filter(o -> o instanceof String)
					.map(o -> (String) o)
					.collect(Collectors.toList());
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getUris() {

		List<String> routes = getRoutes();
		if (routes != null) {
			return ImmutableSet.copyOf(routes);
		} else {
			Boolean noRoute = getAbsoluteValue(ApplicationManifestHandler.NO_ROUTE_PROP, Boolean.class);
			if (Boolean.TRUE.equals(noRoute)) {
				return Collections.emptySet();
			}

			List<CFCloudDomain> domains = cloudData.getDomains();
			LinkedHashSet<String> hostsSet = new LinkedHashSet<>();
			LinkedHashSet<String> domainsSet = new LinkedHashSet<>();

			/*
			 * Gather domains from app node from 'domain' and 'domains' attributes
			 */
			String domain = getAbsoluteValue(ApplicationManifestHandler.DOMAIN_PROP, String.class);
			if (domain != null) {
				domainsSet.add(domain);
			}
			List<String> domainsList = getAbsoluteValue(ApplicationManifestHandler.DOMAINS_PROP, List.class);
			if (domainsList != null) {
				domainsSet.addAll(domainsList);
			}

			/*
			 * Gather hosts from app node from 'host' and 'hosts'
			 * attributes.
			 */
			String host = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAIN_PROP, String.class);
			if (host != null) {
				hostsSet.add(host);
			}
			List<?> hostsList = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAINS_PROP, List.class);
			if (hostsList != null) {
				for (Object o : hostsList) {
					if (o instanceof String) {
						hostsSet.add((String)o);
					}
				}
			}

			/*
			 * If no host names found check for "random-route: true" and
			 * "no-hostname: true" otherwise take app name as the host name
			 */
			if (hostsSet.isEmpty()) {
				Boolean randomRoute = getAbsoluteValue(ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class);
				if (Boolean.TRUE.equals(randomRoute)) {
					hostsSet.add(ApplicationManifestHandler.RANDOM_VAR);
					domainsSet.clear();
					domainsSet.add(cloudData.getDefaultDomain());
				} else {
					Boolean noHostname = getAbsoluteValue(ApplicationManifestHandler.NO_HOSTNAME_PROP, Boolean.class);
					if (!Boolean.TRUE.equals(noHostname)) {
						hostsSet.add(getAppName());
					}
				}
			}

			/*
			 * Set a domain if they are still empty
			 */
			if (domainsSet.isEmpty()) {
				domainsSet.add(cloudData.getDefaultDomain());
			}

			/*
			 * Compose URIs for application based on hosts and domains
			 */
			Set<String> uris = new HashSet<>();
			for (String d : domainsSet) {
				if (hostsSet.isEmpty()) {
					uris.add(CFRoute.builder().domain(d).build().getRoute());
				} else {
					for (String h : hostsSet) {
						uris.add(CFRoute.builder().host(h).domain(d).build().getRoute());
					}
				}
			}

			return uris;
		}
	}

	@Override
	public int getDiskQuota() {
		String quotaStringValue = getAbsoluteValue(ApplicationManifestHandler.DISK_QUOTA_PROP, String.class);
		if (quotaStringValue != null) {
			try {
				return ApplicationManifestHandler.convertMemory(quotaStringValue);
			} catch (CoreException e) {
				Log.log(e);
			}
		}
		return DeploymentProperties.DEFAULT_MEMORY;
	}

	public static <V> V getPropertyValue(final Node n, final String key, Class<V> parameter) {
        Node node = YamlGraphDeploymentProperties.findValueNode(n, key);
        return getValue(node, parameter);
	}

	@SuppressWarnings("unchecked")
	public static <V> V getValue(Node node, Class<V> parameter) {
		return (V) new Constructor(parameter) {
			@Override
			public Object getSingleData(Class<?> type) {
			      // Ensure that the stream contains a single document and construct it
		        if (node != null) {
		        	if (type != null) {
		        		node.setTag(new Tag(type));
		        	} else {
		        		node.setTag(rootTag);
		        	}
		        	return constructObject(node);
		        }
		        return null;
		    }
		}.getSingleData(parameter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <V> V getAbsoluteValue(String key, Class<V> parameter) {
		V v = getPropertyValue(appNode, key, parameter);
		if (Collection.class.isAssignableFrom(parameter)) {
			if (root != appNode) {
				V rootV = getPropertyValue(root, key, parameter);
				if (rootV != null) {
					if (v != null){
						((Collection) rootV).addAll((Collection) v);
					}
					v = rootV;
				}
			}
		} else if (Map.class.isAssignableFrom(parameter)) {
			if (root != appNode) {
				V rootV = getPropertyValue(root, key, parameter);
				if (rootV != null) {
					if (v != null){
						((Map) rootV).putAll((Map) v);
					}
					v = rootV;
				}
			}
		} else if (v == null) {
			if (root != appNode) {
				v = getPropertyValue(root, key, parameter);
			}
		}
		return v;
	}

	private static boolean isLegacyHostDomainManifestYaml(Node n) {
		if (isLegacyHostDomainManifestYamlNode(n)) {
			return true;
		} else {
			Node applicationsObj = findValueNode(n, ApplicationManifestHandler.APPLICATIONS_PROP);
			if (applicationsObj instanceof SequenceNode) {
				return ((SequenceNode) applicationsObj).getValue().stream()
						.map(o -> (Node) o)
						.filter(YamlGraphDeploymentProperties::isLegacyHostDomainManifestYamlNode)
						.findFirst().isPresent();
			}
		}
		return false;
	}

	private static boolean isLegacyHostDomainManifestYamlNode(Node node) {
		return findValueNode(node, ApplicationManifestHandler.DOMAIN_PROP) != null
				|| findValueNode(node, ApplicationManifestHandler.SUB_DOMAIN_PROP) != null
				|| findValueNode(node, ApplicationManifestHandler.DOMAINS_PROP) != null
				|| findValueNode(node, ApplicationManifestHandler.SUB_DOMAINS_PROP) != null
				|| findValueNode(node, ApplicationManifestHandler.NO_HOSTNAME_PROP) != null;
	}

	public String getRawHost() {
		return getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAIN_PROP, String.class);
	}
	public List<String> getRawHosts() {
		List<?> hostsList = getAbsoluteValue(ApplicationManifestHandler.SUB_DOMAINS_PROP, List.class);
		if (hostsList != null) {
			List<String> currentHosts = new ArrayList<>();
			for (Object o : hostsList) {
				if (o instanceof String) {
					currentHosts.add((String) o);
				}
			}
			return ImmutableList.copyOf(currentHosts);
		}
		return null;
	}

	public List<String> getRawDomains() {
		List<?> hostsList = getAbsoluteValue(ApplicationManifestHandler.DOMAINS_PROP, List.class);
		if (hostsList != null) {
			List<String> currentHosts = new ArrayList<>();
			for (Object o : hostsList) {
				if (o instanceof String) {
					currentHosts.add((String) o);
				}
			}
			return ImmutableList.copyOf(currentHosts);
		}
		return null;
	}

	public boolean getRawNoRoute() {
		Boolean v = getAbsoluteValue(ApplicationManifestHandler.NO_ROUTE_PROP, Boolean.class);
		return v==null ? false : v;
	}

	public boolean getRawRandomRoute() {
		Boolean v = getAbsoluteValue(ApplicationManifestHandler.RANDOM_ROUTE_PROP, Boolean.class);
		return v==null ? false : v;
	}

	public String getRawDomain() {
		return getAbsoluteValue(ApplicationManifestHandler.DOMAIN_PROP, String.class);
	}

	public boolean getRawNoHost() {
		Boolean v = getAbsoluteValue(ApplicationManifestHandler.NO_HOSTNAME_PROP, Boolean.class);
		return v==null ? false : v;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getBuildpacks() {
		List<String> buildpacks = getAbsoluteValue(ApplicationManifestHandler.BUILDPACKS_PROP, List.class);
		return buildpacks == null ? new ArrayList<>() : buildpacks;
	}

}
