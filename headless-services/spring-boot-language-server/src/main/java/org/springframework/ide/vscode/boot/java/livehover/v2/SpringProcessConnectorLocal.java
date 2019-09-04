/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringProcessConnectorLocal {
	

	private static final Logger log = LoggerFactory.getLogger(SpringProcessConnectorLocal.class);
	
	private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
	
	/**
	 * this property is automatically set my language servers to their own processes
	 * to avoid the spring boot language server to connect to itself or another language server process
	 * 
	 * this is primarily an optimization to avoid that overhead of trying to connect to such a process
	 */
	private static final String LANGUAGE_SERVER_PROPERTY = "sts4.languageserver.name";
	
	/**
	 * this property can be set to a running spring boot app at startup to indicatew that the
	 * process of this running boot app belongs to a specific project
	 * 
	 * in that case, the running process is only connected if the workspace contains that project
	 */
	private static final String SPRING_APP_PROJECT_NAME_PROPERTY = "spring.boot.project.name";
	
	/**
	 * common prefix for the vm descriptor display name of Eclipse processes, we can filter that out
	 * of the list of processes to connect to immediately to avoid further processing
	 */
	private static final String ECLIPSE_PROCESS_DISPLAY_NAME_PREFIX = "org.eclipse.equinox.launcher.Main";


	private final Collection<String> projects;
	private final Set<String> processes;
	private final SpringProcessConnectorService processConnectorService;
	private final SpringProcessLiveDataProvider liveDataProvider;
	
	public SpringProcessConnectorLocal(SpringProcessConnectorService processConnector, SpringProcessLiveDataProvider liveDataProvider,
			ProjectObserver projectObserver) {
		this.projects = Collections.synchronizedCollection(new HashSet<>());
		this.processes = Collections.synchronizedSet(new HashSet<>());
		this.liveDataProvider = liveDataProvider;
		this.processConnectorService = processConnector;

		projectObserver.addListener(new ProjectObserver.Listener() {
			@Override
			public void created(IJavaProject project) {
				projects.add(project.getElementName());
			}
			@Override
			public void deleted(IJavaProject project) {
				projects.remove(project.getElementName());
			}
			@Override
			public void changed(IJavaProject project) {
			}
		});
	}
	
	/**
	 * checks whether this class can operate normally or not - it is recommended to check this before calling out to this class
	 * (if the attach to VirtualMachine library is not around, this class cannot really do anything and will throw exceptions)
	 */
	public static boolean isAvailable() {
		try {
			Class<?> vmClass = VirtualMachine.class;
			return vmClass != null;
		}
		catch (NoClassDefFoundError e) {
			return false;
		}
	}

	public void searchForNewProcesses() {
		List<VirtualMachineDescriptor> currentVms = VirtualMachine.list();
		Set<String> currentVMKeys = new HashSet<>();

		for (VirtualMachineDescriptor vm : currentVms) {
			
			try {
				String processID = getProcessID(vm);
				String processName = getProcessName(vm);
				String processKey = SpringProcessConnectorService.getProcessKey(processID, processName);
				
				currentVMKeys.add(processKey);
				
				if (!processes.contains(processKey)) {
					connectLocalProcess(processKey, vm, true);
				}
			}
			catch (Exception e) {
				log.error("error looking into local process: " + vm.id(), e);
			}
		}
		
		Iterator<String> i = processes.iterator();
		while (i.hasNext()) {
			String processKey = i.next();
			if (!currentVMKeys.contains(processKey)) {
				i.remove();
				processConnectorService.disconnectProcess(processKey);
			}
		}
	}
	
	public void connectLocalProcess(String processKey, VirtualMachineDescriptor vmDescriptor, boolean checkAutoConnect) {
		processes.add(processKey);
		
		VirtualMachine vm = null;
		try {
			String jmxAddress = null;
			vm = VirtualMachine.attach(vmDescriptor);
			
			if (checkAutoConnect && !shouldAutoConnectToProcess(vmDescriptor, vm)) {
				return;
			}

			try {
				jmxAddress = vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS);
			} catch (Exception e) {
				//ignore
			}
	
			if (jmxAddress == null) {
				try {
					jmxAddress = vm.startLocalManagementAgent();
				} catch (Exception e) {
					log.error("Error starting local management agent", e);
				}
			}
			
			if (jmxAddress != null) {
				String processID = getProcessID(vmDescriptor);
				String processName = getProcessName(vmDescriptor);
				String urlScheme = "http";
				
				SpringProcessConnectorOverJMX connector = new SpringProcessConnectorOverJMX(
						liveDataProvider, processKey, jmxAddress, urlScheme, processID, processName, null, null, false);

				this.processConnectorService.connectProcess(processKey, connector);
			}
		}
		catch (Exception e) {
			log.error("exception while connecting to jvm process", e);
		}
		finally {
			if (vm != null) {
				try {
					vm.detach();
				}
				catch (Exception e) {
					log.error("error detaching from vm: " + vmDescriptor.id(), e);
				}
			}
		}
	}

	private boolean shouldAutoConnectToProcess(VirtualMachineDescriptor vmDescriptor, VirtualMachine vm) {
		try {
			String displayName = vmDescriptor.displayName();
			if (displayName != null && displayName.startsWith(ECLIPSE_PROCESS_DISPLAY_NAME_PREFIX)) {
				log.info("Eclipse process found, do not connect: " + vmDescriptor.id());
				return false;
			}
			
			Properties systemProperties = vm.getSystemProperties();
			
			Object projectNameProperty = systemProperties.get(SPRING_APP_PROJECT_NAME_PROPERTY);
			if (projectNameProperty instanceof String) {
				log.info("Spring boot process found: " + projectNameProperty);
				return this.processes.contains((String) projectNameProperty);
			}
			
			Object languageServerIndicatorProperty = systemProperties.get(LANGUAGE_SERVER_PROPERTY);
			if (languageServerIndicatorProperty != null) {
				log.info("language server process found, do not connect: " + vmDescriptor.id());
				return false;
			}
			
		}
		catch (Exception e) {
			return false;
		}
		
		// default case:
		return true;
	}

	private String getProcessID(VirtualMachineDescriptor descriptor) {
		return descriptor.id();
	}

	private String getProcessName(VirtualMachineDescriptor descriptor) {
		String rawName = descriptor.displayName();
		int firstSpace = rawName.indexOf(' ');
		return firstSpace < 0 ? rawName : rawName.substring(0, firstSpace);
	}
	
}
