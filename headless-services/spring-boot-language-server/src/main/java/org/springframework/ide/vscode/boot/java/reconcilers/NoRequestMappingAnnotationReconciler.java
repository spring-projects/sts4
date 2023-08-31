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
package org.springframework.ide.vscode.boot.java.reconcilers;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.openrewrite.java.spring.NoRequestMappingAnnotation;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class NoRequestMappingAnnotationReconciler implements JdtAstReconciler {
	
    private static final String LABEL = "Replace @RequestMapping with specific @GetMapping, @PostMapping etc.";
	private static final String ID = NoRequestMappingAnnotation.class.getName();
	
	private QuickfixRegistry registry;

	public NoRequestMappingAnnotationReconciler(QuickfixRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) throws RequiredCompleteAstException {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(MarkerAnnotation node) {
				processAnnotation(node);
				return false;
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				processAnnotation(node);
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				processAnnotation(node);
				return false;
			}
			
			private void processAnnotation(Annotation a) {
				if (a.getParent() instanceof MethodDeclaration && isRequestMappingAnnotation(cu, a)) {
					String uri = docUri.toASCIIString();
					ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), LABEL, a.getStartPosition(), a.getLength());
					RewriteQuickFixUtils.setRewriteFixes(registry, problem, List.of(
//						new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.NODE))
//    						.withRangeScope(RewriteQuickFixUtils.createOpenRewriteRange(cu, a))
//    						.withRecipeScope(RecipeScope.NODE),
        				new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
    						.withRecipeScope(RecipeScope.FILE),
            				new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
        						.withRecipeScope(RecipeScope.PROJECT)
        			));
					problemCollector.accept(problem);
				}
			}
			
		});
	}
	
	private static boolean isRequestMappingAnnotation(CompilationUnit cu, Annotation a) {
		// Consider only NormalAnnotation as we need to flag @RequestMapping with single method parameter value
		if (a.isNormalAnnotation()) {
			String typeName = a.getTypeName().getFullyQualifiedName();
			if (Annotations.SPRING_REQUEST_MAPPING.equals(typeName)) {
				return hasApplicableMethodParameter(a);
			} else if (typeName.endsWith("RequestMapping")) {
				ITypeBinding type = a.resolveTypeBinding();
				if (type != null && Annotations.SPRING_REQUEST_MAPPING.equals(type.getQualifiedName())) {
					return hasApplicableMethodParameter(a);
				}
			}
		}
		return false;
	}
	
	private static boolean hasApplicableMethodParameter(Annotation a) {
		if (a.isNormalAnnotation()) {
			for (Object o : ((NormalAnnotation) a).values()) {
				if (o instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) o;
					if ("method".equals(pair.getName().getIdentifier())) {
						if (pair.getValue() instanceof ArrayInitializer) {
							return ((ArrayInitializer) pair.getValue()).expressions().size() == 1;
						}
						return true;
					}
				}
			}
		}
		return false;
	}


	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_PRECISE_REQUEST_MAPPING;
	}

}
