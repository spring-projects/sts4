/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.cloudfoundry.uaa.UaaException;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import reactor.ipc.netty.channel.AbortedException;

/**
 * This is a stateful callable context that is "aware" of CF errors, and is not
 * suitable for reuse as it may cache errors.
 */
public class CFCallableContext {

	private final TargetDiagnosticMessages diagnosticMessages;
	private Exception lastConnectionError;
	private long lastErrorTime = 0;

	public CFCallableContext(TargetDiagnosticMessages diagnosticMessages) {
		this.diagnosticMessages = diagnosticMessages;
	}

	public <T> T run(Callable<T> callable) throws Exception {
		this.lastConnectionError = null;
		try {
			return callable.call();
		} catch (Exception e) {
			lastErrorTime = System.currentTimeMillis();
			throw convertToCfVscodeError(e);
		}
	}

	protected Exception convertToCfVscodeError(Exception e) {
		this.lastConnectionError = getConnectionError(e);
		// return the "converted" error if it is available
		if (this.lastConnectionError != null) {
			return this.lastConnectionError;
		} 
		return e;
	}

	protected Exception getConnectionError(Exception e) {
		Throwable deepestCause = ExceptionUtil.getDeepestCause(e);

		if (deepestCause instanceof UaaException || deepestCause instanceof AbortedException
				|| deepestCause instanceof SocketException || deepestCause instanceof UnknownHostException) {
			if (this.diagnosticMessages != null) {
				return new ConnectionException(this.diagnosticMessages.getConnectionError());
			} else {
				return new ConnectionException(CfTargetsInfoProvder.DEFAULT_MESSAGES.getConnectionError());
			}
		}
		return null;
	}

	public boolean hasExpiredConnectionError() {
		return this.lastConnectionError != null && System.currentTimeMillis() - lastErrorTime > CFTargetCache.ERROR_EXPIRATION.toMillis();
	}
}