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
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * Helper class, represents parsed info from a Resource, and provide method(s) to
 * display it somehow.
 */
public class SpringResource {

	private static final String FILE = "file";
	private static final String CLASS_PATH_RESOURCE = "class path resource";

	private String type;
	private String path;
	private IJavaProject project;

	private static final Pattern BRACKETS = Pattern.compile("\\[[^\\]]*\\]");

	public SpringResource(String toParse, IJavaProject project) {
		this.project = project;
		Matcher matcher = BRACKETS.matcher(toParse);
		if (matcher.find()) {
			type = toParse.substring(0, matcher.start()).trim();
			path = toParse.substring(matcher.start()+1, matcher.end()-1);
		} else {
			path = toParse;
		}
	}

	public String toMarkdown() {
		if (type==null) {
			return path; //path is just the raw text in this case
		}
		switch (type) {
		case FILE:
			return "`"+projectRelativePath(path)+"`";
		case CLASS_PATH_RESOURCE:
			return "`"+path+"`";
		default:
			return path;
		}
	}

	private String projectRelativePath(String pathStr) {
		Path path = Paths.get(pathStr);
		IClasspath classpath = project.getClasspath();
		Path outputFolder = classpath.getOutputFolder();
		System.out.println("outf = "+outputFolder);
		System.out.println("path = "+pathStr);
		if (path.startsWith(outputFolder)) {
			return outputFolder.relativize(path).toString();
		}
		return pathStr;
	}

}
