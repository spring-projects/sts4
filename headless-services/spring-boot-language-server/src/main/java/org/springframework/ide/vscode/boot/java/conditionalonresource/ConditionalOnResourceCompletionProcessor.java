/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.conditionalonresource;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Karthik Sankaranarayanan
 */
public class ConditionalOnResourceCompletionProcessor implements AnnotationAttributeCompletionProvider {

    private Map<String, String> findResources(IJavaProject project) {
    	Map<String, String> resources = IClasspathUtil.getClasspathResources(project.getClasspath()).stream()
                .distinct()
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Paths.get(o1).compareTo(Paths.get(o2));
                    }
                })
                .map(r -> r.replaceAll("\\\\", "/"))
                .map(r -> "classpath:" + r)
                .collect(Collectors.toMap(key -> key, value -> value, (u, v) -> u, LinkedHashMap::new));

        return resources;
    }

	@Override
	public Map<String, String> getCompletionCandidates(IJavaProject project, ASTNode node) {
		return findResources(project);
	}

}