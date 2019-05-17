/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.tooling.ls.eclipse.commons.console.ConsoleUtil.Console;
import org.springframework.tooling.ls.eclipse.commons.console.LanguageServerConsoles;
import org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.ServerInfo;
import org.springsource.ide.eclipse.commons.core.util.IOUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public abstract class STS4LanguageServerProcessStreamConnector extends ProcessStreamConnectionProvider {

	private static LanguageServerProcessReaper processReaper = new LanguageServerProcessReaper();

	private Supplier<Console> consoles = null;

	private Bundle bundle;

	public STS4LanguageServerProcessStreamConnector(ServerInfo server) {
		this.consoles = LanguageServerConsoles.getConsoleFactory(server);
	}

	@Override
	public void start() throws IOException {
		super.start();
		Process process = LanguageServerProcessReaper.getProcess(this);
		processReaper.addProcess(process);
		if (consoles!=null) {
			Console console = consoles.get();
			if (console!=null) {
				forwardTo(getLanguageServerLog(), console.out);
			} else {

				new Thread("Consume LS error stream") {

					@Override
					public void run() {
						try {
							IOUtil.consume(getLanguageServerLog());
						} catch (IOException e) {
							// ignore
						}
					}

				}.start();
			}
		}
	}

	protected JRE getJRE() {
		return JRE.currentJRE();
	}

	protected final void initExplodedJarCommand(Path lsFolder, String mainClass, String configFileName, List<String> extraVmArgs) {
		try {
			Bundle bundle = Platform.getBundle(getPluginId());
			JRE runtime = getJRE();
			if (bundle.getVersion().getQualifier().equals("qualifier")) {
				Builder<String> vmArgs = ImmutableList.builder();
				vmArgs.add("-Dsts.lsp.client=eclipse");
				vmArgs.addAll(extraVmArgs);
				setCommands(runtime.jarLaunchCommand(getLanguageServerJARLocation(), vmArgs.build()));
			} else {
				Assert.isNotNull(lsFolder);
				Assert.isNotNull(mainClass);

				ImmutableList.Builder<String> command = ImmutableList.builder();

				command.add(runtime.getJavaExecutable());
				command.add("-cp");

				File bundleFile = FileLocator.getBundleFile(bundle);

				File bundleRoot = bundleFile.getAbsoluteFile();
				Path languageServerRoot = bundleRoot.toPath().resolve(lsFolder);

				StringBuilder classpath = new StringBuilder();
				classpath.append(languageServerRoot.resolve("BOOT-INF/classes").toFile());
				classpath.append(File.pathSeparator);
				classpath.append(languageServerRoot.resolve("BOOT-INF/lib").toFile());
				// Cannot have * in the java.nio.Path on Windows
				classpath.append(File.separator);
				classpath.append('*');

				command.add(classpath.toString());

				command.add("-Dsts.lsp.client=eclipse");

				command.addAll(extraVmArgs);

				if (configFileName != null) {
					command.add("-Dspring.config.location=file:" + languageServerRoot.resolve("BOOT-INF/classes").resolve(configFileName).toFile());
				}

				command.add(mainClass);

				setCommands(command.build());
			}
		}
		catch (Exception e) {
			LanguageServerCommonsActivator.logError(e, "Failed to assemble exploded LS JAR launch command");
		}
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		if (consoles==null) {
			return super.createProcessBuilder();
		}
		ProcessBuilder builder = new ProcessBuilder(getCommands());
		builder.directory(new File(getWorkingDirectory()));
		//Super does this, but we do not:
		//builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		return builder;
	}

	private void forwardTo(InputStream is, OutputStream os) {
		Job consoleJob = new Job("Forward Language Server log output to console") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				try {
					pipe(is, os);
					os.write("==== Process Terminated====\n".getBytes(Charsets.UTF_8));
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					try {
						os.close();
					} catch (IOException e) {
					}
				}
				return Status.OK_STATUS;
			}

			void pipe(InputStream input, OutputStream output) throws IOException {
				try {
				    byte[] buf = new byte[1024*4];
				    int n = input.read(buf);
				    while (n >= 0) {
				      output.write(buf, 0, n);
				      n = input.read(buf);
				    }
				    output.flush();
				} finally {
					input.close();
				}
			}

		};
		consoleJob.setSystem(true);
		consoleJob.schedule();
	}

	private InputStream getLanguageServerLog() {
		return super.getErrorStream();
	}

	@Override
	public void stop() {
		super.stop();
		processReaper.removeProcess(LanguageServerProcessReaper.getProcess(this));
	}

	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}

	/**
	 * Fatjar launch is now only used for running in development mode with a locally built fatjar
	 */
	final protected String getLanguageServerJARLocation() {
		Bundle bundle = getBundle();
		String bundleVersion = bundle.getVersion().toString();

		Assert.isLegal(bundleVersion.endsWith("qualifier"));
		File userHome = new File(System.getProperty("user.home"));
		File locallyBuiltJar = new File(
				userHome
			,
				"git/sts4/headless-services/"
				+ getLanguageServerArtifactId()
				+ "/target/"
				+ getLanguageServerArtifactId()
				+ "-"
				+ getLanguageServerArtifactVersion()
				+ "-exec.jar"
		);
		Assert.isLegal(locallyBuiltJar.exists());
		return locallyBuiltJar.getAbsolutePath();
	}

	protected String getLanguageServerArtifactVersion() {
		Version bv = getBundle().getVersion();
		//Example of what it should look like:
		//  "1.6.0-SNAPSHOT"
		return bv.getMajor()+"."+bv.getMinor()+"."+bv.getMicro()+"-SNAPSHOT";
	}

	protected Bundle getBundle() {
		if (bundle==null) {
			this.bundle = Platform.getBundle(getPluginId());
		}
		return bundle;
	}

	protected abstract String getLanguageServerArtifactId();

	protected final void copyLanguageServerJAR(String languageServerJarName, String languageServerLocalCopy) throws Exception {
		Bundle bundle = Platform.getBundle(getPluginId());
		InputStream stream = FileLocator.openStream( bundle, new org.eclipse.core.runtime.Path("servers/" + languageServerJarName), false );

		File dataFile = bundle.getDataFile(languageServerLocalCopy);
		Files.copy(stream, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	protected abstract String getPluginId();

}
