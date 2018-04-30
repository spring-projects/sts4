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
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.util.Log;

public class BootProjectUtil {

	public static boolean isBootProject(IJavaProject jp) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				return IClasspathUtil.getBinaryRoots(cp).stream().anyMatch(cpe -> isBootEntry(cpe));
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}

	private static boolean isBootEntry(File cpe) {
		String name = cpe.getName();
		return name.endsWith(".jar") && name.startsWith("spring-boot");
	}

}
