/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.api.Deletable;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.HealthChecks;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cf.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement.CloudAppIdentity;
import org.springframework.ide.eclipse.boot.dash.cf.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cf.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.cf.ops.RemoteDevClientStartOperation;
import org.springframework.ide.eclipse.boot.dash.cf.ops.SetHealthCheckOperation;
import org.springframework.ide.eclipse.boot.dash.cf.routes.ParsedUri;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.JmxSshTunnelStatus;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.OperationTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.OperationTracker.Task;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.console.ApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.console.LegacyLogSource;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveDataCapableElement;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveDataConnectionManagementActions.ExecuteCommandAction;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;
import org.springframework.ide.eclipse.boot.dash.util.Utils;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStores;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import reactor.core.Disposable;

/**
 * A handle to a Cloud application. NOTE: This element should NOT hold Cloud
 * application state as it may be discarded and created multiple times for the
 * same app for any reason.
 * <p/>
 * Cloud application state should always be resolved from external sources
 */
public class CloudAppDashElement extends CloudDashElement<CloudAppIdentity> implements BootDashElement, Deletable, LogSink, LiveDataCapableElement, LegacyLogSource {

	private static final boolean DEBUG =
			(""+Platform.getLocation()).contains("kdvolder");
//			(""+Platform.getLocation()).contains("bamboo");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	static final private String DEPLOYMENT_MANIFEST_FILE_PATH = "deploymentManifestFilePath"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "PROJECT_NAME";
	private static final String CF_JMX_PORT = "CF_JMX_PORT";
	private static final String CF_JMX_ENABLED = "CF_JMX_ENABLED";

	private CancelationTokens cancelationTokens;

	private final CloudFoundryBootDashModel cloudModel;
	private PropertyStoreApi persistentProperties;

	private final LiveVariable<Throwable> error = new LiveVariable<>();
	private final OperationTracker startOperationTracker = new OperationTracker(()-> this.getName(),error);

	private final LiveVariable<CFApplication> appData = new LiveVariable<>();
	private final LiveVariable<List<CFInstanceStats>> instanceData = new LiveVariable<>();
	private final LiveExpression<RunState> baseRunState = new LiveExpression<RunState>() {

		{
			dependsOn(appData);
			dependsOn(instanceData);
			dependsOn(startOperationTracker.inProgress);
			dependsOn(error);
		}

		@Override
		protected RunState compute() {
			debug("Compute baseRunState for "+CloudAppDashElement.this+" ...");
			if (error.getValue()!=null) {
				debug("error.getValue() => "+error.getValue());
				debug("baseRunState for "+CloudAppDashElement.this+" => UNKNOWN");
				return RunState.UNKNOWN;
			}
			if (startOperationTracker.inProgress.getValue()>0) {
				debug("startOperationTracker.inProgress.getValue() => "+startOperationTracker.inProgress.getValue());
				debug("baseRunState for "+CloudAppDashElement.this+" => STARTING");
				return RunState.STARTING;
			}
			CFApplication app = appData.getValue();
			debug("appData => "+app);
			List<CFInstanceStats> instances = instanceData.getValue();
			debug("instances.size() => "+(instances==null?null:instances.size()));
			if (instances!=null && app!=null) {
				RunState rs = ApplicationRunningStateTracker.getRunState(app, instances);
				debug("baseRunState from instances => "+rs);
				return rs;
			}
			debug("baseRunState for "+CloudAppDashElement.this+" => UNKNOWN");
			return RunState.UNKNOWN;
		}
	};

