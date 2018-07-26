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
package org.springframework.ide.kubernetes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Service;
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

	@Autowired
	protected ContainerFactory containerFactory;
	
	@Autowired
	protected KubernetesClientWrapper clientFactory;

	public KubernetesAppDeployer() {
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
	public String deploy(DeploymentDefinition request) {

		String appId = createDeploymentId(request);
		logger.debug(String.format("Deploying app: %s", appId));

		try {

			int containerPort = configureExternalPort(request);

			logger.info(String.format("Creating Service: %s . Please wait...", appId));
			createService(appId, request);
			logger.info(String.format("Created Service: %s.", appId));

			logger.info(String.format("Creating Deployment: %s. Please wait...", appId));
			createDeployment(appId, request, containerPort);
			logger.info(String.format("Created Deployment: %s.", appId));

			return appId;
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	protected Map<String, String> getAppSelector(String appId) {
		Map<String, String> selector = new HashMap<>();
		selector.put(APP_SELECTOR, appId);
		return selector;

	}

	@Override
	public void undeploy(DeploymentDefinition definition) {
		String appName = definition.getAppName();
		logger.debug(String.format("Undeploying app: %s", appName));

		List<Service> apps = client().services().list().getItems();
		if (apps != null) {
			for (Service app : apps) {
				String appIdToDelete = app.getMetadata().getName();
				logger.debug(String.format("Deleting Resources for: %s", appIdToDelete));

				try {

					Boolean svcDeleted = client().services().withName(appIdToDelete).delete();
					logger.debug(String.format("Deleted Service for: %s %b", appIdToDelete, svcDeleted));
					Boolean rcDeleted = client().replicationControllers().withName(appIdToDelete).delete();
					if (rcDeleted) {
						logger.debug(
								String.format("Deleted Replication Controller for: %s %b", appIdToDelete, rcDeleted));
					}
					Boolean deplDeleted = client().extensions().deployments().withName(appIdToDelete).delete();
					if (deplDeleted) {
						logger.debug(String.format("Deleted Deployment for: %s %b", appIdToDelete, deplDeleted));
					}

				} catch (RuntimeException e) {
					logger.error(e.getMessage(), e);
					throw e;
				}
			}
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

		Deployment d = new DeploymentBuilder().withNewMetadata().withName(appId).withLabels(idMap).endMetadata()
				.withNewSpec().withReplicas(replicas).withNewTemplate().withNewMetadata().withLabels(idMap)
				.withAnnotations(annotations).endMetadata().withSpec(createPodSpec(appId, request, externalPort, false))
				.endTemplate().endSpec().build();

		return client().extensions().deployments().create(d);
	}

	protected void createService(String appId, DeploymentDefinition request) {
		ServiceSpecBuilder spec = new ServiceSpecBuilder();

		ServicePort servicePort = request.getServicePort();

		if (request.createNodePort()) {
			spec.withType("NodePort");
		}

		Map<String, String> annotations = getServiceAnnotations(request);
		Map<String, String> idMap = getServiceLabels(appId);

		spec.withSelector(idMap).addNewPortLike(servicePort).endPort();

		client().services().inNamespace(client().getNamespace()).createNew().withNewMetadata().withName(appId)
				.withLabels(idMap).withAnnotations(annotations).endMetadata().withSpec(spec.build()).done();
	}

	protected PodSpec createPodSpec(String appId, DeploymentDefinition request, Integer port, boolean neverRestart) {
		PodSpecBuilder podSpec = new PodSpecBuilder();

		boolean hostNetwork = request.getHostNetwork();

		Container container = containerFactory.create(request);

		ResourceRequirements req = new ResourceRequirements();
		req.setLimits(deduceResourceLimits(request));
		req.setRequests(deduceResourceRequests(request));
		container.setResources(req);
		String pullPolicy = request.getImagePullPolicy();
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

	protected Map<String, Quantity> deduceResourceLimits(DeploymentDefinition request) {
		String memory = request.getMemory();
		String cpu = request.getCpu();

		Map<String, Quantity> limits = new HashMap<String, Quantity>();

		if (memory != null) {
			limits.put("memory", new Quantity(memory));

		}
		if (cpu != null) {
			limits.put("cpu", new Quantity(cpu));
		}
		return limits;
	}

	protected Map<String, Quantity> deduceResourceRequests(DeploymentDefinition request) {
		String memOverride = request.getMemory();

		String cpuOverride = request.getCpu();

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

	private Map<String, String> getPodAnnotations(DeploymentDefinition request) {
		String annotationsProperty = request.getPodAnnotations();

		return PropertyParserUtils.getAnnotations(annotationsProperty);
	}

	private Map<String, String> getServiceAnnotations(DeploymentDefinition request) {
		String annotationsProperty = request.getServiceAnnotations();

		return PropertyParserUtils.getAnnotations(annotationsProperty);
	}

}
