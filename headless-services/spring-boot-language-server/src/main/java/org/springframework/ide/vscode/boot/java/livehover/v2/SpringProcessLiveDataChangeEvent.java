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

/**
 * @author Martin Lippert
 */
public class SpringProcessLiveDataChangeEvent {
	
	private final SpringProcessLiveData[] updatedLiveData;

	public SpringProcessLiveDataChangeEvent(SpringProcessLiveData[] updatedLiveData) {
		this.updatedLiveData = updatedLiveData;
	}
	
	public SpringProcessLiveData[] getUpdatedLiveData() {
		return updatedLiveData;
	}

}
