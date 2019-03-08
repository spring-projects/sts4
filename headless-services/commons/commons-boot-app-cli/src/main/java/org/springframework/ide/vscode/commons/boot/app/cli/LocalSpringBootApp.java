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
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LocalSpringBootApp extends AbstractSpringBootApp {

	private static final Logger logger = LoggerFactory.getLogger(LocalSpringBootApp.class);

	private VirtualMachine vm;
	private VirtualMachineDescriptor vmd;

	private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private static LocalSpringBootAppCache cache = new LocalSpringBootAppCache();

	public static Collection<SpringBootApp> getAllRunningJavaApps() throws Exception {
		return cache.getAllRunningJavaApps();
	}

	public static Collection<SpringBootApp> getAllRunningSpringApps() throws Exception {
		return getAllRunningJavaApps().parallelStream().filter(app -> app.hasUsefulJmxBeans()).collect(CollectorUtil.toImmutableList());
	}

	public LocalSpringBootApp(VirtualMachineDescriptor vmd) throws AttachNotSupportedException, IOException {
		try {
			this.vm = VirtualMachine.attach(vmd);
			this.vmd = vmd;
		} catch (IOException | AttachNotSupportedException e) {
			// Dispose JMX connection before throwing exception
			dispose();
			throw e;
		}
	}


	@Override
	protected String getJmxUrl() {
		String address = null;
		try {
			address = withTimeout(() -> vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS));
		} catch (Exception e) {
			//ignore
		}
		if (address==null) {
			try {
				address = withTimeout(() -> vm.startLocalManagementAgent());
			} catch (Exception e) {
				logger.error("Error starting local management agent", e);
			}
		}
		return address;
	}

	@Override
	public String getProcessID() {
		return vmd.id();
	}

	@Override
	public String getProcessName() {
		String rawName = vmd.displayName();
		int firstSpace = rawName.indexOf(' ');
		return firstSpace < 0 ? rawName : rawName.substring(0, firstSpace);
	}

	@Override
	public Properties getSystemProperties() throws Exception {
		try {
			return withTimeout(() -> vm.getSystemProperties());
		} catch (Exception e) {
			logger.error("Fetching systemprops from local app failed: {}", ExceptionUtil.getMessage(e));
			throw e;
		}
	}

	protected boolean contains(String[] cpElements, String element) {
		for (String cpElement : cpElements) {
			if (cpElement.contains(element)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "LocalSpringBootApp [id=" +getProcessID() + ", name=`"+getProcessName()+"`]";
	}

	/**
	 * For testing / investigation purposes. Dumps out as much information as possible
	 * that can be onbtained from the jvm, without accessing JMX.
	 */
	public void dumpJvmInfo() throws IOException {
		System.out.println("--- vm infos ----");
		System.out.println("id = "+vm.id());
		System.out.println("displayName = "+vmd.displayName());
		dump("agentProperties", vm.getAgentProperties());
		dump("systemProps", vm.getSystemProperties());
		System.out.println("-----------------");
	}

	private void dump(String name, Properties props) {
		System.out.println(name + " = {");
		for (Entry<Object, Object> prop : props.entrySet()) {
			System.out.println("  "+prop.getKey()+" = "+prop.getValue());
		}
		System.out.println("}");
	}

	@Override
	public void dispose() {
		if (vm!=null) {
			logger.info("SpringBootApp disposed: "+this);
			try {
				withTimeout(() -> { vm.detach(); return null; });
			} catch (Exception e) {
			}
			vm = null;
		}
		if (vmd!=null) {
			vmd = null;
		}
		super.dispose();
	}
}
