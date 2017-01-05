/*******************************************************************************
 * Copyright (c) 2009-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;

/**
 * Enum used for the state of an instance
 *
 * Note: copied over from CF V1 client code.
 *
 * @author Thomas Risberg
 */
public enum InstanceState {
	DOWN, STARTING, RUNNING, CRASHED, FLAPPING, UNKNOWN;

	public static InstanceState valueOfWithDefault(String s) {
		try {
			return InstanceState.valueOf(s);
		} catch (IllegalArgumentException e) {
			return InstanceState.UNKNOWN;
		}
	}
}
