/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.api.AppConsoleProvider;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.api.DesiredInstanceCount;
import org.springframework.ide.eclipse.boot.dash.api.DevtoolsConnectable;
import org.springframework.ide.eclipse.boot.dash.api.LogConnection;
import org.springframework.ide.eclipse.boot.dash.api.LogSource;
import org.springframework.ide.eclipse.boot.dash.api.ProjectRelatable;
import org.springframework.ide.eclipse.boot.dash.api.SystemPropertySupport;
import org.springframework.ide.eclipse.boot.dash.api.TemporalBoolean;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.docker.jmx.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.BuildScriptLocator.BuildKind;
import org.springframework.ide.eclipse.boot.dash.labels.BootDashLabels;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.remote.ChildBearing;
import org.springframework.ide.eclipse.boot.dash.model.remote.RefreshStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.LineBasedStreamGobler;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.core.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.OldValueDisposer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class DockerApp extends AbstractDisposable implements App, ChildBearing, Deletable, ProjectRelatable, DesiredInstanceCount, SystemPropertySupport, LogSource, DevtoolsConnectable {

	private static final String DOCKER_IO_LIBRARY = "docker.io/library/";
	private static final String[] NO_STRINGS = new String[0];
	private DockerClient client;
	private final IProject project;
	private DockerRunTarget target;
	private final String name;
	
	public static final String APP_NAME = "sts.app.name";
	public static final String BUILD_ID = "sts.app.build-id";
	public static final String SYSTEM_PROPS = "sts.app.sysprops";
	public static final String JMX_PORT = "sts.app.jmx.port";
	public static final String DEBUG_PORT = "sts.app.debug.port";
	public static final String APP_LOCAL_PORT = "sts.app.port.local";
	
	private static final int STOP_WAIT_TIME_IN_SECONDS = 20;
	public final CompletableFuture<RefreshStateTracker> refreshTracker = new CompletableFuture<>();
	
	private OldValueDisposer<LogConnection> containerLogConnection = new OldValueDisposer<>(this);
	private AppContext context;
	
	private static File initFile;

	private final String DEBUG_JVM_ARGS(String debugPort) {
		if (JavaProjectUtil.isJava9OrLater(project)) {
			return "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n,address=*:"+debugPort;
		} else {
			return "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n,address="+debugPort;
		}
	}

	public DockerApp(String name, DockerRunTarget target, DockerClient client) {
		this.target = target;
		this.name = name;
		this.client = client;
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(deployment().getName());
	}
	
	public DockerClient getClient() {
		return this.client;
	}

	public DockerDeployment deployment() {
		return target.deployments.get(name);
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return EnumSet.of(RunState.INACTIVE, RunState.RUNNING, RunState.DEBUGGING);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<App> fetchChildren() throws Exception {
		Builder<App> builder = ImmutableList.builder();
		if (client!=null) {
			List<Image> images = JobUtil.interruptAfter(Duration.ofSeconds(15), 
					() -> client.listImagesCmd().withShowAll(true).exec()
			);
			synchronized (this) {
				Set<String> persistedImages = new HashSet<>(Arrays.asList(getPersistedImages()));
				Set<String> existingImages = new HashSet<>();
				for (Image image : images) {
					if (persistedImages.contains(image.getId())) {
						builder.add(new DockerImage(this, image));
						existingImages.add(image.getId());
					}
				}
				setPersistedImages(existingImages);
			}
		}
		return builder.build();
	}
	
	@Override
	public void delete() throws Exception {
		target.deployments.remove(project.getName());
	}

	@Override
	public DockerRunTarget getTarget() {
		return this.target;
	}
	
	public CompletableFuture<Void> synchronizeWithDeployment() {
		return this.refreshTracker.thenComposeAsync(refreshTracker -> {
			DockerDeployment deployment = deployment();
			return refreshTracker.runAsync("Synchronizing deployment "+deployment.getName(), () -> {
				String currentSession = this.target.sessionId.getValue();
				if (currentSession.equals(deployment.getSessionId())) {
					RunState desiredRunState = deployment.getRunState();
					List<Container> containers = client.listContainersCmd()
							.withShowAll(true)
							.withLabelFilter(ImmutableMap.of(APP_NAME, getName()))
							.exec();
					if (desiredRunState==RunState.INACTIVE) {
						stop(containers);
					} else if (desiredRunState.isActive()) {
						List<Container> toStop = new ArrayList<>(containers.size());
						boolean desiredContainerFound = false;
						for (Container c : containers) {
							if (isMatchingContainer(deployment, c)) {
								if (new DockerContainer(getTarget(), this, c).fetchRunState()==desiredRunState) {
									desiredContainerFound = true;
								}
							} else {
								toStop.add(c);
							}
						}
						stop(toStop);
						if (!desiredContainerFound) {
							start(deployment);
						}
					}
				}
			});
		});
	}

	/**
	 * Checks whether a container's metadata matches all of the desired deployment props.
	 * But without considering runstate.
	 */
	private boolean isMatchingContainer(DockerDeployment d, Container c) {
		String desiredBuildId = d.getBuildId();
		Map<String, String> desiredProps = d.getSystemProperties();
		return desiredBuildId.equals(c.getLabels().get(BUILD_ID)) && 
				desiredProps.equals(DockerContainer.getSystemProps(c));
	}

	private void stop(List<Container> containers) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		refreshTracker.run("Stopping containers for app "+name, () -> {
			for (Container container : containers) {
				try {
					client.stopContainerCmd(container.getId()).withTimeout(STOP_WAIT_TIME_IN_SECONDS).exec();
				} catch (NotModifiedException e) {
					//ignore... this isn't a real error, it means container was already stopped.
				}
			}
		});
	}
	
	public void start(DockerDeployment deployment) throws Exception {
		RefreshStateTracker refreshTracker = this.refreshTracker.get();
		
		AppConsole console = target.injections().getBean(AppConsoleProvider.class).getConsole(this);
		console.show();
		
		String image = refreshTracker.call("Building image" + BootDashLabels.ELLIPSIS, () -> {
			if (!project.isAccessible()) {
				throw new IllegalStateException("The project '"+project.getName()+"' is not accessible");
			}
			console.write("Deploying Docker app " + getName() + BootDashLabels.ELLIPSIS, LogType.STDOUT);
			try {
				return build(console);
			} catch (Exception e) {
				console.write(ExceptionUtil.getMessage(e), LogType.STDERROR);
				if (e instanceof MissingBuildScriptException) {
					console.write("Places we looked for build script: ", LogType.STDERROR);
					for (File loc : ((MissingBuildScriptException) e).locationsChecked) {
						console.write(" - "+loc, LogType.STDERROR);
					}
					showBuildScriptHelp(console, (MissingBuildScriptException) e);
				}
				throw e;
			}
		});
		
		refreshTracker.run("Starting container '" + image + "'" +  + BootDashLabels.ELLIPSIS, () -> {
			run(console, image, deployment);
			console.write("DONE Deploying Docker app " + getName(), LogType.STDOUT);
		});
	}

	private void showBuildScriptHelp(AppConsole console, MissingBuildScriptException e) {
		String[] help = {
				"To build a docker image, Boot Dash needs to run a build script from your project.",
				"Three different types are supported and checked for in this order:",
				"",
				"1. "+e.locationsChecked.get(0).getAbsoluteFile().getName(),
				"   A custom script placed by you at the project root.",
				"   Typically this runs a custom maven or gradle command on your project.",
				"",
				"2. maven",
				"   If your project has a mvnw, we will use that to execute the `spring-boot:build-image` task",
				"",
				"3. gradle",
				"   If your project has a gradlew, we will use that to execute the `bootBuildImage` task",
		};
		try {
			for (String line : help) {
				console.write(line, LogType.STDERROR);
			}
		} catch (Exception e1) {
			//ignore
		}
	}

	private void run(AppConsole console, String image, DockerDeployment deployment) throws Exception {
		if (client==null) {
			console.write("Cannot start container... Docker client is disconnected!", LogType.STDERROR);
		} else {
			Network network = target.ensureNetwork();
			console.write("Running container with '"+image+"'", LogType.STDOUT);
			JmxSupport jmx = new JmxSupport();
			String jmxUrl = jmx.getJmxUrl();
			if (jmxUrl!=null) {
				console.write("JMX URL = "+jmxUrl, LogType.STDOUT);
			}
			String desiredBuildId = deployment.getBuildId();
			Map<String, String> systemProperties = deployment.getSystemProperties();
			String sysprops = new ObjectMapper().writeValueAsString(systemProperties);
			ImmutableMap.Builder<String,String> labels = ImmutableMap.<String,String>builder()
					.put(APP_NAME, getName())
					.put(BUILD_ID, desiredBuildId)
					.put(SYSTEM_PROPS, sysprops);
			
			ImmutableSet.Builder<ExposedPort> exposedPorts = ImmutableSet.builder();
			ImmutableList.Builder<PortBinding> portBindings = ImmutableList.builder();

			CreateContainerCmd cb = client.createContainerCmd(image);
			
			int appLocalPort = PortFinder.findFreePort();
			int appContainerPort = 8080;
			
			if (appLocalPort > 0) {
				labels.put(APP_LOCAL_PORT, ""+appLocalPort);
				portBindings.add(new PortBinding(new Binding("0.0.0.0", ""+appLocalPort), ExposedPort.tcp(appContainerPort)));
				exposedPorts.add(ExposedPort.tcp(appContainerPort));
			}

			StringBuilder javaOpts = new StringBuilder();

			if (jmxUrl!=null) {
				int jmxPort = jmx.getPort();
				labels.put(JMX_PORT, ""+jmxPort);

				portBindings.add(new PortBinding(new Binding("0.0.0.0", ""+jmxPort), ExposedPort.tcp(jmxPort)));
				exposedPorts.add(ExposedPort.tcp(jmxPort));
				
				javaOpts.append(jmx.getJavaOpts());
				javaOpts.append(" ");
			}
			
			RunState desiredRunState = deployment.getRunState();
			if (desiredRunState==RunState.DEBUGGING) {
				int debugPort = PortFinder.findFreePort();
				labels.put(DockerApp.DEBUG_PORT, ""+debugPort);
				
				portBindings.add(new PortBinding(new Binding("0.0.0.0", ""+debugPort), ExposedPort.tcp(debugPort)));
				exposedPorts.add(ExposedPort.tcp(debugPort));
				
				javaOpts.append(DEBUG_JVM_ARGS(""+debugPort));
				console.write("Debug Port = "+debugPort, LogType.STDOUT);
				javaOpts.append(" ");
			}
			
			if (!systemProperties.isEmpty()) {
				for (Entry<String, String> prop : systemProperties.entrySet()) {
					Assert.isTrue(!prop.getValue().contains(" ")); //TODO: Escaping stuff like spaces in the value
					Assert.isTrue(!prop.getValue().contains("\t"));
					Assert.isTrue(!prop.getValue().contains("\n"));
					Assert.isTrue(!prop.getValue().contains("\r"));
					javaOpts.append("-D"+prop.getKey()+"="+prop.getValue()); 
					javaOpts.append(" ");
				}
			}
						
			String javaOptsStr = javaOpts.toString();
			if (StringUtils.hasText(javaOptsStr)) {
				cb.withEnv("JAVA_OPTS="+javaOptsStr.trim());
				console.write("JAVA_OPTS="+javaOptsStr.trim(), LogType.STDOUT);
			}
			
			cb.withHostConfig(new HostConfig()
					.withPortBindings(portBindings.build())
					.withNetworkMode(network.getName())
			);
			cb.withExposedPorts(exposedPorts.build().asList());
			String networkAlias = getName();
			cb.withAliases(networkAlias);
			cb.withLabels(labels.build());
			CreateContainerResponse c = cb.exec();
			console.write("Container created: "+c.getId(), LogType.STDOUT);
			console.write("Starting container: "+c.getId(), LogType.STDOUT);
			console.write("Ports: "+appLocalPort+"->"+appContainerPort, LogType.STDOUT);
			console.write("Container Network: "+ network.getName(), LogType.STDOUT);
			console.write("Network alias: "+networkAlias , LogType.STDOUT);
			
			//Disabled show of console here. See: https://www.pivotaltracker.com/story/show/174316849
			//appContext.showConsole(c.id());
			
			client.startContainerCmd(c.getId()).exec();
			containerLogConnection.setValue(DockerContainer.connectLog(target, c.getId(), console, true));
		}
	}

	private static final Pattern BUILT_IMAGE_MESSAGE = Pattern.compile("Successfully built image.*\\'(.*)\\'");
	
	private String build(AppConsole console) throws Exception {
		AtomicReference<String> image = new AtomicReference<>();
		File directory = new File(project.getLocation().toString());
		String[] command = getBuildCommand(directory);

		ProcessBuilder builder = new ProcessBuilder(command).directory(directory);
		String jhome = getJavaHome();
		builder.environment().put("JAVA_HOME", jhome);
		console.write("build.env.JAVA_HOME="+jhome, LogType.STDOUT);
		console.write("build.directory="+directory, LogType.STDOUT);
		console.write("build.command="+CommandUtil.escape(command), LogType.STDOUT);
		Process process = builder.start();
		LineBasedStreamGobler outputGobler = new LineBasedStreamGobler(process.getInputStream(), (line) -> {
			System.out.println(line);
			Matcher matcher = BUILT_IMAGE_MESSAGE.matcher(line);
			if (matcher.find()) {
				image.set(matcher.group(1));
			}
			try {
				console.write(line, LogType.APP_OUT);
			} catch (Exception e) {
				Log.log(e);
			}
		});
		new LineBasedStreamGobler(process.getErrorStream(), (line) -> {
			try {
				console.write(line, LogType.APP_ERROR);
			} catch (Exception e) {
				Log.log(e);
			}
		});
		int exitCode = process.waitFor();
		if (exitCode!=0) {
			throw new IOException("Command execution failed!");
		}
		outputGobler.join();
		
		String imageTag = image.get();
		if (imageTag.startsWith(DOCKER_IO_LIBRARY)) {
			imageTag = imageTag.substring(DOCKER_IO_LIBRARY.length());
		}
		List<Image> images = client.listImagesCmd().withImageNameFilter(imageTag).exec();
		
		for (Image img : images) {
			addPersistedImage(img.getId());
		}
		
		return imageTag;
	}
	
	private String getJavaHome() throws CoreException {
		IVMInstall jvm = JavaRuntime.getVMInstall(JavaCore.create(project));
		return jvm.getInstallLocation().toString();
	}
	
	private String[] getBuildCommand(File directory) throws MissingBuildScriptException {
		BuildScriptLocator buildScriptLocator = new BuildScriptLocator(directory);
		BuildKind buildKind = buildScriptLocator.getBuildKind();
		if (buildKind==null) {
			throw new MissingBuildScriptException(buildScriptLocator.checkedLocations);
		}
		boolean wantsDevtools = deployment().getSystemProperties().getOrDefault(DevtoolsUtil.REMOTE_SECRET_PROP, null)!=null;
		List<String> command = buildScriptLocator.command;
		if (wantsDevtools) {
			if (buildKind==BuildKind.MAVEN) {
				command.add("-Dspring-boot.repackage.excludeDevtools=false");
			} else if (buildKind==BuildKind.GRADLE) {
				try {
					command.addAll(gradle_initScript(
							"allprojects {\n" + 
							"    afterEvaluate {\n" + 
							"        bootJar {\n" + 
							"          classpath configurations.developmentOnly\n" + 
							"        }\n" + 
							"   }\n" + 
							"}"
					));
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}
		return command.toArray(new String[command.size()]);
    }

	private synchronized static List<String> gradle_initScript(String script) throws IOException {
		if (initFile==null) {
			initFile = File.createTempFile("init-script", ".gradle");
			FileUtils.writeStringToFile(initFile, script, "UTF8");
			initFile.deleteOnExit();
		}
		return ImmutableList.of(
				"-I",
				initFile.getAbsolutePath()
		);
	}

	synchronized private void addPersistedImage(String imageId) {
		String key = imagesKey();
		try {
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			PropertyStoreApi props = getTarget().getPersistentProperties();
			builder.addAll(Arrays.asList(props.get(key, NO_STRINGS)));
			builder.add(imageId);
			props.put(key, builder.build().toArray(NO_STRINGS));

		} catch (Exception e) {
			Log.log(e);
		}
	}
	
	private void setPersistedImages(Set<String> existingImages) {
		try {
			getTarget().getPersistentProperties().put(imagesKey(), existingImages.toArray(NO_STRINGS));
		} catch (Exception e) {
			Log.log(e);
		}		
	}

	private String[] getPersistedImages() {
		try {
			return getTarget().getPersistentProperties().get(imagesKey(), NO_STRINGS);
		} catch (Exception e) {
			Log.log(e);
		}
		return NO_STRINGS;
	}

	private String imagesKey() {
		return getName() + ".images";
	}

	@Override
	public void setContext(AppContext context) {
		this.refreshTracker.complete(context.getRefreshTracker());
		this.context = context;
	}

	@Override
	public void restart(RunState runningOrDebugging) {
		DockerDeployment d = deployment();
		d.setBuildId(UUID.randomUUID().toString());
		d.setSessionId(target.sessionId.getValue());
		d.setRunState(runningOrDebugging);
		target.deployments.createOrUpdate(d);
	}

	@Override
	public void setSystemProperty(String name, String value) {
		DockerDeployment d = new DockerDeployment(deployment());
		d.setSystemProperty(name, value);
		d.setSessionId(target.sessionId.getValue());
		target.deployments.createOrUpdate(d);
	}
	
	@Override
	public String getSystemProperty(String key) {
		DockerDeployment d = deployment();
		if (d!=null ) {
			return d.getSystemProperties().getOrDefault(key, null);
		}
		return null;
	}

	@Override
	public void setGoalState(RunState newGoalState) {
		DockerDeployment deployment = deployment();
		if (deployment.getRunState()!=newGoalState) {
			target.deployments.createOrUpdate(deployment.withGoalState(newGoalState, target.sessionId.getValue()));
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public int getDesiredInstances() {
		DockerDeployment deployment = deployment();
		if (deployment != null) {
			return deployment.getDesiredInstances();
		}
		return 0;
	}

	
	@Override
	public String getConsoleDisplayName() {
		return getName() + " - image build output @ "+getTarget().getName();
	}

	@Override
	public LogConnection connectLog(AppConsole logConsole, boolean includeHistory) {
		// There is nothing to connect to because docker app only writes output directly to the console
		// There is no streaming API to fetch output.
		return null;
	}

	@Override
	public TemporalBoolean isDevtoolsConnectable() {
		return TemporalBoolean.NEVER;
	}
	
	@Override
	public String getDevtoolsSecret() {
		DockerDeployment d = deployment();
		if (d!=null) {
			return d.getSystemProperties().getOrDefault(DevtoolsUtil.REMOTE_SECRET_PROP, null);
		}
		return null;
	}

	@Override
	public boolean hasDevtoolsDependency() {
		return context!=null && context.projectHasDevtoolsDependency();
	}
}
