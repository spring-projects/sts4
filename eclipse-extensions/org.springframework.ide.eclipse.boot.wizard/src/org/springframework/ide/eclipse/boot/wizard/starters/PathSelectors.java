/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Predicate;

import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Predicates for testing file paths. Paths should be always separated with '/' and folders assumed to have trailing '/'
 *
 * @author Alex Boyko
 *
 */
public class PathSelectors {

	public static Predicate<String> rootFiles() {
		return path -> {
			return path.indexOf('/') < 0;
		};
	}

	public static Predicate<String> path(String path) {
		return path::equals;
	}

	public static Predicate<String> pattern(String glob) {
		PathMatcher matcher = null;
		try {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		} catch (Exception e) {
			Log.error("Inavlid glob pattern: " + glob);
		}
		final PathMatcher pathMatcher = matcher;
		return path -> {
			String[] pathArray = path.split("/");
			Path p = null;
			if (pathArray.length == 1) {
				p = Paths.get(pathArray[0]);
			} else if (pathArray.length > 1) {
				p = Paths.get(pathArray[0], Arrays.copyOfRange(pathArray, 1, pathArray.length));
			}
			if (p == null) {
				return false;
			} else {
				return pathMatcher != null && pathMatcher.matches(p);
			}
		};
	}

}
