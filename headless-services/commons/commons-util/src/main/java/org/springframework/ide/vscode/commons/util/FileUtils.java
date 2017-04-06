/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.File;

/**
 * Utilitity methods for working with  files
 * 
 * @authro Kris De Volder
 * @author Alex Boyko
 *
 */
public class FileUtils {

	/**
	 * Find file given its fil name in the given folder or its parent folders
	 * @param folder Starting folder
	 * @param fileNameToFind Name of the file to find
	 * @return Found <code>File</code>
	 */
	public static File findFile(File folder, String fileNameToFind) {
		if (folder!=null && folder.exists()) {
			File file = new File(folder, fileNameToFind);
			if (file.isFile()) {
				return file;
			} else {
				return findFile(folder.getParentFile(), fileNameToFind);
			}
		}
		return null;
	}

}
