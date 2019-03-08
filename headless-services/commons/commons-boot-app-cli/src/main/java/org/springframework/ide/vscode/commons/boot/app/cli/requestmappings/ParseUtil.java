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
package org.springframework.ide.vscode.commons.boot.app.cli.requestmappings;

import java.util.Arrays;

public class ParseUtil {

	public static String[] splitPaths(String paths) {
		return Arrays.stream(paths.split("\\|\\|"))
				.map(s -> s.trim())
				.filter(s -> !s.isEmpty())
				.map(s -> {
					if (s.charAt(0) != '/') {
						return '/' + s;
					} else {
						return s;
					}
				})
				.toArray(String[]::new);
	}

}
