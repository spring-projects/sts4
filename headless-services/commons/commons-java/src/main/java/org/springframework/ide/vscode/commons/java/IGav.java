/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.protocol.java.Gav;

public interface IGav {
	
	String getGroupId();
	
	String getArtifactId();
	
	String getVersion();
	
	static IGav create(Gav gav) {
		return new IGav() {
			
			@Override
			public String getVersion() {
				return gav.version();
			}
			
			@Override
			public String getGroupId() {
				return gav.groupId();
			}
			
			@Override
			public String getArtifactId() {
				return gav.artifactId();
			}
		};
	}
}
