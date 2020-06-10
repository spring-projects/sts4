/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringProcessDescriptor {
	
	private static final Logger log = LoggerFactory.getLogger(SpringProcessDescriptor.class);

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

	private final String processKey;
	private final String processID;
	private final String processName;
	private final VirtualMachineDescriptor vm;
	
	private SpringProcessStatus status;
	private String projectName;
	
	public SpringProcessDescriptor(String processKey, String processID, String processName, VirtualMachineDescriptor vm) {
		this.processKey = processKey;
		this.processID = processID;
		this.processName = processName;
		this.vm = vm;
		
		this.status = SpringProcessStatus.UNKNOWN;
	}

	public String getProcessKey() {
		return this.processKey;
	}
	
	public String getProcessID() {
		return processID;
	}
	
	public String getProcessName() {
		return processName;
	}
	
	public VirtualMachineDescriptor getVm() {
		return vm;
	}

	public SpringProcessStatus getStatus() {
		return this.status;
	}

	@Override
	public int hashCode() {
		return processKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpringProcessDescriptor other = (SpringProcessDescriptor) obj;
		if (processKey == null) {
			if (other.processKey != null)
				return false;
		} else if (!processKey.equals(other.processKey))
			return false;
		return true;
	}

	public CompletableFuture<Void> updateStatus(Predicate<String> projectIsKnown, Predicate<String> projectHasActuators) {
		return CompletableFuture.supplyAsync(() -> {
			this.status = checkStatus(projectIsKnown, projectHasActuators);
			return null;
		});
	}
	
	private SpringProcessStatus checkStatus(Predicate<String> projectIsKnown, Predicate<String> projectHasActuators) {
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(this.getVm());
			if (shouldIgnore(this.getVm(), vm)) {
				return SpringProcessStatus.IGNORE;
			}
			if (shouldAutoConnect(this.getVm(), vm, projectIsKnown, projectHasActuators)) {
				return SpringProcessStatus.AUTO_CONNECT;
			}

			return SpringProcessStatus.REGULAR;
		}
		catch (Exception e) {
			return SpringProcessStatus.IGNORE;
		}
		finally {
			if (vm != null) {
				try {
					vm.detach();
				}
				catch (Exception e) {
					log.error("error detaching from vm: " + this.getVm().id(), e);
				}
			}
		}
	}

	private boolean shouldIgnore(VirtualMachineDescriptor vmDescriptor, VirtualMachine vm) {
		try {
			String displayName = vmDescriptor.displayName();
			if (displayName != null && displayName.startsWith(ECLIPSE_PROCESS_DISPLAY_NAME_PREFIX)) {
				log.info("Eclipse process found, do not connect: " + vmDescriptor.id());
				return true;
			}
			
			Properties systemProperties = vm.getSystemProperties();
			
			Object languageServerIndicatorProperty = systemProperties.get(LANGUAGE_SERVER_PROPERTY);
			if (languageServerIndicatorProperty != null) {
				log.info("language server process found, do not connect: " + vmDescriptor.id());
				return true;
			}
			
		}
		catch (Exception e) {
			return true;
		}
		
		return false;
	}

	private boolean shouldAutoConnect(VirtualMachineDescriptor vmDescriptor, VirtualMachine vm, Predicate<String> projectIsKnown, Predicate<String> projectHasActuators) {
		try {
			Properties systemProperties = vm.getSystemProperties();
			
			Object projectName = systemProperties.get(SPRING_APP_PROJECT_NAME_PROPERTY);
			if (projectName instanceof String) {
				log.info("Spring boot process found: " + projectName);
				this.projectName = (String) projectName;
				
				boolean knownProject = projectIsKnown.test((String) projectName);
				boolean hasActuators = projectHasActuators.test((String)projectName);

				log.info("Spring boot process details: " + knownProject + " - " + hasActuators);
				
				return knownProject && hasActuators;
			}
		}
		catch (Exception e) {
			return false;
		}
		
		return false;
	}

	public String getProjectName() {
		return projectName;
	}

	@Override
	public String toString() {
		return "SpringProcessDescriptor [processKey=" + processKey + ", processID=" + processID + ", processName="
				+ processName + "]";
	}
	
	

}
