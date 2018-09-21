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
package org.springframework.ide.kubernetes.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.api.model.ResponseItem.ErrorDetail;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.google.common.io.Files;

public class DockerHandler {

	private static final String DEPLOYER_DOCKER_FILE = "Dockerfile";
	private static final String FAT_JAR_NAME = "fatjar.jar";
	private File tempDir = null;

	private Logger logger = LoggerFactory.getLogger(DockerHandler.class);

	private DockerClient client;
	private DockerClientFactory clientFactory;

	BuildImageResultCallback callback = new BuildImageResultCallback() {
		@Override
		public void onNext(BuildResponseItem item) {
			logResponse(item);
			super.onNext(item);
		}
	};

	@Autowired
	public DockerHandler(DockerClientFactory clientFactory) {

		this.clientFactory = clientFactory;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				deleteTempDir();
				if (client != null) {
					try {
						client.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
		}));
	}

	private synchronized void deleteTempDir() {
		if (tempDir != null && tempDir.exists()) {
			try {
				FileUtils.deleteDirectory(tempDir);
			} catch (IOException e) {
				logger.error("", e);
			}
			tempDir = null;
		}
	}

	private synchronized File getTempDir() {
		if (tempDir == null) {
			tempDir = Files.createTempDir();
		}
		return tempDir;
	}

	private File getDockerFile(File parent) throws IOException {
		File[] files = parent.listFiles((file) -> file.getName().equals(DEPLOYER_DOCKER_FILE));
		if (files != null && files.length >= 1) {
			return files[0];
		} else {
			File tempDockerFile = new File(parent, DEPLOYER_DOCKER_FILE);
			// The original docker file needs to be copied to the location that contains
			// the boot jars as it relies on the jars being in the same directory where
			// docker file will be read during image building
			ClassLoader classLoader = getClass().getClassLoader();
			// Must read as stream as docker file may be inside the deployer jar
			InputStream resourceStream = classLoader.getResourceAsStream(DEPLOYER_DOCKER_FILE);
			FileUtils.copyInputStreamToFile(resourceStream, tempDockerFile);
			return tempDockerFile;
		}
	}

	private void copyJarToDir(File parent, File originalJar) throws Exception {
		File destFile = new File(parent, FAT_JAR_NAME);
		FileUtils.copyFile(originalJar, destFile);
	}

	public synchronized void push(String jarPath, DockerImage dockerImage) throws Exception {

		logger.info(String.format("Building Docker image from app jar: %s", jarPath));

		File jar = new File(jarPath);
		File tempDir = getTempDir();
		File dockerFile = getDockerFile(tempDir);

		// Copy jar into temporary directory where image will be built.
		copyJarToDir(tempDir, jar);

		DockerClient client = getDockerClient();
		String imageId = client.buildImageCmd().withDockerfile(dockerFile).withBaseDirectory(tempDir).exec(callback)
				.awaitImageId();
		String name = dockerImage.getRepository();
		String tag = dockerImage.getTag();

		logger.info(String.format("Built Docker image: %s", imageId));
		client.tagImageCmd(imageId, name, tag).exec();
		logger.info(String.format("Tagged Docker image: %s", tag));
		logger.info(String.format("Pushing image: %s...", name));

		client.pushImageCmd(name).withTag(tag)
				.exec(new ResultCallbackTemplate<PushImageResultCallback, PushResponseItem>() {

					@Override
					public void onNext(PushResponseItem item) {
						logResponse(item);
					}

				}).awaitCompletion();

	}

	private DockerClient getDockerClient() throws Exception {

		if (this.client == null) {
			this.client = this.clientFactory.getDockerClient();
		}
		return this.client;
	}

	private void logResponse(ResponseItem item) {
		if (item != null && item.isErrorIndicated()) {
			ErrorDetail detail = item.getErrorDetail();
			if (detail != null && StringUtils.hasText(detail.getMessage())) {
				logger.error(detail.getMessage());
			}
		}
	}

}
