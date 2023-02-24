/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import java.util.Comparator;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

public class AddConfigurationAnnotationIfBeansPresent extends Recipe {

	private static final String CONFIGURATION_PACKAGE = "org.springframework.context.annotation";

	private static final String CONFIGURATION_SIMPLE_NAME = "Configuration";

	private static final String FQN_CONFIGURATION = CONFIGURATION_PACKAGE + "." + CONFIGURATION_SIMPLE_NAME;

	private static final String FQN_BEAN = "org.springframework.context.annotation.Bean";

	@Override
	public String getDisplayName() {
		return "Add missing '@Configuration' annotation";
	}

	@Override
	public String getDescription() {
		return "Class having `@Bean' annotation over any methods but missing '@Configuration' annotation over the declaring class would have '@Configuration' annotation added.";
	}
	
	@Override
	protected TreeVisitor<?, ExecutionContext> getApplicableTest() {
		return new UsesType<>(FQN_BEAN);
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<ExecutionContext>() {

			@Override
			public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext p) {
				J.ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				if (isApplicableClass(c, getCursor())) {
					c = addConfigurationAnnotation(c);
				}
				return c;
			}

			private J.ClassDeclaration addConfigurationAnnotation(J.ClassDeclaration c) {
				maybeAddImport(FQN_CONFIGURATION);
				JavaTemplate template = JavaTemplate.builder(() -> getCursor(), "@" + CONFIGURATION_SIMPLE_NAME)
						.imports(
								FQN_CONFIGURATION)
						.javaParser(() -> JavaParser.fromJavaVersion().dependsOn("package " + CONFIGURATION_PACKAGE
								+ "; public @interface " + CONFIGURATION_SIMPLE_NAME + " {}").build())
						.build();
				return c.withTemplate(template,
						c.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
			}

		};
	}

	public static boolean isApplicableClass(J.ClassDeclaration classDecl, Cursor cursor) {
		if (classDecl.getKind() == J.ClassDeclaration.Kind.Type.Class) {
			boolean isStatic = false;
			for (J.Modifier m : classDecl.getModifiers()) {
				if (m.getType() == J.Modifier.Type.Abstract) {
					return false;
				} else if (m.getType() == J.Modifier.Type.Static) {
					isStatic = true;
				}
			}

			if (!isStatic) {
				// no static keyword? check if it is top level class in the CU
				J.CompilationUnit cu = cursor.dropParentUntil(J.CompilationUnit.class::isInstance).getValue();
				if (!cu.getClasses().contains(classDecl)) {
					return false;
				}
			}

			// check if '@Configuration' is already over the class
			AnnotationMatcher matcher = new AnnotationMatcher("@" + FQN_CONFIGURATION, true);

			for (J.Annotation a : classDecl.getLeadingAnnotations()) {
				JavaType.FullyQualified aType = TypeUtils.asFullyQualified(a.getType());
				if (aType != null && matcher.matchesAnnotationOrMetaAnnotation(aType)) {
					// Found '@Configuration' annotation
					return false;
				}
			}
			// No '@Configuration' present. Check if any methods have '@Bean' annotation
			for (Statement s : classDecl.getBody().getStatements()) {
				if (s instanceof J.MethodDeclaration) {
					if (isBeanMethod((J.MethodDeclaration) s)) {
						return true;
					}
				}
			}

		}
		return false;
	}

	private static boolean isBeanMethod(J.MethodDeclaration methodDecl) {
		for (J.Modifier m : methodDecl.getModifiers()) {
			if (m.getType() == J.Modifier.Type.Abstract) {
				return false;
			} else if (m.getType() == J.Modifier.Type.Static) {
				return false;
			}
		}
		AnnotationMatcher beanAnnotationMatcher = new AnnotationMatcher("@" + FQN_BEAN, true);
		for (J.Annotation a : methodDecl.getLeadingAnnotations()) {
			JavaType.FullyQualified aType = TypeUtils.asFullyQualified(a.getType());
			if (aType != null && beanAnnotationMatcher.matchesAnnotationOrMetaAnnotation(aType)) {
				return true;
			}
		}
		return false;
	}

}
