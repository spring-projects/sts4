/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol;

/**
 * Json data object that is sent by process connected/disconnect/updated events. The main purpose
 * is to identify a process to an external client such as (real use case!) vscode-boot-dashboard
 * from Microsoft; or other 3rd party vscode-extension that want to integrate with
 * the live process connections tracked by vscode-spring-boot. 
 * 
 * @author Kris De Volder
 */
public class LiveProcessSummary {
	
	private String type;
	private String processKey;
	private String processName;
	private String pid;  //only meaningful for type = 'local'
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getProcessKey() {
		return processKey;
	}
	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
}
