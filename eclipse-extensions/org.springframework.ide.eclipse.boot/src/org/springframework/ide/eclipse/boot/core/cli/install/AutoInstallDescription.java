/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

/**
 * Descriptor returned by IBootInstall.checkAutoInstall.
 * <p>
 * Specifies whether auto-install is possible as well as an explanation
 * message.
 * <p>
 * When installation is possible, the message describes what will be installed
 * (in user-readable format, i.e. this string is meant to be shown to the user
 * informing them what will be installed if they accept).
 * <p>
 * When installation is not possible, the message may offer an explanation of
 * the reason.
 */
public class AutoInstallDescription {

	public final boolean isPossible;
	public final String message;

	private AutoInstallDescription(boolean isPossible, String message) {
		super();
		this.isPossible = isPossible;
		this.message = message;
	}

	@Override
	public String toString() {
		return "AutoInstallDescription [isPossible=" + isPossible + ", message=" + message + "]";
	}

	public static AutoInstallDescription impossible(String message) {
		return new AutoInstallDescription(false, message);
	}

	public static AutoInstallDescription describe(String message) {
		return new AutoInstallDescription(true, message);
	}
}
