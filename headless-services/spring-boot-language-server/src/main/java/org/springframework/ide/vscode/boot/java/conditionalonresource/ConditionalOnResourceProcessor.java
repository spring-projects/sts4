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

import static org.springframework.ide.vscode.commons.util.StringUtil.camelCaseToHyphens;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.openrewrite.yaml.internal.grammar.JsonPathParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProposal;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.FuzzyMap.Match;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Karthik Sankaranarayanan
 */
public class ConditionalOnResourceProcessor implements CompletionProvider {

    private static final Logger log = LoggerFactory.getLogger(ConditionalOnResourceProcessor.class);

    private final JavaProjectFinder projectFinder;

    public ConditionalOnResourceProcessor(JavaProjectFinder projectFinder) {
        this.projectFinder = projectFinder;
    }

    @Override
    public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type,
                                   int offset, TextDocument doc, Collection<ICompletionProposal> completions) {

        try {
            Optional<IJavaProject> optionalProject = this.projectFinder.find(doc.getId());
            if (optionalProject.isEmpty()) {
                return;
            }

            IJavaProject project = optionalProject.get();

            // case: @ConditionalOnResource(resources=<*>)
            if (node instanceof SimpleName && node.getParent() instanceof MemberValuePair
                    && ("resources".equals(((MemberValuePair)node.getParent()).getName().toString()))) {
                computeProposalsForSimpleName(project, node, completions, offset, doc);
            }
            // case: @ConditionalOnResource(resources=<*>)
            else if (node instanceof SimpleName && node.getParent() instanceof QualifiedName && node.getParent().getParent() instanceof MemberValuePair
                    && ("resources".equals(((MemberValuePair)node.getParent().getParent()).getName().toString()))) {
                computeProposalsForSimpleName(project, node.getParent(), completions, offset, doc);
            }
            // case:@ConditionalOnResource(resources="prefix<*>")
            else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
                    &&  ("resources".equals(((MemberValuePair)node.getParent()).getName().toString()))) {
                if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
                    computeProposalsForStringLiteral(project, (StringLiteral) node, completions, offset, doc);
                }
            }
        }
        catch (Exception e) {
            log.error("problem while looking for ConditionalOnResource annotation proposals", e);
        }
    }

    private void addClasspathResourceProposals(IJavaProject project, TextDocument doc, int startOffset, int endOffset, String prefix, boolean includeQuotes, Collection<ICompletionProposal> completions) {
        String[] resources = findResources(project, prefix);

        double score = resources.length + 1000;
        for (String resource : resources) {

            DocumentEdits edits = new DocumentEdits(doc, false);

            if (includeQuotes) {
                edits.replace(startOffset, endOffset, "\"classpath:" + resource + "\"");
            }
            else {
                edits.replace(startOffset, endOffset, "classpath:" + resource);
            }

            String label = "classpath:" + resource;

            ICompletionProposal proposal = new AnnotationAttributeCompletionProposal(edits, label, label, null, score--);
            completions.add(proposal);
        }

    }

    private void computeProposalsForSimpleName(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) {
        int startOffset = node.getStartPosition();
        int endOffset = node.getStartPosition() + node.getLength();

        String unfilteredPrefix = node.toString().substring(0, offset - node.getStartPosition());
        addClasspathResourceProposals(project, doc, startOffset, endOffset, unfilteredPrefix, true, completions);
    }

    private void computeProposalsForStringLiteral(IJavaProject project, StringLiteral node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
        String prefix = identifyPropertyPrefix(doc.get(node.getStartPosition() + 1, offset - (node.getStartPosition() + 1)), offset - (node.getStartPosition() + 1));

        int startOffset = offset - prefix.length();
        int endOffset = offset;

        String unfilteredPrefix = node.getLiteralValue().substring(0, offset - (node.getStartPosition() + 1));
        addClasspathResourceProposals(project, doc, startOffset, endOffset, unfilteredPrefix, false, completions);
    }

    public String identifyPropertyPrefix(String nodeContent, int offset) {
        String result = nodeContent.substring(0, offset);

        int i = offset - 1;
        while (i >= 0) {
            char c = nodeContent.charAt(i);
            if (c == '}' || c == '{'  || c == '$' || c == '#') {
                result = result.substring(i + 1, offset);
                break;
            }
            i--;
        }

        return result;
    }

    private String[] findResources(IJavaProject project, String prefix) {
        String[] resources = IClasspathUtil.getClasspathResources(project.getClasspath()).stream()
                .distinct()
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Paths.get(o1).compareTo(Paths.get(o2));
                    }
                })
                .map(r -> r.replaceAll("\\\\", "/"))
                .filter(r -> ("classpath:" + r).contains(prefix))
                .toArray(String[]::new);

        return resources;
    }

}