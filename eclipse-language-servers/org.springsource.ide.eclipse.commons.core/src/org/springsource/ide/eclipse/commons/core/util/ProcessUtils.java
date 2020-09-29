/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Process Utilities making use of JDK tools
 *
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class ProcessUtils {

	/**
	 * Retrieves PID of a process
	 * @param p the process
	 * @return PID
	 */
	public static long getProcessID(Process p) {
		long result = -1;
		try {
			Field f = p.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			result = f.getLong(p);
			f.setAccessible(false);
		}
		catch (Exception ex) {
			throw new UnsupportedOperationException("Process PID calculation not supported on the current platform",
					ex);
		}
		return result;
	}

	/**
	 * Creates JMX Connector to a process specified by its PID
	 * @param pid the PID
	 * @return JMX connector
	 */
	public static JMXConnector createJMXConnector(String pid) {
		List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
		VirtualMachineDescriptor vmd = vmds.stream().filter(descriptor -> descriptor.id().equals(pid)).findFirst().orElse(null);
		if (vmd != null) {
			try {
				String agentUrl = VirtualMachine.attach(vmd).startLocalManagementAgent();
				if (agentUrl != null) {
					JMXServiceURL serviceUrl = new JMXServiceURL(agentUrl);
					return JMXConnectorFactory.connect(serviceUrl, null);
				}
			} catch (AttachNotSupportedException e) {
				CorePlugin.log(e);
			} catch (IOException e) {
				CorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Determines whether current JDK supports utilities defined in this class
	 * @return <code>true</code> for compatible JDK
	 */
	public static boolean isLatestJdkForTools() {
		try {
			return VirtualMachine.class.getDeclaredMethod("startLocalManagementAgent") != null;
		}
		catch (NoSuchMethodException | SecurityException e) {
			// ignore
		}
		return false;
	}

}
