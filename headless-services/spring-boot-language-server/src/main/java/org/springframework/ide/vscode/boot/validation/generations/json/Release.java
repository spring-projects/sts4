/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations.json;

import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;

public class Release extends JsonHalLinks {
	
	private String version;
	private String status;
	private boolean current;
	private String referenceDocUrl;
	private String apiDocUrl;

	
	public Version getVersion() {
		return SpringProjectUtil.getVersion(version);
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isCurrent() {
		return current;
	}
	
	public String getReferenceDocUrl() {
		return referenceDocUrl;
	}
	
	public String getApiDocUrl() {
		return apiDocUrl;
	}
		
}
