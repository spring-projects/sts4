/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

public enum JmxSshTunnelStatus {

	/**
	 * Tunneling is not enabled.
	 */
	DISABLED,

	/**
	 * Tunneling is enabled, but the tunnel does not (yet) exist.
	 */
	INACTIVE,

	/**
	 * Tunneling is enabled and a active tunnel currently exists.
	 */
	ACTIVE;

	/**
	 * Nice label for display in ui.
	 */
	public String getLabel() {
		switch (this) {
		case DISABLED:
			return "Disabled";
		case INACTIVE:
			return "Enabled - SSH Tunnel not (yet) created";
		case ACTIVE:
			return "Enabled - SSH Tunnel is Active";
		default:
			throw new IllegalStateException("Missing switch case "+this);
		}
	}

}
