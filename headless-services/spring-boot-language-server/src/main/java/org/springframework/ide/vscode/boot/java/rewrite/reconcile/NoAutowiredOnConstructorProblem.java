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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.marker.Range;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class NoAutowiredOnConstructorProblem implements RecipeSpringJavaProblemDescriptor {

	private static final String ID = "org.openrewrite.java.spring.NoAutowiredOnConstructor";
	private static final String LABEL = "Remove Unnecessary @Autowired";

	@Override
	public String getRecipeId() {
		return ID;
	}

	@Override
	public String getLabel(RecipeScope s) {
		return LABEL;
	}

	@Override
	public RecipeScope[] getScopes() {
		return new RecipeScope[] { RecipeScope.NODE };
	}

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
                        		FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId(), getId())
    	                        	.withRecipeId(ID)
    	                        	.withScope(getCursor().firstEnclosing(ClassDeclaration.class).getMarkers().findFirst(Range.class).get());
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
