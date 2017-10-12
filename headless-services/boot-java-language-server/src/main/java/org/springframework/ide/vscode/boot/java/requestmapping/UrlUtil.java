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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class UrlUtil {


	public static Stream<String> processOrPaths(String pathExp) {
		if (pathExp.contains("||")) {
			String[] paths = pathExp.split(Pattern.quote("||"));
			return Stream.of(paths).map(String::trim);
		} else {
			return Stream.of(pathExp);
		}
	}


	public static String extractPath(String key) {
		if (key.startsWith("{[")) { //Case 2 (see above)
			//An almost json string. Unfortunately not really json so we can't
			//use org.json or jackson Mapper to properly parse this.
			int start = 2; //right after first '['
			int end = key.indexOf(']');
			if (end>=2) {
				return key.substring(start, end);
			}
		}
		//Case 1, or some unanticipated stuff.
		//Assume the key is the path, which is right for Case 1
		// and  probably more useful than null for 'unanticipated stuff'.
		return key;
	}

	/**
	 * Creates http URL string based on host, port and path
	 * @param host
	 * @param port
	 * @param path
	 * @return the resultant URL
	 */
	public static String createUrl(String host, String port, String path) {
		if (path==null) {
			path = "";
		}
		if (host!=null) {
			if (port != null) {
				if (!path.startsWith("/")) {
					path = "/" +path;
				}
				return "http://"+host+":"+port+path;
			}
		}
		return null;
	}

}
