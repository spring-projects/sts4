/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Karthik Sankaranarayanan
 */
public class BeanTypesCompletionProcessor implements AnnotationAttributeCompletionProvider {

    private final SpringMetamodelIndex springIndex;

    public BeanTypesCompletionProcessor(SpringMetamodelIndex springIndex) {
        this.springIndex = springIndex;
    }

    @Override
    public List<AnnotationAttributeProposal> getCompletionCandidates(IJavaProject project, ASTNode node) {
        Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
        return Arrays.stream(beans)
                .map(bean -> new AnnotationAttributeProposal(getClass(bean.getType()), bean.getType(), bean.getType()))
                .distinct()
                .collect(Collectors.toList());
    }
    
    private String getClass(String type) {
    	if (type != null && type.lastIndexOf('.') >= 0) {
    		return type.substring(type.lastIndexOf('.') + 1);
    	}
    	return type;
    }
}
