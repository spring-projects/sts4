/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.springframework.ide.vscode.commons.util.StringUtil;

public class UrlUtil {

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
				if (port.equals("80")) {
					return "http://"+host+path;
				} else {
					return "http://"+host+":"+port+path;
				}
			}
		}
		return null;
	}

}
