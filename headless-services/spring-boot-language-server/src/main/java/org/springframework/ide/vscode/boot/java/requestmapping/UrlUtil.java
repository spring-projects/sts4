/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableSet;

public class UrlUtil {

	private static final ImmutableSet<String> LOCALHOST_ALIASES = ImmutableSet.of(
			"localhost",
			"127.0.0.1"
	);

	/**
	 * Creates http URL string based on host, port and path
	 * @param host
	 * @param port
	 * @param path
	 * @param contextPath
	 * @return the resultant URL
	 */
	public static String createUrl(String host, String port, String path, String contextPath) {
		if (path==null) {
			path = "";
		}
		if (host!=null) {
			if (port != null) {
				if (!path.startsWith("/")) {
					path = "/" +path;
				}
				if (StringUtil.hasText(contextPath)) {
					if (!contextPath.startsWith("/")) {
						contextPath = "/" + contextPath;
					}
					path = contextPath + path;
				}
				if ("80".equals(port)) {
					return "http://"+host+path;
				} else if ("443".equals(port)) {
					return "https://"+host+path;
				} else {
					if (LOCALHOST_ALIASES.contains(host)) {
						return "http://" + host + ":" + port + path;
					} else {
						return "https://" + host + ":" + port + path;
					}
				}
			}
		}
		return null;
	}

}
