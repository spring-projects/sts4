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
package org.springframework.ide.vscode.boot.java.annotations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public class AnnotationHierarchiesTests {
	
	private List<Path> createdFiles = new ArrayList<>();
	
	private Path createFile(String projectName, String packageName, String name, String content) throws Exception {
		Path projectPath = Paths.get(getClass().getResource("/test-projects/" + projectName).toURI());
		Path filePath = projectPath.resolve("src/main/java").resolve(packageName.replace('.', '/')).resolve(name);
		Files.createDirectories(filePath.getParent());
		createdFiles.add(Files.createFile(filePath));
		Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
		return filePath;
	}
	
	private void clearTestFiles() {
		for (Iterator<Path> itr = createdFiles.iterator(); itr.hasNext();) {
			Path path = itr.next();
			try {
				Files.delete(path);
				itr.remove();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@AfterEach
	void tearDown() {
		clearTestFiles();
	}
	
	@Test
	void circularAnnotations() throws Exception {
		String projectName = "test-spring-validations";
		IJavaProject project = ProjectsHarness.INSTANCE.mavenProject(projectName);
		createFile(projectName, "test", "CustomComponent1.java", """
		package test;
		
		import org.springframework.stereotype.Component;
		
		@Component
		@CustomComponent2
		public @interface CustomComponent1 {
		
		}
		""");
		createFile(projectName, "test", "CustomComponent2.java", """
		package test;
		
		import org.springframework.stereotype.Component;
		
		@Component
		@CustomComponent1
		public @interface CustomComponent2 {
		
		}
		""");
		Path file = createFile(projectName, "test", "MyComponent.java", """
		package test;
		
		@CustomComponent1
		public class MyComponent {
		
		}
		""");
		
		SpringIndexerJava.createParser(project, true).createASTs(new String[] { file.toFile().toString() }, null, new String[0], new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				cu.accept(new ASTVisitor() {

					@Override
					public boolean visit(MarkerAnnotation node) {
						ITypeBinding binding = node.resolveTypeBinding();
						assertThat(binding).isNotNull();
						assertThat(binding.getQualifiedName()).isEqualTo("test.CustomComponent1");
						
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "test.CustomComponent2")).isTrue();
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "org.springframework.context.annotation.Configuration")).isFalse();
						assertThat(AnnotationHierarchies.getMetaAnnotations(binding, qn -> true).stream().toList().size()).isEqualTo(3);
						assertThat(AnnotationHierarchies.isMetaAnnotation(binding, "org.springframework.context.annotation.Configuration"::equals)).isFalse();
						assertThat(AnnotationHierarchies.getDirectSuperAnnotations(binding).stream().toList().size()).isEqualTo(2);
						
						IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
						assertThat(annotationBinding).isNotNull();
						assertThat(AnnotationHierarchies.getDirectSuperAnnotationBindings(annotationBinding).stream().toList().size()).isEqualTo(2);
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "test.CustomComponent2")).isTrue();
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "org.springframework.stereotype.Component")).isTrue();
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "org.springframework.context.annotation.Configuration")).isFalse();
						return super.visit(node);
					}
					
				});
			}	
		}, null);

	}
	
	@Test
	void simpleHierarchy() throws Exception {
		String projectName = "test-spring-validations";
		IJavaProject project = ProjectsHarness.INSTANCE.mavenProject(projectName);
		Path file = createFile(projectName, "test", "MyComponent.java", """
		package test;
		
		import org.springframework.boot.autoconfigure.SpringBootApplication
		
		@SpringBootApplication
		public class MyComponent {
		
		}
		""");
		
		SpringIndexerJava.createParser(project, true).createASTs(new String[] { file.toFile().toString() }, null, new String[0], new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				cu.accept(new ASTVisitor() {

					@Override
					public boolean visit(MarkerAnnotation node) {
						ITypeBinding binding = node.resolveTypeBinding();
						assertThat(binding).isNotNull();
						assertThat(binding.getQualifiedName()).isEqualTo("org.springframework.boot.autoconfigure.SpringBootApplication");
						
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "test.CustomComponent2")).isFalse();
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "org.springframework.context.annotation.Configuration")).isTrue();
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "org.springframework.boot.autoconfigure.SpringBootApplication")).isTrue();
						assertThat(AnnotationHierarchies.hasTransitiveSuperAnnotationType(binding, "org.springframework.stereotype.Component")).isTrue();
						List<ITypeBinding> metaAnnotations = AnnotationHierarchies.getMetaAnnotations(binding, qn -> true).stream().toList();
						assertThat(metaAnnotations.size()).isEqualTo(8);
						assertThat(AnnotationHierarchies.isMetaAnnotation(binding, "org.springframework.context.annotation.Configuration"::equals)).isTrue();
						assertThat(AnnotationHierarchies.getDirectSuperAnnotations(binding).stream().toList().size()).isEqualTo(3);
						
						IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
						assertThat(annotationBinding).isNotNull();
						assertThat(AnnotationHierarchies.getDirectSuperAnnotationBindings(annotationBinding).stream().toList().size()).isEqualTo(3);
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "test.CustomComponent2")).isFalse();
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "org.springframework.context.annotation.Configuration")).isTrue();
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "org.springframework.boot.autoconfigure.SpringBootApplication")).isTrue();
						assertThat(AnnotationHierarchies.isSubtypeOf(node, "org.springframework.stereotype.Component")).isTrue();
						return super.visit(node);
					}
					
				});
			}	
		}, null);

	}

}
