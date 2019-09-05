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
	
	public SpringProcessDescriptor(String processKey, String processID, String processName, VirtualMachineDescriptor vm) {
		this.processKey = processKey;
		this.processID = processID;
		this.processName = processName;
		this.vm = vm;
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
		return processID + " (" + processName + ") ";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processID == null) ? 0 : processID.hashCode());
		result = prime * result + ((processKey == null) ? 0 : processKey.hashCode());
		result = prime * result + ((processName == null) ? 0 : processName.hashCode());
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
		SpringProcessDescriptor other = (SpringProcessDescriptor) obj;
		if (processID == null) {
			if (other.processID != null)
				return false;
		} else if (!processID.equals(other.processID))
			return false;
		if (processKey == null) {
			if (other.processKey != null)
				return false;
		} else if (!processKey.equals(other.processKey))
			return false;
		if (processName == null) {
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		return true;
	}

}
