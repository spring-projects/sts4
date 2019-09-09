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

import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringProcessDescriptor {
	
	private final String processKey;
	private final String processID;
	private final String processName;
	private final VirtualMachineDescriptor vm;
	
	private SpringProcessStatus status;
	
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

	public String getLabel() {
		return processID + " (" + processName + ")";
	}
	
	public void setStatus(SpringProcessStatus status) {
		this.status = status;
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
	
	


}
