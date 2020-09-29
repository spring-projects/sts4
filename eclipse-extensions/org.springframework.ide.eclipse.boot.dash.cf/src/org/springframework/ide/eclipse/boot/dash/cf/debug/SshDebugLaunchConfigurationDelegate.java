/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.launch.AbstractBootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.WaitFor;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

@SuppressWarnings("restriction")
public class SshDebugLaunchConfigurationDelegate extends AbstractBootLaunchConfigurationDelegate {

	public static final String TYPE_ID = "org.springframework.ide.eclipse.boot.dash.ssh.tunnel.launch";

	private static final long DEBUG_CONNECT_TIMEOUT = 20000;
	public static final String RUN_TARGET = "ssh.debug.runtarget.id";
	public static final String APP_NAME = "ssh.debug.app.name";
	public static final String INSTANCE_IDX = "ssh.debug.app.instance";

	private static BootDashViewModel getContext() {
		//TODO: it may be necessary to allow injecting this, for example via setting a threadlocal,
		// to make code more amenable to unit testing.
		//This method is here because LaunchConf delegates are created by eclipse debug framework and
		// so we can't easily inject a context object into it.
		//The only method that should be calling this is 'launch'. Everything else should be doing
		// the proper thing and pass in the model as parameter somehow.
		return BootDashActivator.getDefault().getModel();
	}

	@Override
	public void launch(ILaunchConfiguration conf, String mode, ILaunch launch, IProgressMonitor mon)
			throws CoreException {
		conf = configureClassPathProviders(conf);
		Assert.isTrue(ILaunchManager.DEBUG_MODE.equals(mode));
		BootDashViewModel context = getContext();
		mon.beginTask("Establish SSH Debug Connection to "+getAppName(conf)+" on "+getRunTarget(conf, context), 4);
		CloudAppDashElement app = null;
		try {
			CloudFoundryRunTarget target = getRunTarget(conf, context);
			SshDebugSupport debugSupport = getDebugSupport(conf, context);

			app = getApp(conf, context);

			if (app!=null && target!=null && debugSupport.isSupported(app)) {
				//1: determine SSH tunnel parameters
				app.log("Fetching SSH tunnel parameters...");
				SshClientSupport sshInfo = target.getSshClientSupport();
				SshHost sshHost = sshInfo.getSshHost();
				String sshUser = sshInfo.getSshUser(app.getAppGuid(), getInstanceIndex(conf));
				String sshCode = sshInfo.getSshCode();
				int remotePort = debugSupport.getRemotePort();

				app.log("SSH tunnel parameters:");
				app.log("  host: "+sshHost);
				app.log("  user: "+sshUser);
				app.log("  code: "+sshCode);
				app.log("  remote port: "+remotePort);
				mon.worked(1);

				//2: create tunnel
				app.log("Creating tunnel...");
				SshTunnel tunnel = new SshTunnelImpl(sshHost, sshUser, sshCode, remotePort, app); //TODO: use SshTunnelFactory?

				//3: connect debugger stuff
				app.log("Launching remote debug connector...");
				launchRemote(tunnel, conf, launch, new SubProgressMonitor(mon, 1));
				app.log("Launching remote debug connector... DONE");
			}
		} catch (Exception e) {
			if (app!=null) {
				app.log("ERROR: "+ExceptionUtil.getMessage(e));
			}
			throw ExceptionUtil.coreException(e);
		} finally {
			mon.done();
		}
	}

	public static int getInstanceIndex(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(INSTANCE_IDX, 0);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return 0;
	}

	private SshDebugSupport getDebugSupport(ILaunchConfiguration conf, BootDashViewModel context) {
		CloudAppDashElement app = getApp(conf, context);
		if (app!=null) {
			DebugSupport ds = app.getDebugSupport();
			if (ds instanceof DebugSupport) {
				return (SshDebugSupport) ds;
			}
		}
		return null;
	}


	public static String getAppName(ILaunchConfiguration conf) {
		return getString(conf, APP_NAME);
	}

	public static CloudAppDashElement getApp(ILaunchConfiguration conf, BootDashViewModel context) {
		String appName = getAppName(conf);
		if (appName!=null) {
			BootDashModel section = context.getSectionByTargetId(getRunTargetId(conf));
			if (section instanceof CloudFoundryBootDashModel) {
				CloudFoundryBootDashModel cfmodel = (CloudFoundryBootDashModel) section;
				return cfmodel.getApplication(appName);
			}
		}
		return null;
	}

	public static void setAppName(ILaunchConfigurationWorkingCopy conf, String name) {
		if (name!=null) {
			conf.setAttribute(APP_NAME, name);
		} else {
			conf.removeAttribute(APP_NAME);
		}
	}

	public static void setRunTarget(ILaunchConfigurationWorkingCopy conf, CloudFoundryRunTarget target) {
		if (target!=null) {
			conf.setAttribute(RUN_TARGET, target.getId());
		} else {
			conf.removeAttribute(RUN_TARGET);
		}
	}

	private static String getRunTargetId(ILaunchConfiguration conf) {
		String at = RUN_TARGET;
		return getString(conf, at);
	}


