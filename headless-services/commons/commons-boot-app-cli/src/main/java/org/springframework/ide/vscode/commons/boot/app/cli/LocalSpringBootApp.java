/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.AsyncRunner;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import reactor.core.scheduler.Schedulers;

/**
 * @author Martin Lippert
 */
public class LocalSpringBootApp extends AbstractSpringBootApp {

	private static final Logger logger = LoggerFactory.getLogger(LocalSpringBootApp.class);

	private static AsyncRunner async = new AsyncRunner(Schedulers.elastic());
	private static <T> T withTimeout(Callable<T> doit) throws Exception {
		return async.invoke(TIMEOUT, doit).get();
	}

	private VirtualMachine vm;
	private VirtualMachineDescriptor vmd;

	private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private static final Duration TIMEOUT = Duration.ofMillis(1000);

	private Boolean isSpringBootApp;

	private static LocalSpringBootAppCache cache = new LocalSpringBootAppCache();

	public static Collection<SpringBootApp> getAllRunningJavaApps() throws Exception {
		return cache.getAllRunningJavaApps();
	}

	public static Collection<SpringBootApp> getAllRunningSpringApps() throws Exception {
		return getAllRunningJavaApps().stream().filter(SpringBootApp::isSpringBootApp).collect(CollectorUtil.toImmutableList());
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
			address = vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS);
		} catch (Exception e) {
			//ignore
		}
		if (address==null) {
			try {
				address = vm.startLocalManagementAgent();
			} catch (IOException e) {
				logger.error("Error starting local management agent", e);
			}
		}
		return address;
	}

//	Supplier<String> jmxConnectUrl = Suppliers.memoize(() -> {
//	});

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
	public boolean isSpringBootApp() {
		if (isSpringBootApp==null) {
			try {
				isSpringBootApp = !containsSystemProperty("sts4.languageserver.name")
					&& (
							isSpringBootAppClasspath() ||
							isSpringBootAppSysprops()
					);
			} catch (Exception e) {
				//Couldn't determine if the VM is a spring boot app. Could be it already died. Or could be its not accessible (yet).
				// We will ignore the exception, pretend its not a boot app (most likely isn't) but DO NOT CACHE this result
				// so it will be retried again on the next polling loop.
				return false;
			}
		}
		return isSpringBootApp;
	}

	private boolean isSpringBootAppSysprops() throws Exception {
		Properties sysprops = getSystemProperties();
		return "org.springframework.boot.loader".equals(sysprops.getProperty("java.protocol.handler.pkgs"));
	}

	private boolean isSpringBootAppClasspath() throws Exception {
		return contains(getClasspath(), "spring-boot");
	}

	@Override
	public Properties getSystemProperties() throws Exception {
		return withTimeout(() -> vm.getSystemProperties());
	}

	public boolean containsSystemProperty(Object key) throws Exception {
		Properties props = getSystemProperties();
		return props.containsKey(key);
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
				vm.detach();
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
