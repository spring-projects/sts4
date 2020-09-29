/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JavaTypeUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Helper class, represents parsed info from a Resource, and provide method(s)
 * to display it somehow.
 */
public class SpringResource {

	public static final String BEAN_DEFINITION_IN = "BeanDefinition defined in";
	public static final String URL = "URL";
	private static final String CF_CLASSPATH_PREFIX = "/home/vcap/app/";
	private static final String CLASS = ".class";

	private String path;
	private IProject project;

	private static final Pattern BRACKETS = Pattern.compile("\\[[^\\]]*\\]");

	private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static final String REGEX_FQCN = ID_PATTERN + "(\\." + ID_PATTERN + ")*";

	public SpringResource(String resourceDefinition, IProject project) {
		this.project = project;
		parse(resourceDefinition);
	}

	private void parse(String resourceDefinition) {
		Matcher matcher = BRACKETS.matcher(resourceDefinition);
		if (matcher.find()) {
			String type = resourceDefinition.substring(0, matcher.start()).trim();
			path = resourceDefinition.substring(matcher.start() + 1, matcher.end() - 1);
			if (type.equals("file") && path.startsWith(CF_CLASSPATH_PREFIX)) {
				path = path.substring(CF_CLASSPATH_PREFIX.length());
			}
		} else if (Pattern.matches(REGEX_FQCN, resourceDefinition)) {
			// Resource is fully qualified Java type name
			path = resourceDefinition.replace('.', '/') + CLASS;
		} else {
			path = parseFromBeanDefIn(resourceDefinition);
			if (path == null) {
				path = resourceDefinition;
			}
		}
	}

	private String parseFromBeanDefIn(String resourceDefinition) {
		int beanDefInIndex = resourceDefinition.indexOf(BEAN_DEFINITION_IN);
		if (beanDefInIndex >= 0 && beanDefInIndex + BEAN_DEFINITION_IN.length() + 1 < resourceDefinition.length()) {
			String defInVal = resourceDefinition.substring(beanDefInIndex + BEAN_DEFINITION_IN.length() + 1);
			if (Pattern.matches(REGEX_FQCN, defInVal)) {
				return defInVal.replace('.', '/') + CLASS;
			}
		} 
		return null;
	}
	
	public String getResourcePath() {
		return path;
	}
	
	public String getClassName() {
		if (path != null && path.endsWith(".class")) {
			String clssName = path;
			
			// Check if the class name is relative to either WEB-INF or
			// project path (e.g. the bean is defined in the project)
			int index = clssName.lastIndexOf("/WEB-INF/classes/");
			int length = "/WEB-INF/classes/".length();
			if (index >= 0) {
				clssName = clssName.substring(index + length);
			} else if (project != null) {
				try {
					String possibleType = typeFromProjectRelativePath(project, clssName);
					if (possibleType != null) {
						clssName = possibleType;
					}
				} catch (Exception e) {
					Log.log(e);
				}
			}
			
			clssName = clssName.substring(0, clssName.lastIndexOf(".class"));
			clssName = clssName.replaceAll("\\\\|\\/", "."); // Tolerate both '/' and '\'.
			clssName = clssName.replace('$', '.'); // Replace inner classes '$' with JDT's '.'

			return clssName;
		}
		return null;
	}

	protected String typeFromProjectRelativePath(IProject project, String fullPath) throws Exception {
		IJavaProject javaProject = getJavaProject(project);
		return javaProject != null ? JavaTypeUtil.getFQTypeName(javaProject, fullPath) : null;
	}

	protected IJavaProject getJavaProject(IProject project) throws Exception {
		if (project != null && project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
			return JavaCore.create(project);
		}
		return null;
	}
}
