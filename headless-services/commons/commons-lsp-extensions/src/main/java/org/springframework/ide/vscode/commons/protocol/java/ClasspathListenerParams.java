/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

public class ClasspathListenerParams {

	private String callbackCommandId;
	
	/**
	 * The requestor can set this to true to request 'batched' events 
	 * (if the client supports this, it will send multiple events in a single
	 * callback.
	 */
	private boolean batched = false;

	public ClasspathListenerParams() {}

	public ClasspathListenerParams(String callbackCommandId, boolean isBatched) {
		this.callbackCommandId = callbackCommandId;
		this.batched = isBatched;
	}

	public ClasspathListenerParams(String callbackCommandId) {
		this.callbackCommandId = callbackCommandId;
	}

	public String getCallbackCommandId() {
		return callbackCommandId;
	}

	public void setCallbackCommandId(String callbackCommandId) {
		this.callbackCommandId = callbackCommandId;
	}
	
	@Override
	public String toString() {
		return "ClasspathListenerParams [callbackCommandId=" + callbackCommandId + ", batched=" + batched + "]";
	}

	public boolean isBatched() {
		return batched;
	}

	public void setBatched(boolean batched) {
		this.batched = batched;
	}

}
