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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.kubernetes.container.ContainerFactory;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesAppDeployer implements AppDeployer {

	private static final String APP_SELECTOR = "app";

	public static final String SERVER_PORT_KEY = "server.port";

	protected final Log logger = LogFactory.getLog(getClass().getName());

	protected KubernetesClient client;

	private final ContainerFactory containerFactory;

	private final KubernetesClientFactory clientFactory;

	@Autowired
	public KubernetesAppDeployer(ContainerFactory containerFactory, KubernetesClientFactory clientFactory) {
		this.containerFactory = containerFactory;
		this.clientFactory = clientFactory;
	}

	private KubernetesClient client() {
		if (this.client == null) {
			String nameSpace = System.getenv("KUBERNETES_NAMESPACE") != null ? System.getenv("KUBERNETES_NAMESPACE")
					: "default";
			this.client = clientFactory.getKubernetesClient(nameSpace);
		}
		return this.client;

	}

	@Override
	public String deploy(DeploymentDefinition definition) throws Exception {
		logger.info("Deploying from application image: " + definition.getDockerImage().getUri());

		String appId = createDeploymentId(definition);
		logger.debug(String.format("Deploying app: %s", appId));

		int containerPort = configureExternalPort(definition);

		createService(appId, definition);
		logger.info(String.format("Created Service: %s.", appId));

		createDeployment(appId, definition, containerPort);
		logger.info(String.format("Created Deployment: %s.", appId));

		return appId;
	}

	protected Map<String, String> getAppSelector(String appId) {
		Map<String, String> selector = new HashMap<>();
		selector.put(APP_SELECTOR, appId);
		return selector;

	}

	@Override
	public void undeploy(DeploymentDefinition definition) throws Exception {
		String appName = definition.getAppName();
		logger.debug(String.format("Undeploying: %s", appName));
		Boolean deleted = client().services().withName(appName).delete();
		logger.info(String.format("Deleted Service for: %s %b", appName, deleted));
		deleted = client().replicationControllers().withName(appName).delete();
		if (deleted) {
			logger.info(String.format("Deleted Replication Controller for: %s", appName));
		}
		deleted = client().extensions().deployments().withName(appName).delete();
		if (deleted) {
			logger.info(String.format("Deleted Deployment for: %s", appName));
		}
	}

	protected int configureExternalPort(final DeploymentDefinition request) {
		return request.getContainerPort();
	}

	protected String createDeploymentId(DeploymentDefinition request) {
		String deploymentId = String.format("%s", request.getAppName());

		// Kubernetes does not allow . in the name and does not allow uppercase in the
		// name
		return deploymentId.replace('.', '-').toLowerCase();
	}

	private Deployment createDeployment(String appId, DeploymentDefinition request, int externalPort) {

		int replicas = request.getReplicaCount();

		Map<String, String> annotations = getPodAnnotations(request);

		Map<String, String> idMap = new HashMap<>();
		idMap.put(APP_SELECTOR, appId);

		logger.info(String.format("Creating Deployment: %s. Please wait...", appId));

		Deployment d = new DeploymentBuilder().withNewMetadata().withName(appId).withLabels(idMap).endMetadata()
				.withNewSpec().withReplicas(replicas).withNewTemplate().withNewMetadata().withLabels(idMap)
				.withAnnotations(annotations).endMetadata().withSpec(createPodSpec(appId, request, externalPort, false))
				.endTemplate().endSpec().build();

		return client().extensions().deployments().create(d);
	}

	protected void createService(String appId, DeploymentDefinition definition) {
		ServiceSpecBuilder spec = new ServiceSpecBuilder();

		ServicePort servicePort = definition.getServicePort();

		if (definition.createNodePort()) {
			logger.info("Using 'NodePort' for service");
			spec.withType("NodePort");

			if (definition.getServicePort().getPort() != null) {
				logger.info("Setting service port: " + definition.getServicePort().getPort());
			}

			if (definition.getServicePort().getTargetPort() != null) {
				logger.info("Setting service target port: " + definition.getServicePort().getTargetPort());
			}
		}

		Map<String, String> annotations = getServiceAnnotations(definition);
		Map<String, String> idMap = getServiceLabels(appId);

		spec.withSelector(idMap).addNewPortLike(servicePort).endPort();

		logger.info(String.format("Creating Service: %s . Please wait...", appId));

		client().services().inNamespace(client().getNamespace()).createNew().withNewMetadata().withName(appId)
				.withLabels(idMap).withAnnotations(annotations).endMetadata().withSpec(spec.build()).done();
	}

	protected PodSpec createPodSpec(String appId, DeploymentDefinition definition, Integer port, boolean neverRestart) {
		PodSpecBuilder podSpec = new PodSpecBuilder();

		boolean hostNetwork = definition.getHostNetwork();

		Container container = containerFactory.create(definition);

		ResourceRequirements req = new ResourceRequirements();
		req.setLimits(deduceResourceLimits(definition));
		req.setRequests(deduceResourceRequests(definition));
		container.setResources(req);
		String pullPolicy = definition.getImagePullPolicy();
		container.setImagePullPolicy(pullPolicy);

		if (hostNetwork) {
			podSpec.withHostNetwork(true);
		}
		podSpec.addToContainers(container);

		if (neverRestart) {
			podSpec.withRestartPolicy("Never");
		}

		return podSpec.build();
	}

	protected Map<String, Quantity> deduceResourceLimits(DeploymentDefinition definition) {
		String memory = definition.getMemory();
		String cpu = definition.getCpu();

		Map<String, Quantity> limits = new HashMap<String, Quantity>();

		if (memory != null) {
			limits.put("memory", new Quantity(memory));

		}
		if (cpu != null) {
			limits.put("cpu", new Quantity(cpu));
		}
		return limits;
	}

	protected Map<String, Quantity> deduceResourceRequests(DeploymentDefinition definition) {
		String memOverride = definition.getMemory();

		String cpuOverride = definition.getCpu();

		logger.debug("Using requests - cpu: " + cpuOverride + " mem: " + memOverride);

		Map<String, Quantity> requests = new HashMap<String, Quantity>();
		if (memOverride != null) {
			requests.put("memory", new Quantity(memOverride));
		}
		if (cpuOverride != null) {
			requests.put("cpu", new Quantity(cpuOverride));
		}
		return requests;
	}

	private Map<String, String> getServiceLabels(String appId) {
		HashMap<String, String> labels = new HashMap<String, String>();
		labels.put(APP_SELECTOR, appId);
		return labels;
	}

	private Map<String, String> getPodAnnotations(DeploymentDefinition definition) {
		String annotationsProperty = definition.getPodAnnotations();

		return PropertyParserUtils.getAnnotations(annotationsProperty);
	}

	private Map<String, String> getServiceAnnotations(DeploymentDefinition definition) {
		String annotationsProperty = definition.getServiceAnnotations();

		return PropertyParserUtils.getAnnotations(annotationsProperty);
	}
}
