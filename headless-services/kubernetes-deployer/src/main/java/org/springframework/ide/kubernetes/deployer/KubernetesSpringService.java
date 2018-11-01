/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes.deployer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.kubernetes.container.DockerImageHandler;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesSpringService implements KubernetesService {

	private final DockerImageHandler dockerHandler;
	private final String RESULT_LOG_PREFIX = "STS KUBERNETES";
	private static final String APP_SELECTOR = "app";

	public static final String SERVER_PORT_KEY = "server.port";

	protected final Log logger = LogFactory.getLog(getClass().getName());

	protected KubernetesClient client;

	private final KubernetesClientFactory clientFactory;
	

	@Autowired
	public KubernetesSpringService(KubernetesClientFactory clientFactory, DockerImageHandler dockerHandler) {
		this.clientFactory = clientFactory;
		this.dockerHandler = dockerHandler;
	}

	@Override
	public DeploymentResult deploy(DeploymentDefinition definition) throws Exception {
		if (definition.getJarPath() != null) {
			dockerHandler.createImageAndPush(definition.getJarPath(), definition.getDockerImage());
			DeploymentResult results = performDeploy(definition);
			logUris(results.getUris());
			return results;
		} else {
			throw new Exception("Unable to deploy application. No application jar specified.");
		}
	}

	@Override
	public void undeploy(DeploymentDefinition definition) throws Exception {
		performUndeploy(definition);
	}
	
	@Override
	public ServicesResult getServices() throws Exception {
		List<String> serviceNames = new ArrayList<>();
		ServiceList serviceList = client().services().list();

		if (serviceList != null && serviceList.getItems() != null) {
			for (Service service : serviceList.getItems()) {
				serviceNames.add(service.getMetadata().getName());
			}
		}
		return new ServicesResult(serviceNames);
	}

	private DeploymentResult performDeploy(DeploymentDefinition definition) throws Exception {

		logger.info("Deploying image: " + definition.getDockerImage().getImage());

		String appId = getDeploymentId(definition);
		logger.debug(String.format("Deploying app: %s", appId));

		int containerPort = configureExternalPort(definition);

		createService(appId, definition);
		logger.info(String.format("Created Service: %s.", appId));

		createDeployment(appId, definition, containerPort);
		logger.info(String.format("Created Deployment: %s.", appId));

		if (definition.useNodePort()) {
			List<String> uris = getNodePortUris(appId);
			if (!uris.isEmpty()) {
				return new DeploymentResult(uris);
			} else {
				List<Integer> nodePorts = getNodePorts(appId);
				StringWriter writer = new StringWriter();
				writer.append("Service available at the following node ports: ");
				for (Integer port : nodePorts) {
					writer.append("" + port);
				}
				logger.info(writer.toString());
				return DeploymentResult.EMPTY;
			}
		} else {
			return new DeploymentResult(getAllUris());
		}
	}



	private void performUndeploy(DeploymentDefinition definition) throws Exception {
		String appId = getDeploymentId(definition);

		logger.debug(String.format("Undeploying: %s", appId));
		Boolean deleted = client().services().withName(appId).delete();
		logger.info(String.format("Deleted Service for: %s %b", appId, deleted));
		deleted = client().replicationControllers().withName(appId).delete();
		if (deleted) {
			logger.info(String.format("Deleted Replication Controller for: %s", appId));
		}
		deleted = client().extensions().deployments().withName(appId).delete();
		if (deleted) {
			logger.info(String.format("Deleted Deployment for: %s", appId));
		}
	}

	protected int configureExternalPort(final DeploymentDefinition request) {
		return request.getContainerPort();
	}

	protected String getDeploymentId(DeploymentDefinition request) {
		String deploymentId = String.format("%s", request.getAppName());

		// Kubernetes does not allow . in the name and does not allow uppercase in the
		// name
		return deploymentId.replace('.', '-').toLowerCase();
	}

	private Deployment createDeployment(String appId, DeploymentDefinition request, int externalPort) {

		int replicas = request.getReplicaCount();

		Map<String, String> idMap = new HashMap<>();
		idMap.put(APP_SELECTOR, appId);

		logger.info(String.format("Creating Deployment: %s. Please wait...", appId));

		Deployment d = new DeploymentBuilder().withNewMetadata().withName(appId).withLabels(idMap).endMetadata()
				.withNewSpec().withReplicas(replicas).withNewTemplate().withNewMetadata().withLabels(idMap)
				.endMetadata().withSpec(createPodSpec(appId, request, externalPort, false)).endTemplate().endSpec()
				.build();

		return client().extensions().deployments().create(d);
	}

	protected void createService(String appId, DeploymentDefinition definition) {
		ServiceSpecBuilder spec = new ServiceSpecBuilder();

		ServicePort servicePort = definition.getServicePort();

		Map<String, String> idMap = getServiceLabels(appId);

		if (definition.useNodePort()) {
			logger.info("Using 'NodePort' for service");
			spec.withType("NodePort");

			if (definition.getServicePort() != null) {
				if (definition.getServicePort().getPort() != null) {
					logger.info("Setting service port: " + definition.getServicePort().getPort());
				}

				if (definition.getServicePort().getTargetPort() != null) {
					logger.info(
							"Setting service target port: " + definition.getServicePort().getTargetPort().getIntVal());
				}
			}
		}

		spec.withSelector(idMap).addNewPortLike(servicePort).endPort();

		logger.info(String.format("Creating Service: %s . Please wait...", appId));

		client().services().inNamespace(client().getNamespace()).createNew().withNewMetadata().withName(appId)
				.withLabels(idMap).endMetadata().withSpec(spec.build()).done();
	}

	protected PodSpec createPodSpec(String appId, DeploymentDefinition definition, Integer port, boolean neverRestart) {
		PodSpecBuilder podSpec = new PodSpecBuilder();

		boolean hostNetwork = definition.getHostNetwork();

		Container container = clientFactory.createContainer(definition);

		String pullPolicy = definition.getImagePullPolicy();
		container.setImagePullPolicy(pullPolicy);

		if (hostNetwork) {
			podSpec.withHostNetwork(true);
		}
		podSpec.addToContainers(container);

		if (neverRestart) {
			podSpec.withRestartPolicy(definition.getRestartPolicy());
		}

		return podSpec.build();
	}

	private Map<String, String> getServiceLabels(String appId) {
		HashMap<String, String> labels = new HashMap<String, String>();
		labels.put(APP_SELECTOR, appId);
		return labels;
	}

	private List<String> getNodePortUris(String appId) {
		List<String> serviceUris = new ArrayList<>();
		List<String> nodeUris = getNodeExternalIps();
		List<Integer> nodePorts = getNodePorts(appId);

		for (String nodeUri : nodeUris) {
			for (Integer port : nodePorts) {
				serviceUris.add(nodeUri + ':' + port);
			}
		}

		return serviceUris;
	}

	private List<String> getNodeExternalIps() {
		List<String> nodeUris = new ArrayList<>();
		List<NodeAddress> addresses = getNodeAddresses();
		for (NodeAddress address : addresses) {
			if ("ExternalIP".equals(address.getType())) {
				String uri = address.getAddress();
				if (!nodeUris.contains(uri)) {
					nodeUris.add(uri);
				}
			}
		}
		return nodeUris;
	}

	private List<String> getAllUris() {
		List<String> nodeUris = new ArrayList<>();
		List<NodeAddress> addresses = getNodeAddresses();
		for (NodeAddress address : addresses) {
			String uri = address.getAddress();
			if (!nodeUris.contains(uri)) {
				nodeUris.add(uri);
			}
		}
		return nodeUris;
	}

	private List<NodeAddress> getNodeAddresses() {
		List<Node> items = client().nodes().list().getItems();
		List<NodeAddress> all = new ArrayList<NodeAddress>();
		if (items != null) {
			for (Node node : items) {
				NodeStatus status = node.getStatus();
				List<NodeAddress> nodeAddresses = status.getAddresses();
				if (all != null) {
					for (NodeAddress address : nodeAddresses) {
						all.add(address);
					}
				}
			}
		}
		return all;
	}

	private List<Integer> getNodePorts(String appId) {
		List<Integer> nodePorts = new ArrayList<>();

		try {
			Service s = client().services().withName(appId).waitUntilReady(20, TimeUnit.SECONDS);

			ServiceSpec sp = s.getSpec();
			List<ServicePort> ports = sp.getPorts();
			if (ports != null) {
				for (ServicePort servicePort : ports) {
					Integer nodePort = servicePort.getNodePort();
					if (nodePort != null) {
						nodePorts.add(nodePort);
					}
				}
			}

		} catch (InterruptedException e) {
			logger.error("Failure while waiting for service to be ready", e);
		}
		return nodePorts;
	}
	
	private void logUris(List<String> uris) {
		if (uris != null && !uris.isEmpty()) {
			logger.info(RESULT_LOG_PREFIX + "- URI: " + "http://" + uris.get(0) + '\n');
		}
	}
	

	private KubernetesClient client() {
		if (this.client == null) {
			String nameSpace = System.getenv("KUBERNETES_NAMESPACE") != null ? System.getenv("KUBERNETES_NAMESPACE")
					: "default";
			this.client = clientFactory.getKubernetesClient(nameSpace);
		}
		return this.client;

	}

}
