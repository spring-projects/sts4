package com.example.demo;

import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

public class DockerClientWrapperApplication {

	private static final String DEFAULT_UNIX_DOCKER_URL = "unix:///var/run/docker.sock";

	public static void main(String[] args) throws Exception {
		DefaultDockerClientConfig conf = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(DEFAULT_UNIX_DOCKER_URL).build();
		
		DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
				.dockerHost(conf.getDockerHost())
				.sslConfig(conf.getSSLConfig())
				.build();
		DockerClient client = DockerClientImpl.getInstance(conf, httpClient);
		
		List<Image> imgs = client.listImagesCmd().withShowAll(true).exec();
		for (Image image : imgs) {
			System.out.println(image.getId());
		}
	}

}
