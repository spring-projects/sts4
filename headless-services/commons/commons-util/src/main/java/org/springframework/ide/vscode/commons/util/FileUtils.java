/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utilitity methods for working with files
 * 
 * @author Kris De Volder
 * @author Alex Boyko
 */
public class FileUtils {

	/**
	 * Find file given its file name in the given folder or its parent folders
	 * 
	 * @param folder Starting folder
	 * @param fileNameToFind Name of the file to find
	 * @return Found <code>File</code>
	 */
	public static File findFile(File folder, String fileNameToFind) {
		return findFile(folder, fileNameToFind, true);
	}

	public static File findFile(File folder, String fileNameToFind, boolean recursiveUp) {
		if (folder != null && folder.exists()) {
			File file = new File(folder, fileNameToFind);
			if (file.isFile()) {
				return file;
			} else if (recursiveUp) {
				return findFile(folder.getParentFile(), fileNameToFind);
			}
		}
		return null;
	}
	
	public static Optional<File> findFile(File folder, List<PathMatcher> matchers, boolean recursiveUp) {
		if (folder != null && folder.exists()) {
			Optional<File> found = (folder.isDirectory() ? Arrays.stream(folder.listFiles()) : Stream.of(folder))
					.map(f -> f.toPath())
					.filter(p -> matchers.stream().filter(m -> m.matches(p)).findFirst().isPresent())
					.findFirst()
					.map(p -> p.toFile());
			if (found.isPresent()) {
				return found;
			} else {
				return recursiveUp ? findFile(folder.getParentFile(), matchers, recursiveUp) : found;
			}
		}
		return Optional.empty();
	}

}
