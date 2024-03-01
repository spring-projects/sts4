/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;

import sts.model.plugin.StsToolingModel;

public class StsGradleToolingModelBuilder {

	private static Path initScript = null;

	private static Path getCacheFolder() {
		IPath iPath = StsGradlePlugin.getDefault().getStateLocation().makeAbsolute();
		return iPath.toFile().toPath().resolve("gradle-plugin");
	};

	private static Path getInitScript() {
		if (initScript == null) {
			Path cacheFolder = getCacheFolder();
			initScript = cacheFolder.resolve("init.gradle");
			Path jarPath = cacheFolder.resolve("sts-gradle-model-plugin.jar");
			if (!Files.isDirectory(cacheFolder)) {
				cacheFolder.toFile().mkdirs();
			}
			if (!Files.exists(initScript)) {
				try {
					Files.copy(StsGradleToolingModelBuilder.class.getResourceAsStream("/gradle-plugin/init.gradle"),
							initScript, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					StsGradlePlugin.getDefault().getLog().error("Failed to copy '/gradle-plugin/init.gradle'", e);
				}
			}
			if (!Files.exists(jarPath)) {
				try {
					Files.copy(StsGradleToolingModelBuilder.class
							.getResourceAsStream("/gradle-plugin/sts-gradle-model-plugin.jar"), jarPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					StsGradlePlugin.getDefault().getLog()
							.error("Failed to copy '/gradle-plugin/sts-gradle-model-plugin.jar'", e);
				}
			}
		}
		return initScript;
	}

	public static ModelBuilder<StsToolingModel> getModelBuilder(ProjectConnection connection, File projectDir, File buildFile) {
		List<String> arguments = new ArrayList<>();
		if (buildFile != null && buildFile.exists()) {
			arguments.add("-b");
			arguments.add(buildFile.getAbsolutePath());
		}
		arguments.add("--init-script");
		arguments.add(getInitScript().toString());
		ModelBuilder<StsToolingModel> modelBuilder = connection.model(StsToolingModel.class);
		modelBuilder.withArguments(arguments);
		return modelBuilder;
	}

}
