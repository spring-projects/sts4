package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class DockerDeploymentList {
	
	private List<DockerDeployment> deployments;

	public DockerDeploymentList() {
	}

	public DockerDeploymentList(Collection<DockerDeployment> values) {
		this.deployments = ImmutableList.copyOf(values);
	}

	public List<DockerDeployment> getDeployments() {
		return deployments;
	}

	public void setDeployments(List<DockerDeployment> deployments) {
		this.deployments = deployments;
	}

}
