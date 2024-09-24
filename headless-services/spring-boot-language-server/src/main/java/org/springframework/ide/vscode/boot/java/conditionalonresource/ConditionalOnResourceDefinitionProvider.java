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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.IJavaDefinitionProvider;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ConditionalOnResourceDefinitionProvider implements IJavaDefinitionProvider {

    private static final Logger log = LoggerFactory.getLogger(ConditionalOnResourceDefinitionProvider.class);

    @Override
    public List<LocationLink> getDefinitions(CancelChecker cancelToken, IJavaProject project,
                                             TextDocumentIdentifier docId, CompilationUnit cu, ASTNode n, int offset) {

        if (n instanceof StringLiteral) {
            StringLiteral valueNode = (StringLiteral) n;

            String literalValue = valueNode.getLiteralValue();
            if (literalValue != null) {
                if (literalValue.startsWith("classpath")) {
                    return getDefinitionForClasspathResource(project, cu, valueNode, literalValue);
                }
            }
        }
        return Collections.emptyList();
    }


    private List<LocationLink> getDefinitionForClasspathResource(IJavaProject project, CompilationUnit cu, StringLiteral valueNode, String literalValue) {
        literalValue = literalValue.substring("classpath:".length());

        String[] resources = findResources(project, literalValue);

        List<LocationLink> result = new ArrayList<>();

        for (String resource : resources) {
            String uri = "file://" + resource;

            Position startPosition = new Position(cu.getLineNumber(valueNode.getStartPosition()) - 1,
                    cu.getColumnNumber(valueNode.getStartPosition()));
            Position endPosition = new Position(
                    cu.getLineNumber(valueNode.getStartPosition() + valueNode.getLength()) - 1,
                    cu.getColumnNumber(valueNode.getStartPosition() + valueNode.getLength()));
            Range nodeRange = new Range(startPosition, endPosition);

            LocationLink locationLink = new LocationLink(uri,
                    new Range(new Position(0, 0), new Position(0, 0)), new Range(new Position(0, 0), new Position(0, 0)),
                    nodeRange);

            result.add(locationLink);
        }

        return result;
    }

    private String[] findResources(IJavaProject project, String resource) {
        String[] resources = IClasspathUtil.getClasspathResourcesFullPaths(project.getClasspath())
                .filter(path -> path.toString().endsWith(resource))
                .map(path -> path.toString())
                .toArray(String[]::new);

        return resources;
    }

}