	protected static String getString(ILaunchConfiguration conf, String attName) {
		try {
			return conf.getAttribute(attName, (String)null);
		} catch (CoreException e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	public static CloudFoundryRunTarget getRunTarget(ILaunchConfiguration conf, BootDashViewModel context) {
		try {
			String id = conf.getAttribute(RUN_TARGET, (String)null);
			if (id!=null) {
				RunTarget target = context.getRunTargetById(id);
				if (target instanceof CloudFoundryRunTarget) {
					return (CloudFoundryRunTarget) target;
				}
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	/**
	 * Create debugging target similar to a remote debugging session would and add them to the launch.
	 * This is to support debugging of the remote boot-app that is reachable over http tunnel
	 * the client creates. From our side this just as if we are opening a remote debug
	 * session to the client.
	 */
	private void launchRemote(final SshTunnel tunnel, ILaunchConfiguration configuration, final ILaunch launch, IProgressMonitor _monitor) throws CoreException {
		int port = tunnel.getLocalPort();
		final IProgressMonitor monitor = _monitor==null?new NullProgressMonitor():_monitor;

		monitor.beginTask(NLS.bind(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Attaching_to__0_____1, new String[]{configuration.getName()}), 3);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

			//String connectorId = "org.eclipse.jdt.launching.socketListenConnector";//getVMConnectorId(configuration);
			String connectorId = "org.eclipse.jdt.launching.socketAttachConnector";
			final IVMConnector connector = JavaRuntime.getVMConnector(connectorId);
			if (connector == null) {
				abort(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Connector_not_specified_2, null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE);
			}

			final Map<String, String> argMap = new HashMap<>();

			int connectTimeout = Platform.getPreferencesService().getInt(
					LaunchingPlugin.ID_PLUGIN,
					JavaRuntime.PREF_CONNECT_TIMEOUT,
					JavaRuntime.DEF_CONNECT_TIMEOUT,
					null);
			argMap.put("hostname", "localhost");
			argMap.put("timeout", ""+connectTimeout);
			argMap.put("port", ""+port);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			monitor.worked(1);


			monitor.subTask(LaunchingMessages.JavaRemoteApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// connect to remote VM
			try {
				new WaitFor(DEBUG_CONNECT_TIMEOUT) {
					@Override
					public void run() throws Exception {
						connector.connect(argMap, monitor, launch);
					}
				};
				new ProcessTracker(new ProcessListenerAdapter() {
					@Override
					public void debugTargetTerminated(ProcessTracker tracker, IDebugTarget target) {
						handleTermination(tracker, target.getLaunch());
					}
					@Override
					public void processTerminated(ProcessTracker tracker, IProcess process) {
						handleTermination(tracker, process.getLaunch());
					}
					private void handleTermination(ProcessTracker tracker, ILaunch targetLaunch) {
						if (launch.equals(targetLaunch)) {
							tracker.dispose();
							tunnel.dispose();
						}
					}
				});
			} catch (Exception e) {
				terminateAllTargets(launch);
				throw ExceptionUtil.coreException(e);
			}

			// check for cancellation
			if (monitor.isCanceled()) {
				terminateAllTargets(launch);
				return;
			}
		}
		finally {
			monitor.done();
		}
	}

	public void terminateAllTargets(final ILaunch launch) {
		//Note: its better to discconect debugtargets before terminating processes
		// because that allows a cleaner disconnect from the debugged process.
		// (If the devtools client process is terminated its no longer possible to talk to the
		// debugged process).
		IDebugTarget[] debugTargets = launch.getDebugTargets();
		for (int i = 0; i < debugTargets.length; i++) {
			IDebugTarget target = debugTargets[i];
			if (target.canDisconnect()) {
				try {
					target.disconnect();
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}
		}
		IProcess[] processes = launch.getProcesses();
		for (IProcess process : processes) {
			if (process.canTerminate()) {
				try {
					process.terminate();
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}
		}
	}


	public static ILaunchConfiguration getOrCreateLaunchConfig(CloudAppDashElement app) throws CoreException {
		IProject project = app.getProject();
		String appName = app.getName();
		CloudFoundryRunTarget target = app.getTarget();
		Assert.isTrue(project!=null);
		ILaunchConfiguration existing = findConfig(project, target, appName);
		ILaunchConfigurationWorkingCopy wc;
		if (existing!=null) {
			return existing;
		} else {
			wc = createConfiguration(project, target, appName);
			return wc.doSave();
		}
	}

	private static ILaunchConfigurationWorkingCopy createConfiguration(IProject project, CloudFoundryRunTarget target, String appName) throws CoreException {
		ILaunchConfigurationType configType = getLaunchType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchMan().generateLaunchConfigurationName("ssh-tunnel["+appName+"]"));
		BootLaunchConfigurationDelegate.setProject(wc, project);
		setRunTarget(wc, target);
		setAppName(wc, appName);
		wc.setMappedResources(new IResource[] {project});
		return wc;
	}

	public static ILaunchConfiguration findConfig(CloudAppDashElement app) {
		IProject project = app.getProject();
		String appName = app.getName();
		CloudFoundryRunTarget target = app.getTarget();
		return findConfig(project, target, appName);
	}

	private static ILaunchConfiguration findConfig(IProject project, CloudFoundryRunTarget target, String appName) {
		try {
			if (project!=null) {
				for (ILaunchConfiguration c : getLaunchMan().getLaunchConfigurations(getLaunchType())) {
					if (
							project.equals(BootLaunchConfigurationDelegate.getProject(c)) &&
							target.getId().equals(getRunTargetId(c)) &&
							appName.equals(getAppName(c))
							) {
						return c;
					}
				}
			}
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

	public static ILaunchConfigurationType getLaunchType() {
		return getLaunchMan().getLaunchConfigurationType(TYPE_ID);
	}


	public static void doLaunch(CloudAppDashElement app, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration conf = SshDebugLaunchConfigurationDelegate.getOrCreateLaunchConfig(app);
		conf.launch(ILaunchManager.DEBUG_MODE, monitor);
	}


}
