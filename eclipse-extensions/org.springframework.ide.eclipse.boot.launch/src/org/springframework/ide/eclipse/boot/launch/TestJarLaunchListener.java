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
package org.springframework.ide.eclipse.boot.launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
class TestJarLaunchListener implements ILaunchesListener2 {

	private static final Pattern TESTJAR_PATTERN = Pattern.compile("^spring-boot-testjars-\\d+\\.\\d+\\.\\d+(.*)?.jar$");

	private static final String TESTJAR_ARTIFACTS = "spring.boot.test-jar-artifacts";

	public record ExecutableProject(String name, String uri, String gav, String mainClass, Collection<String> classpath) {}

	public void launchRemoved(ILaunch launch) {
		clearTestJarWorkspaceProjectFiles(launch.getLaunchConfiguration());
	}

	private void clearTestJarWorkspaceProjectFiles(ILaunchConfiguration configuration) {
		try {
			if (JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION.equals(configuration.getType().getIdentifier())) {
				Map<String, String> oldTestJarArtifacts = configuration.getAttribute(TESTJAR_ARTIFACTS, Collections.emptyMap());
				Map<String, String> env = new HashMap<>(configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, Collections.emptyMap()));
				for (Map.Entry<String, String> testJarArtifactEnvEntry : oldTestJarArtifacts.entrySet()) {
					env.remove(testJarArtifactEnvEntry.getKey());
					try {
						Files.deleteIfExists(Paths.get(testJarArtifactEnvEntry.getValue()));
					} catch (IOException e) {
						Log.log(e);
					}
				}
				ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
				wc.removeAttribute(TESTJAR_ARTIFACTS);
				if (env.isEmpty()) {
					wc.removeAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES);
				} else {
					wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
				}
				wc.doSave();
			}
		} catch (CoreException e) {
			Log.log(e);
		}
	}

	private void setupTestJarWorkspaceProjectFiles(ILaunchConfiguration configuration, Map<String, String> testJarArtifactsEnvMap) {
		try {
			Map<String, String> originalEnv = new HashMap<>(configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, Collections.emptyMap()));
			Map<String, String> oldTestJarArtifacts = configuration.getAttribute(TESTJAR_ARTIFACTS, Collections.emptyMap());
			for (Map.Entry<String, String> envEntry : oldTestJarArtifacts.entrySet()) {
				try {
					Files.deleteIfExists(Paths.get(envEntry.getValue()));
				} catch (IOException e) {
					Log.log(e);
				}
				originalEnv.remove(envEntry.getKey());
			}
			for (String envVar : originalEnv.keySet()) {
				testJarArtifactsEnvMap.remove(envVar);
			}
			originalEnv.putAll(testJarArtifactsEnvMap);
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(TESTJAR_ARTIFACTS, testJarArtifactsEnvMap);
			wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, originalEnv);
			wc.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void launchAdded(ILaunch launch) {
		try {
			ILaunchConfiguration configuration = launch.getLaunchConfiguration();
			if (JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION.equals(configuration.getType().getIdentifier())) {
				ILaunchDelegate[] delegates = configuration.getType().getDelegates(Set.of(launch.getLaunchMode()));
				if (delegates.length > 0) {
					JUnitLaunchConfigurationDelegate delegate = (JUnitLaunchConfigurationDelegate) delegates[0].getDelegate();
					String[] classpath = delegate.getClasspathAndModulepath(configuration)[0];
					if (isTestJarsOnClasspath(classpath)) {
						try {
							getTestJarArtifactsMap()
								.thenAccept(testJarArtifactsEnvMap -> setupTestJarWorkspaceProjectFiles(configuration, testJarArtifactsEnvMap))
								.get(3000000, TimeUnit.SECONDS);
						} catch (Exception e) {
							Log.log(e);
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static String createTestJarArtifactEnvKey(String gav) {
		return "TESTJARS_ARTIFACT_%s".formatted(gav.replace(":", "_"));
	}

	private static boolean isTestJarsOnClasspath(String[] classpath) {
		for (String cpe : classpath) {
			Path p = Paths.get(cpe);
			if (Files.isRegularFile(p) && TESTJAR_PATTERN.matcher(p.getFileName().toString()).matches()) {
				return true;
			}
		}
		return false;
	}

	private CompletableFuture<Map<String, String>> getTestJarArtifactsMap() {
		try {
			Optional<?> opt = BootLsCommandUtils.executeCommand(TypeToken.getParameterized(List.class, ExecutableProject.class), "sts/spring-boot/executableBootProjects").get(3, TimeUnit.SECONDS);
			if (opt.isPresent() && opt.get() instanceof List<?> projects) {
				Map<String, String> gavToFile = new ConcurrentHashMap<>();
				CompletableFuture<?>[] futures = projects.stream().map(ExecutableProject.class::cast).map(p -> CompletableFuture.runAsync(() -> {
					try {
						Path file = Files.createTempFile("%s_".formatted(p.gav().replace(":", "_")), UUID.randomUUID().toString());
						Files.write(file, List.of(
								"# the main class to invoke",
								"main=%s".formatted(p.mainClass()),
								"# the classpath to use delimited by the OS specific delimiters",
								"classpath=%s".formatted(String.join(File.pathSeparator, p.classpath())
						)));
						gavToFile.put(createTestJarArtifactEnvKey(p.gav()), file.toFile().toString());
					} catch (IOException e) {
						Log.log(e);
					}
				})).toArray(CompletableFuture[]::new);
				return CompletableFuture.allOf(futures).thenApply(v -> gavToFile);
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			Log.log(e);
		}

		return CompletableFuture.completedFuture(Collections.emptyMap());
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		// nothing to do
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch l : launches) {
			launchAdded(l);
		}
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
		// nothing to do
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch l : launches) {
			clearTestJarWorkspaceProjectFiles(l.getLaunchConfiguration());
		}
	}

	void clearTestJarArtifactEnvKeyFromLaunches(ILaunchManager launchManager) {
		for (ILaunch l : launchManager.getLaunches()) {
			if (!l.isTerminated() && l.getLaunchConfiguration() != null) {
				clearTestJarWorkspaceProjectFiles(l.getLaunchConfiguration());
			}
		}
	}

}