	/**
	 * Used as a temporary override of health-check info fetched from CF. This is cleared when element gets 'proper'
	 * data fetched from CF. This exists so that we can implement 'setHealthCheck' which is called to update
	 * model state after changing the health-check value individually. It makes sense in that case to only update
	 * this one bit of the model state rather than refresh all the data from CF.
	 */
	private final LiveVariable<String> healthCheckOverride = new LiveVariable<>();
	private JmxSupport jmxSupport;
	private LiveExpression<JmxSshTunnelStatus> jmxSshTunnelStatus = new LiveExpression<JmxSshTunnelStatus>() {

		//This liveexp is refreshed calls to its 'refresh' method
		//  - from jmxSshTunnelManager whenever a tunnel is created or disposed.
		//  - from setEnableJmxSshTunnel method.

		@Override
		protected JmxSshTunnelStatus compute() {
			if (getEnableJmxSshTunnel()) {
				 return jmxSupport!=null && jmxSupport.isTunnelActive()
					? JmxSshTunnelStatus.ACTIVE
					: JmxSshTunnelStatus.INACTIVE;
			} else {
				return JmxSshTunnelStatus.DISABLED;
			}
		}
	};
	private LiveExpression<String> activeJmxUrl = new LiveExpression<String>() {
		{
			dependsOn(jmxSshTunnelStatus);
		}

		@Override
		protected String compute() {
			JmxSshTunnelStatus status = jmxSshTunnelStatus.getValue();
			if (status==JmxSshTunnelStatus.ACTIVE) {
				return JmxSupport.getJmxUrl(jmxSupport.getPort());
			}
			return null;
		}
	};

	{
		appData.addListener((e, v) -> {
			healthCheckOverride.setValue(null);
		});
	}

