/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.cloudfoundry.uaa.UaaException;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import reactor.ipc.netty.channel.AbortedException;

public class CFCallableContext {

	public static final String UNAUTHORIZED_ERROR = "unauthorized";

	private final CFParamsProviderMessages paramsProviderMessages;
	private Throwable lastConnectionError;

	public CFCallableContext(CFParamsProviderMessages paramsProviderMessages) {
		this.paramsProviderMessages = paramsProviderMessages;
	}

	public <T> T checkConnection(Callable<T> callable) throws Exception {
		this.lastConnectionError = null;
		try {
			return callable.call();
		} catch (Exception e) {
			throw convertToCfVscodeError(e);
		}
	}

	private Exception convertToCfVscodeError(Exception e) {
		Throwable deepestCause = ExceptionUtil.getDeepestCause(e);
		if (deepestCause instanceof UaaException) {
			String error = ((UaaException) deepestCause).getError();
			if (error != null && error.contains(UNAUTHORIZED_ERROR)) {
				this.lastConnectionError = deepestCause;
				return new UnauthorizedException(this.paramsProviderMessages.unauthorised());
			}
		} else if (deepestCause instanceof AbortedException) {
			// This one is odd. It is thrown when a wrong token is specified, but
			// instead of getting an expected UaaException, this AbortedException is thrown instead by reactor netty.
			this.lastConnectionError = deepestCause;
			return new UnauthorizedException(this.paramsProviderMessages.unauthorised());
		} else if (deepestCause instanceof UnknownHostException) {
			this.lastConnectionError = deepestCause;
			String message = ExceptionUtil.getMessage(deepestCause);
			return new ConnectionException(this.paramsProviderMessages.noNetworkConnection() + " " + message);
		}
		return e;
	}

	public boolean hasConnectionError() {
		return this.lastConnectionError != null;
	}

}
