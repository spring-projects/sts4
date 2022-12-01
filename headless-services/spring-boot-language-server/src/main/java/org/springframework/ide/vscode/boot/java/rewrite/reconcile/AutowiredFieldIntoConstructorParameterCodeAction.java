/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.spring.AutowiredFieldIntoConstructorParameterVisitor;
import org.openrewrite.java.tree.J.Block;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.ORAstUtils;
import org.springframework.ide.vscode.commons.rewrite.maven.MavenProjectParser;

public class AutowiredFieldIntoConstructorParameterCodeAction implements RecipeCodeActionDescriptor {
	
	private static final String LABEL = "Convert @Autowired field into Constructor Parameter";
	private static final String ID = "org.springframework.ide.vscode.commons.rewrite.java.ConvertAutowiredFieldIntoConstructorParameter";
	private static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<>() {

			@Override
			public CompilationUnit visitCompilationUnit(CompilationUnit cu, ExecutionContext p) {
				JavaSourceSet sourceSet = cu.getMarkers().findFirst(JavaSourceSet.class).orElse(null);
				if (sourceSet != null && MavenProjectParser.TEST.equals(sourceSet.getName())) {
					return cu;
				}
				return super.visitCompilationUnit(cu, p);
			}

			@Override
			public VariableDeclarations visitVariableDeclarations(VariableDeclarations multiVariable,
					ExecutionContext p) {
				VariableDeclarations m = super.visitVariableDeclarations(multiVariable, p);
				Cursor blockCursor = getCursor().dropParentUntil(Block.class::isInstance);
				if (multiVariable.getVariables().size() == 1
						&& multiVariable.getLeadingAnnotations().stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), AUTOWIRED))
						&& blockCursor.getParent().getValue() instanceof ClassDeclaration) {
					ClassDeclaration classDeclaration = (ClassDeclaration) blockCursor.getParent().getValue();
					FullyQualified fqType = TypeUtils.asFullyQualified(classDeclaration.getType());
					if (fqType != null && isApplicableType(fqType)) {
						List<MethodDeclaration> constructors = ORAstUtils.getMethods(classDeclaration).stream().filter(c -> c.isConstructor()).limit(2).collect(Collectors.toList());
						String fieldName = multiVariable.getVariables().get(0).getSimpleName();
						String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toString();
						FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId())
								.withFix(
									new FixDescriptor(ID, List.of(uri), LABEL)
										.withRangeScope(classDeclaration.getMarkers().findFirst(Range.class).get())
										.withParameters(Map.of("classFqName", fqType.getFullyQualifiedName(), "fieldName", fieldName))
										.withRecipeScope(RecipeScope.NODE)
								);
						if (constructors.size() == 0) {
							m = m.withMarkers(m.getMarkers().add(marker));							
						} else if (constructors.size() == 1 && AutowiredFieldIntoConstructorParameterVisitor.isNotConstructorInitializingField(constructors.get(0), fieldName)) {
							m = m.withMarkers(m.getMarkers().add(marker));							
						} else {
			                List<MethodDeclaration> autowiredConstructors = constructors.stream().filter(constr -> constr.getLeadingAnnotations().stream()
	                                .map(a -> TypeUtils.asFullyQualified(a.getType()))
	                                .filter(Objects::nonNull)
	                                .map(FullyQualified::getFullyQualifiedName)
	                                .anyMatch(AUTOWIRED::equals)
	                        )
	                        .limit(2)
	                        .collect(Collectors.toList());
			                if (autowiredConstructors.size() == 1 && AutowiredFieldIntoConstructorParameterVisitor.isNotConstructorInitializingField(autowiredConstructors.get(0), fieldName)) {
								m = m.withMarkers(m.getMarkers().add(marker));							
			                }
						}
					}
				}
				return m;
			}

			private boolean isApplicableType(FullyQualified type) {
				return !AnnotationHierarchies
						.getTransitiveSuperAnnotations(type, fq -> fq.getFullyQualifiedName().startsWith("java."))
						.contains("org.springframework.boot.test.context.SpringBootTest");
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_CONSTRUCTOR_PARAMETER_INJECTION;
	}

}
