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

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class NoAutowiredOnConstructorProblem implements RecipeCodeActionDescriptor {

	private static final String ID = "org.openrewrite.java.spring.NoAutowiredOnConstructor";
	private static final String LABEL = "Remove Unnecessary @Autowired";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
                J.ClassDeclaration cd =  super.visitClassDeclaration(classDecl, context);

                int constructorCount = 0;
                for(Statement s : cd.getBody().getStatements()) {
                    if(isConstructor(s)) {
                        constructorCount++;
                        if(constructorCount > 1) {
                            return cd;
                        }
                    }
                }
                
                FullyQualified type = TypeUtils.asFullyQualified(classDecl.getType());
                if (type != null && isApplicableType(type)) {
                    return cd.withBody(cd.getBody().withStatements(
                            ListUtils.map(cd.getBody().getStatements(), s -> {
                                if(!isConstructor(s)) {
                                    return s;
                                }
                                MethodDeclaration constructor = (MethodDeclaration) s;
            					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
                        		FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId(), getId())
                        			.withFix(
                        					new FixDescriptor(ID, List.of(uri), LABEL)
                        						.withRecipeScope(RecipeScope.NODE)
                        						.withRangeScope(getCursor().firstEnclosing(ClassDeclaration.class).getMarkers().findFirst(Range.class).get())
                        			);
                                constructor = constructor.withLeadingAnnotations(ListUtils.map(constructor.getLeadingAnnotations(), a -> {
                                	if (TypeUtils.isOfClassType(a.getType(), Annotations.AUTOWIRED)) {
    									a = a.withMarkers(a.getMarkers().add(fixAssistMarker)); 
                                	}
                                	return a;
                                }));
                                return constructor;
                            })
                    ));
                }
                return cd;
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
	public Boot2JavaProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_AUTOWIRED_CONSTRUCTOR;
	}
	
    private static boolean isConstructor(Statement s) {
        return s instanceof J.MethodDeclaration && ((J.MethodDeclaration)s).isConstructor();
    }


}
