/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import static org.springframework.ide.eclipse.boot.dash.cf.debug.SshDebugLaunchConfigurationDelegate.getApp;
import static org.springframework.ide.eclipse.boot.dash.cf.debug.SshDebugLaunchConfigurationDelegate.getLaunchType;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.model.IDebugTarget;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Uses ssh tunnelling on Diego to support debugging of app running on CF.
 *
 * @author Kris De Volder
 */
public class SshDebugSupport extends DebugSupport {

	public static final SshDebugSupport INSTANCE = new SshDebugSupport();

	private static final int REMOTE_DEBUG_PORT = 47822;
	private static final String REMOTE_DEBUG_JVM_ARGS = "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n,address="+REMOTE_DEBUG_PORT;
	private static final String JAVA_OPTS = "JAVA_OPTS";

	private SshDebugSupport() {}

	@Override
	public boolean isSupported(CloudAppDashElement app) {
		String notSupportedMessage = getNotSupportedMessage(app);
		return notSupportedMessage==null;
	}

	@Override
	public String getNotSupportedMessage(CloudAppDashElement app) {
		//TODO: There are a number of different ways that ssh and/or diego might be disabled for
		//  an app. e.g. it can be disabled on the app itself, on the space, on the org or
		//  on the whole CF installation. This check should recognize these situation.
		//  At the moment it pretty much returns true if the CF has global info about the 'ssh-host',
		//  but this probably usually the case on any recent enough version of PCF, even if
		//  ssh has been explicitly disabled.
		CloudFoundryRunTarget target = app.getTarget();
		try {
			if (target.getSshClientSupport().getSshHost()==null) {
				return "Cloud controller doesn't specify an ssh-host. This probably means your version of CloudFoundry doesn't support SSH.";
			}
			return null;
		} catch (Exception e) {
			BootDashActivator.log(e); //for traceability
			String msg = ExceptionUtil.getMessage(e);
			if (!StringUtil.hasText(msg)) {
				msg = "Exception: "+e.getClass().getName();
			}
			return msg;
		}
	}

	@Override
	public boolean isDebuggerAttached(CloudAppDashElement app) {
		ILaunchConfiguration conf = SshDebugLaunchConfigurationDelegate.findConfig(app);
		if (conf!=null) {
			for (ILaunch l : BootLaunchUtils.getLaunches(conf)) {
				if (!l.isTerminated()) {
					for (IDebugTarget dt : l.getDebugTargets()) {
						if (!dt.isTerminated()) {
							//Active debug target found, so debugger is attached.
							return true;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Operation<?> createOperation(CloudAppDashElement app, String opName, UserInteractions ui, CancelationToken cancelationToken) {
		return new SshDebugStartOperation(app, this, cancelationToken);
	}

	@Override
	public void setupEnvVars(Map<String, String> env) {
		String javaOpts = clearJavaOpts(env.get(JAVA_OPTS));
		StringBuilder sb = new StringBuilder(javaOpts);
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(REMOTE_DEBUG_JVM_ARGS);
		env.put(JAVA_OPTS, sb.toString());

	}

	private static String clearJavaOpts(String opts) {
		if (opts!=null) {
			opts = opts.replaceAll(REMOTE_DEBUG_JVM_ARGS + "\\s*", "");
			return opts;
		} else {
			return "";
		}
	}


	@Override
	public void clearEnvVars(Map<String, String> env) {
		String jopts = clearJavaOpts(env.get(JAVA_OPTS));
		if (StringUtil.hasText(jopts)) {
			env.put(JAVA_OPTS, clearJavaOpts(env.get(JAVA_OPTS)));
		} else {
			env.remove(JAVA_OPTS);
		}
	}

	public int getRemotePort() {
		return REMOTE_DEBUG_PORT;
	}

	@Override
	public CloudAppDashElement getElementFor(ILaunch l, BootDashViewModel context) {
		try {
			ILaunchConfigurationType interestingType = getLaunchType();
			ILaunchConfiguration conf = l.getLaunchConfiguration();
			if (interestingType.equals(conf.getType())) {
				return getApp(conf, context);
			}
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

}
