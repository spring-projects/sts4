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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Karthik Sankaranarayanan
 */
public class ConditionalOnResourceCompletionProcessor implements AnnotationAttributeCompletionProvider {

    private List<AnnotationAttributeProposal> findResources(IJavaProject project) {
    	List<AnnotationAttributeProposal> resources = IClasspathUtil.getClasspathResources(project.getClasspath()).stream()
                .distinct()
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Paths.get(o1).compareTo(Paths.get(o2));
                    }
                })
                .map(r -> r.replaceAll("\\\\", "/"))
                .map(r -> "classpath:" + r)
                .map(resource -> new AnnotationAttributeProposal(resource))
                .collect(Collectors.toList());

        return resources;
    }

	@Override
	public List<AnnotationAttributeProposal> getCompletionCandidates(IJavaProject project, ASTNode node) {
		return findResources(project);
	}

}