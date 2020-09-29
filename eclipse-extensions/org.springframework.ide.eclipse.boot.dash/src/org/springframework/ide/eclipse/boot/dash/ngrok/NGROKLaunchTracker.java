/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Martin Lippert
 */
public class NGROKLaunchTracker {

	private static Map<String, NGROKClient> tunnels = new ConcurrentHashMap<String, NGROKClient>();

	public static void add(String tunnelName, NGROKClient ngrokClient, NGROKTunnel tunnel) {
		tunnels.put(tunnelName, ngrokClient);
	}

	public static NGROKClient get(String tunnelName) {
		return tunnels.get(tunnelName);
	}

	public static void remove(String tunnelName) {
		tunnels.remove(tunnelName);
	}

}
