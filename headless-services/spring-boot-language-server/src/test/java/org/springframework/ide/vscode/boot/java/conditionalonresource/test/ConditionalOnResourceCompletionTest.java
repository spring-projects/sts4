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
package org.springframework.ide.vscode.boot.java.conditionalonresource.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.bootiful.AdHocPropertyHarnessTestConf;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.editor.harness.AdHocPropertyHarness;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.boot.index.cache.IndexCache;
import org.springframework.ide.vscode.boot.index.cache.IndexCacheVoid;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Karthik Sankaranarayanan
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import({AdHocPropertyHarnessTestConf.class, ConditionalOnResourceCompletionTest.TestConf.class})
public class ConditionalOnResourceCompletionTest {

    @Autowired private BootLanguageServerHarness harness;
    @Autowired private JavaProjectFinder projectFinder;

    private Editor editor;

    @Autowired private PropertyIndexHarness indexHarness;
    @Autowired private AdHocPropertyHarness adHocProperties;

    @Configuration
    static class TestConf {

        @Bean MavenJavaProject testProject() throws Exception {
            return ProjectsHarness.INSTANCE.mavenProject("test-annotation-conditionalonresource");
        }

        @Bean PropertyIndexHarness indexHarness(ValueProviderRegistry valueProviders) {
            return new PropertyIndexHarness(valueProviders);
        }

        @Bean JavaProjectFinder projectFinder(MavenJavaProject testProject) {
            return new JavaProjectFinder() {

                @Override
                public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
                    return Optional.ofNullable(testProject);
                }

                @Override
                public Collection<? extends IJavaProject> all() {
                    // TODO Auto-generated method stub
                    return testProject == null ? Collections.emptyList() : ImmutableList.of(testProject);
                }
            };
        }

        @Bean BootLanguageServerHarness harness(SimpleLanguageServer server, BootLanguageServerParams serverParams, PropertyIndexHarness indexHarness, JavaProjectFinder projectFinder) throws Exception {
            return new BootLanguageServerHarness(server, serverParams, indexHarness, projectFinder, LanguageId.JAVA, ".java");
        }

        @Bean BootLanguageServerParams serverParams(SimpleLanguageServer server, JavaProjectFinder projectFinder, ValueProviderRegistry valueProviders, PropertyIndexHarness indexHarness) {
            BootLanguageServerParams testDefaults = BootLanguageServerHarness.createTestDefault(server, valueProviders);
            return new BootLanguageServerParams(
                    projectFinder,
                    ProjectObserver.NULL,
                    indexHarness.getIndexProvider(),
                    testDefaults.typeUtilProvider
            );
        }

        @Bean IndexCache symbolCache() {
            return new IndexCacheVoid();
        }

        @Bean SourceLinks sourceLinks(SimpleTextDocumentService documents, CompilationUnitCache cuCache) {
            return SourceLinkFactory.NO_SOURCE_LINKS;
        }

    }

    @BeforeEach
    public void setup() throws Exception {
        IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-annotation-conditionalonresource");
        harness.useProject(testProject);
        harness.intialize(null);
    }

    @Test
    void testPrefixCompletionWithParamName() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=foo<*>)");

        assertClasspathCompletions(
                "@ConditionalOnResource(resources=\"classpath:org/test/foo\"<*>)");
    }

    @Test
    void testEmptyBracketsCompletionWithParamName() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=<*>)");

        assertClasspathCompletions(
                "@ConditionalOnResource(resources=\"classpath:a-random-resource-root.md\"<*>)",
                "@ConditionalOnResource(resources=\"classpath:org/random-resource-org.md\"<*>)",
                "@ConditionalOnResource(resources=\"classpath:org/test/foo\"<*>)",
                "@ConditionalOnResource(resources=\"classpath:org/test/random-resource-org-test.txt\"<*>)");
    }

    @Test
    void testEmptyBracketsCompletionWithWrongParamName() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(wrong=foo<*>)");
        assertClasspathCompletions();
    }

    @Test
    void testEmptyBracketsCompletionWithoutMandatoryResourcesParam() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(<*>)");
        assertClasspathCompletions();
    }

    @Test
    void testEmptyStringLiteralCompletionWithParamName() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=\"<*>\")");

        assertClasspathCompletions(
                "@ConditionalOnResource(resources=\"classpath:a-random-resource-root.md<*>\")",
                "@ConditionalOnResource(resources=\"classpath:org/random-resource-org.md<*>\")",
                "@ConditionalOnResource(resources=\"classpath:org/test/foo<*>\")",
                "@ConditionalOnResource(resources=\"classpath:org/test/random-resource-org-test.txt<*>\")");
    }

    @Test
    void testEmptyStringLiteralCompletionWithoutMandatoryResourcesParam() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(\"<*>\")");

        assertClasspathCompletions();
    }

    @Test
    void testComplexResourceNameInPrefixWithParamNameCompletion() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=root.md<*>)");

        assertClasspathCompletions(
                "@ConditionalOnResource(resources=\"classpath:a-random-resource-root.md\"<*>)");
    }

    @Test
    void testComplexResourceNameInPrefixWithWrongExtensionAndParamNameCompletion() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=root.xml<*>)");

        assertClasspathCompletions();
    }

    @Test
    void testComplexResourceNameInPrefixWithinQuotesAndParamNameCompletion() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=\"root.md<*>\")");

        assertClasspathCompletions(
                "@ConditionalOnResource(resources=\"classpath:a-random-resource-root.md<*>\")");
    }

    @Test
    void testComplexResourceNameWithSlashPrefixAndWithResourcesAndParamNameCompletion() throws Exception {
        prepareCase("@ConditionalOnResource(resources=\"onClass\")", "@ConditionalOnResource(resources=\"/root.md<*>\")");

        assertClasspathCompletions();
    }





    private void prepareCase(String selectedAnnotation, String annotationStatementBeforeTest) throws Exception {
        InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-annotation-conditionalonresource/src/main/java/org/test/TestConditionalOnResourceCompletion.java");
        String content = IOUtils.toString(resource, Charset.defaultCharset());

        content = content.replace(selectedAnnotation, annotationStatementBeforeTest);
        editor = new Editor(harness, content, LanguageId.JAVA);
    }

    private void assertClasspathCompletions(String... completedAnnotations) throws Exception {
        List<CompletionItem> completions = editor.getCompletions();

        List<CompletionItem> filteredCompletions = completions.stream()
                .filter(item -> item.getTextEdit().getLeft().getNewText().contains("classpath"))
                .sorted(new Comparator<CompletionItem>() {
                    @Override
                    public int compare(CompletionItem o1, CompletionItem o2) {
                        return o1.getLabel().compareTo(o2.getLabel());
                    }
                })
                .toList();

        assertEquals(completedAnnotations.length, filteredCompletions.size());

        for (int i = 0; i < completedAnnotations.length; i++) {
            CompletionItem completion = filteredCompletions.get(i);

            Editor clonedEditor = editor.clone();
            clonedEditor.apply(completion);

            String expected = completedAnnotations[i];
            if (!clonedEditor.getText().contains(expected)) {
                fail("Not found '" + expected +"' in \n" + clonedEditor.getText());
            }
        }
    }
}