	protected void showConsole() {
		try {
			getBootDashModel().getElementConsoleManager().showConsole(this);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	protected void resetAndShowConsole() {
		try {
			getBootDashModel().getElementConsoleManager().resetConsole(this);
			getBootDashModel().getElementConsoleManager().showConsole(this);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	public CloudAppDashElement(CloudFoundryBootDashModel model, String appName, IPropertyStore modelStore) {
		super(model, new CloudAppIdentity(appName, model.getRunTarget()));
		this.cancelationTokens = new CancelationTokens();
		this.cloudModel = model;
		IPropertyStore backingStore = PropertyStores.createSubStore("A"+getName(), modelStore);
		this.persistentProperties = PropertyStores.createApi(backingStore);
		addElementNotifier(baseRunState);
		addElementNotifier(appData);
		addElementNotifier(healthCheckOverride);
		addElementNotifier(jmxSshTunnelStatus);
		this.addDisposableChild(baseRunState);
		getJmxSupport(); //Must force creation of jmxSupport object, if applicable, otherwise it's runstate listener will not be
				// active to start JmxSshTunnel. See bug: https://www.pivotaltracker.com/story/show/159376406
		jmxSshTunnelStatus.refresh();
	}

	@Override
	public CloudFoundryBootDashModel getBootDashModel() {
		return (CloudFoundryBootDashModel) super.getBootDashModel();
	}

	public void stopAsync() throws Exception {
		cancelOperations();
		String appName = getName();
		getBootDashModel().runAsynch("Stopping application " + appName, appName, (IProgressMonitor monitor) -> {
			stop(createCancelationToken(), monitor);
		}, ui());
	}

	@Override
	public void stop() throws Exception {
		cancelOperations();
		stop(createCancelationToken(), new NullProgressMonitor());
	}

	public void stop(CancelationToken cancelationToken, IProgressMonitor monitor) throws Exception {
		checkTerminationRequested(cancelationToken, monitor);
		getClient().stopApplication(getName());
		refresh();
	}

	@Override
	public void restart(RunState runningOrDebugging, UserInteractions ui) throws Exception {
		Job job = new Job("Restarting " + getName()) {
			@Override
			protected IStatus run(IProgressMonitor _monitor) {
				CancelationToken cancelationToken = cancelationTokens.create();
				try {
					startOperationTracker.whileExecuting(ui, cancelationToken, _monitor,  () -> {
						cancelationTokens.cancelAllBefore(cancelationToken);
						//Caution! It is important that canceling older tokens is done *inside* the 'whileExecuting'.
						//Otherwise currently executing operations will exit before the restart operation is registered
						//as 'in progress' which causes a brief flash in the runstate where it incorrectly registers as
						// 'running' while it should be registering as 'starting'.
						//See: https://www.pivotaltracker.com/story/show/159639098/comments/193569587
						cloudModel.runSynch("Restarting, goal state: " + runningOrDebugging, getName(), (IProgressMonitor monitor) -> {
							if (getProject() != null) {
								// Let push and debug resolve deployment properties
								CloudApplicationDeploymentProperties deploymentProperties = null;
								pushAndDebug(deploymentProperties, runningOrDebugging, ui, cancelationToken, monitor);
							} else {
								restartOnly(ui, cancelationToken, monitor);
							}
						}, ui);
					});
				} catch (Exception e) {
					Log.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	public DebugSupport getDebugSupport() {
		//In the future we may need to choose between multiple strategies here.
		return getBootDashModel().getDebugSupport();
	}

	public BootDashViewModel getViewModel() {
		return getBootDashModel().getViewModel();
	}

	public void restartWithRemoteClient(UserInteractions ui, CancelationToken cancelationToken) {
		String opName = "Restart Remote DevTools Client for application '" + getName() + "'";
		getBootDashModel().runAsynch(opName, getName(), (IProgressMonitor monitor) -> {
			doRestartWithRemoteClient(RunState.RUNNING, ui, cancelationToken, monitor);
		}, ui);
	}

	protected void doRestartWithRemoteClient(RunState runningOrDebugging, UserInteractions ui, CancelationToken cancelationToken, IProgressMonitor monitor)
			throws Exception {

		CloudFoundryBootDashModel model = getBootDashModel();
		Map<String, String> envVars = model.getRunTarget().getClient().getApplicationEnvironment(getName());

		if (getProject() == null) {
			ExceptionUtil.coreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
					"Local project not associated to CF app '" + getName() + "'"));
		}

		new SetHealthCheckOperation(this, HealthChecks.HC_PROCESS, ui, /* confirmChange */true, cancelationToken)
				.run(monitor);

		if (!DevtoolsUtil.isEnvVarSetupForRemoteClient(envVars, DevtoolsUtil.getSecret(getProject()))) {
			// Let the push and debug operation resolve default properties
			CloudApplicationDeploymentProperties deploymentProperties = null;

			pushAndDebug(deploymentProperties , runningOrDebugging, ui, cancelationToken, monitor);
			/*
			 * Restart and push op resets console anyway, no need to reset it
			 * again
			 */
		} else if (getRunState() == RunState.INACTIVE) {
			restartOnly(ui, cancelationToken, monitor);
		}

		new RemoteDevClientStartOperation(model, getName(), runningOrDebugging, cancelationToken).run(monitor);
	}


	public void restartOnly(UserInteractions ui, CancelationToken cancelationToken, IProgressMonitor monitor) throws Exception {
		whileStarting(ui, cancelationToken, monitor, () -> {
			if (!getClient().applicationExists(getName())) {
				throw ExceptionUtil.coreException(
						"Unable to start the application. Application does not exist anymore in Cloud Foundry: "
								+ getName());
			}

			checkTerminationRequested(cancelationToken, monitor);

			log("Starting application: " + getName());
			getClient().restartApplication(getName(), CancelationTokens.merge(cancelationToken, monitor));

			new ApplicationRunningStateTracker(cancelationToken, this).startTracking(monitor);

			CFApplicationDetail updatedInstances = getClient().getApplication(getName());
			setDetailedData(updatedInstances);
		});
	}

	public void restartOnlyAsynch(UserInteractions ui, CancelationToken cancelationToken) {
		String opName = "Restarting application " + getName();
		getBootDashModel().runAsynch(opName, getName(), (IProgressMonitor monitor) -> {
			restartOnly(ui, cancelationToken, monitor);
		}, ui);
	}

	public void pushAndDebug(CloudApplicationDeploymentProperties deploymentProperties, RunState runningOrDebugging,
			UserInteractions ui, CancelationToken cancelationToken, IProgressMonitor monitor) throws Exception {
		String opName = "Starting application '" + getName() + "' in "
				+ (runningOrDebugging == RunState.DEBUGGING ? "DEBUG" : "RUN") + " mode";
		DebugSupport debugSupport = getDebugSupport();

		if (runningOrDebugging == RunState.DEBUGGING) {

			if (debugSupport != null && debugSupport.isSupported(this)) {
				Operation<?> debugOp = debugSupport.createOperation(this, opName, ui, cancelationToken);

				push(deploymentProperties, runningOrDebugging, debugSupport, cancelationToken, ui, monitor);
				debugOp.run(monitor);
			} else {
				String title = "Debugging is not supported for '" + getName() + "'";
				String msg = debugSupport.getNotSupportedMessage(this);
				if (msg == null) {
					msg = title;
				}
				ui.errorPopup(title, msg);
				throw ExceptionUtil.coreException(msg);
			}
		} else {
			push(deploymentProperties, runningOrDebugging, debugSupport, cancelationToken, ui, monitor);
		}
	}

	@Override
	public void openConfig(UserInteractions ui) {

	}

	@Override
	public String getName() {
		return delegate.getAppName();
	}

	/**
	 * Returns the project associated with this element or null. If includeNonExistingProjects is
	 * true, then the project is returned even it no longer exists.
	 */
	public IProject getProject(boolean includeNonExistingProjects) {
		String name = getPersistentProperties().get(PROJECT_NAME);
		if (name!=null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (includeNonExistingProjects || project.exists()) {
				return project;
			}
		}
		return null;
	}

	/**
	 * Returns the remote jmx port that should/will be used when deploying this
	 * app to CF (so that we can SSH tunnel to that port to attach to JMX on the remote app).
	 */
	public int getCfJmxPort() {
		return getPersistentProperties().get(CF_JMX_PORT, -1);
	}

	public void setCfJmxPort(int port) throws Exception {
		getPersistentProperties().put(CF_JMX_PORT, port>0 ? ""+port : null);
	}

	public LiveExpression<JmxSshTunnelStatus> getJmxSshTunnelStatus() {
		return jmxSshTunnelStatus;
	}

	public boolean getEnableJmxSshTunnel() {
		return getPersistentProperties().get(CF_JMX_ENABLED, false);
	}

	public void setEnableJmxSshTunnel(boolean enable) throws Exception {
		boolean old = getEnableJmxSshTunnel();
		getPersistentProperties().put(CF_JMX_ENABLED, enable);
		jmxSshTunnelStatus.refresh();
	}


	/**
	 * Returns the project associated with this element or null. The project returned is
	 * guaranteed to exist.
	 */
	@Override
	public IProject getProject() {
		return getProject(false);
	}

	/**
	 * Set the project 'binding' for this element.
	 * @return true if the element was changed by this operation.
	 */
	public boolean setProject(IProject project) {
		try {
			PropertyStoreApi props = getPersistentProperties();
			String oldValue = props.get(PROJECT_NAME);
			String newValue = project==null?null:project.getName();
			if (!Objects.equals(oldValue, newValue)) {
				props.put(PROJECT_NAME, newValue);
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.log(e);
			return false;
		}
	}

	@Override
	public RunState getRunState() {
		RunState state = baseRunState.getValue();
		if (state == RunState.RUNNING) {
			DebugSupport debugSupport = getDebugSupport();
			if (debugSupport.isDebuggerAttached(CloudAppDashElement.this)) {
//			if (DevtoolsUtil.isDevClientAttached(this, ILaunchManager.DEBUG_MODE)) {
				state = RunState.DEBUGGING;
			}
		}
		return state;
	}

	/**
	 * This method is mostly meant just for test purposes. The 'baseRunState' is really
	 * part of how this class internally computes runstate. Clients should have no business
	 * using it separate from the runtstate.
	 */
	public LiveExpression<RunState> getBaseRunStateExp() {
		return baseRunState;
	}

	@Override
	public int getLivePort() {
		return 443;
	}

	@Override
	public String getLiveHost() {
		CFApplication app = getSummaryData();
		if (app != null) {
			List<String> uris = app.getUris();
			if (uris != null) {
				for (String _uri : uris) {
					if (StringUtil.hasText(_uri)) {
						ParsedUri uri = new ParsedUri(_uri);
						return uri.getHostAndDomain();
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getUrl() {
		CFApplication app = getSummaryData();
		if (app != null) {
			List<String> uris = app.getUris();
			if (uris != null) {
				for (String uri : uris) {
					if (StringUtil.hasText(uri)) {
						return Utils.pathJoin("https://"+uri, getDefaultRequestMappingPath());
					}
				}
			}
		}
		return null;
	}

	public Integer getMemory() {
		CFApplication app = getSummaryData();
		if (app != null) {
			return app.getMemory();
		}
		return null;
	}

	public String getHealthCheckHttpEndpoint() {
		CFApplication app = getSummaryData();
		if (app != null) {
			return app.getHealthCheckHttpEndpoint();
		}
		return null;
	}

	public CFApplication getSummaryData() {
		return appData.getValue();
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return null;
	}

	@Override
	public int getActualInstances() {
		CFApplication data = getSummaryData();
		return data != null ? data.getRunningInstances() : 0;
	}

	@Override
	public int getDesiredInstances() {
		CFApplication data = getSummaryData();
		return data != null ? data.getInstances() : 0;
	}

	public String getHealthCheck() {
		String hc = healthCheckOverride.getValue();
		if (hc!=null) {
			return hc;
		}
		CFApplication data = getSummaryData();
		return data!=null ? data.getHealthCheckType() : DeploymentProperties.DEFAULT_HEALTH_CHECK_TYPE;
	}

	/**
	 * Changes the cached health-check value for this model element. Note that this
	 * doesnt *not* change the real value of the health-check.
	 */
	public void setHealthCheck(String hc) {
		this.healthCheckOverride.setValue(hc);
	}


	public UUID getAppGuid() {
		CFApplication app = getSummaryData();
		if (app!=null) {
			return app.getGuid();
		}
		return null;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	static class CloudAppIdentity {

		private final String appName;
		private final RunTarget runTarget;

		@Override
		public String toString() {
			return appName + "@" + runTarget;
		};

		CloudAppIdentity(String appName, RunTarget runTarget) {
			this.appName = appName;
			this.runTarget = runTarget;
		}

		public String getAppName() {
			return this.appName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appName == null) ? 0 : appName.hashCode());
			result = prime * result + ((runTarget == null) ? 0 : runTarget.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CloudAppIdentity other = (CloudAppIdentity) obj;
			if (appName == null) {
				if (other.appName != null)
					return false;
			} else if (!appName.equals(other.appName))
				return false;
			if (runTarget == null) {
				if (other.runTarget != null)
					return false;
			} else if (!runTarget.equals(other.runTarget))
				return false;
			return true;
		}

	}

	@Override
	public void log(String message) {
		log(message, LogType.STDOUT);
	}

	public void log(String message, LogType logType) {
		try {
			getBootDashModel().getElementConsoleManager().writeToConsole(this, message, logType);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public BootDashModel getParent() {
		return getBootDashModel();
	}

	@Override
	protected LiveExpression<String> getActuatorUrl() {
		return activeJmxUrl;
	}

	public IFile getDeploymentManifestFile() {
		String text = getPersistentProperties().get(DEPLOYMENT_MANIFEST_FILE_PATH);
		try {
			return text == null ? null : ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(text));
		} catch (IllegalArgumentException e) {
			Log.log(e);
			return null;
		}
	}

	public void setDeploymentManifestFile(IFile file) {
		try {
			if (file == null) {
				getPersistentProperties().put(DEPLOYMENT_MANIFEST_FILE_PATH, (String) null);
			} else {
				getPersistentProperties().put(DEPLOYMENT_MANIFEST_FILE_PATH, file.getFullPath().toString());
			}
		} catch (Exception e) {
			Log.log(e);
		}
	}

	public void setDetailedData(CFApplicationDetail appDetails) {
		if (appDetails!=null) {
			this.appData.setValue(appDetails);
			this.instanceData.setValue(appDetails.getInstanceDetails());
		} else {
			this.appData.setValue(null);
			this.instanceData.setValue(null);
		}
	}

	public List<CFInstanceStats> getInstanceData() {
		return this.instanceData.getValue();
	}

	public void setError(Throwable t) {
		error.setValue(t);
	}

	@Override
	public CancelationToken createCancelationToken() {
		return cancelationTokens.create();
	}

	@Override
	public void cancelOperations() {
		cancelationTokens.cancelAll();
	}

	/**
	 * Print a message to the console associated with this application.
	 */
	public void print(String msg) {
		print(msg, LogType.STDOUT);
	}

	/**
	 * Print a message to the console associated with this application.
	 */
	public void printError(String string) {
		print(string, LogType.STDERROR);
	}

	/**
	 * Print a message to the console associated with this application.
	 */
	public void print(String msg, LogType type) {
		try {
			BootDashModelConsoleManager consoles = getBootDashModel().getElementConsoleManager();
			consoles.writeToConsole(this, msg+"\n", type);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	/**
	 * Attempt to refresh the data associated with this app in the model. Returns the
	 * refreshed element if this was succesful, null if the element was deleted (because during the
	 * refresh we discovered it not longer exists) and if something failed trying to refresh the element.
	 */
	public CloudAppDashElement refresh() throws Exception {
		debug("Refreshing element: "+this.getName());
		CFApplicationDetail data = getClient().getApplication(getName());
		if (data==null) {
			//Looks like element no longer exist in CF so remove it from the model
			CloudFoundryBootDashModel model = getBootDashModel();
			model.removeApplication(getName());
			return null;
		}
		getBootDashModel().updateApplication(data);
		return this;
	}

	@Override
	public CloudFoundryRunTarget getTarget() {
		return (CloudFoundryRunTarget) super.getTarget();
	}

	private ClientRequests getClient() {
		return getTarget().getClient();
	}

	public void push(CloudApplicationDeploymentProperties deploymentProperties, RunState runningOrDebugging,
			DebugSupport debugSupport, CancelationToken cancelationToken, UserInteractions ui, IProgressMonitor monitor)
			throws Exception {

		boolean isDebugging = runningOrDebugging == RunState.DEBUGGING;
		whileStarting(ui, cancelationToken, monitor, () -> {
			// Refresh app data and check that the application (still) exists in
			// Cloud Foundry
			// This also ensures that the 'diff change dialog' will pick up on
			// the latest changes.
			// TODO: should this refresh be moved closer to the where we
			// actually compute the diff?
			CloudAppDashElement updatedApp = this.refresh();
			if (updatedApp == null) {
				ExceptionUtil.coreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
						"No Cloud Application found for '" + getName() + "'"));
			}
			IProject project = getProject();
			if (project == null) {
				ExceptionUtil.coreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID,
						"Local project not associated to CF app '" + getName() + "'"));
			}

			checkTerminationRequested(cancelationToken, monitor);

			CloudApplicationDeploymentProperties properties = deploymentProperties == null
					? getBootDashModel().resolveDeploymentProperties(updatedApp, ui, monitor) : deploymentProperties;

			// Update JAVA_OPTS env variable with Remote DevTools Client secret
			DevtoolsUtil.setupEnvVarsForRemoteClient(properties.getEnvironmentVariables(),
					DevtoolsUtil.getSecret(project));
			JmxSupport jmxSupport = getJmxSupport();
			if (jmxSupport!=null) {
				jmxSupport.setupEnvVars(properties.getEnvironmentVariables());
			}
			if (debugSupport != null) {
				if (isDebugging) {
					debugSupport.setupEnvVars(properties.getEnvironmentVariables());
				} else {
					debugSupport.clearEnvVars(properties.getEnvironmentVariables());
				}
			}

			checkTerminationRequested(cancelationToken, monitor);

			CFPushArguments pushArgs = properties.toPushArguments(getBootDashModel().getCloudDomains(monitor));

			getClient().push(pushArgs, CancelationTokens.merge(cancelationToken, monitor));

			log("Application pushed to Cloud Foundry: " + getName());
		});
	}

	public void whileStarting(UserInteractions ui, CancelationToken cancelationToken, IProgressMonitor monitor, Task task) throws Exception {
		resetAndShowConsole();
		startOperationTracker.whileExecuting(ui, cancelationToken, monitor, task);
		refresh();
	}

	public void checkTerminationRequested(CancelationToken cancelationToken, IProgressMonitor mon)
			throws OperationCanceledException {
		if (mon != null && mon.isCanceled() || cancelationToken != null && cancelationToken.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	@Override
	public void delete() {
		CloudFoundryBootDashModel model = getBootDashModel();
		CloudAppDashElement cloudElement = this;
		cloudElement.cancelOperations();
		CancelationToken cancelToken = cloudElement.createCancelationToken();
		CloudApplicationOperation operation = new CloudApplicationOperation("Deleting: " + cloudElement.getName(), model,
				cloudElement.getName(), cancelToken) {

			@Override
			protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				// Delete from CF first. Do it outside of synch block to avoid
				// deadlock
				model.getRunTarget().getClient().deleteApplication(appName);
				model.getElementConsoleManager().terminateConsole(cloudElement);
				model.removeApplication(cloudElement.getName());
				cloudElement.setProject(null);
			}
		};

		// Allow deletions to occur concurrently with any other application
		// operation
		operation.setSchedulingRule(null);
		getBootDashModel().runAsynch(operation, ui());
	}

	private UserInteractions ui() {
		return injections().getBean(UserInteractions.class);
	}

	@Override
	public EnumSet<RunState> supportedGoalStates() {
		return CloudFoundryRunTarget.RUN_GOAL_STATES;
	}

	public synchronized JmxSupport getJmxSupport() {
		if (jmxSupport == null && getProject()!=null && getEnableJmxSshTunnel()) {
			this.jmxSupport = new JmxSupport(this,
					injections().getBean(JmxSshTunnelManager.class),
					injections().getBean(SshTunnelFactory.class)
			);
		}
		return jmxSupport;
	}

	private SimpleDIContext injections() {
		return this.getViewModel().getContext().injections;
	}

	public String getJmxUrl() {
		int port = getCfJmxPort();
		if (port>0) {
			return JmxSupport.getJmxUrl(port);
		}
		return null;
	}

	@Override
	public ImageDescriptor getRunStateImageDecoration() {
		if (this.getTarget() != null && this.getRunState() == RunState.RUNNING) {
			if (DevtoolsUtil.isDevClientAttached(this, ILaunchManager.RUN_MODE)) {
				return BootDashActivator.getDefault().getImageRegistry().getDescriptor(BootDashActivator.DT_ICON_ID);
			}
		}
		return null;
	}

	@Override
	public boolean matchesLiveProcessCommand(ExecuteCommandAction action) {
		UUID appGuid = getAppGuid();
		return appGuid!=null && appGuid.toString().equals(action.getProcessId());
	}

	@Override
	public Image getPropertiesTitleIconImage() {
		return BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CLOUD_ICON);
	}

	@Override
	public Disposable connectLog(ApplicationLogConsole logConsole) {
		if (logConsole.getLogStreamingToken() == null) {
			try {
				ClientRequests client = getClient();
				return client.streamLogs(getName(), logConsole);
			} catch (Exception e) {
				logConsole.writeApplicationLog("Failed to stream contents from "+getTarget().getDisplayName()+" due to: " + e.getMessage(),
						LogType.STDERROR);
			}
		}
		return null;
	}

	@Override
	public String getProtocol() {
		return "https";
	}

}
