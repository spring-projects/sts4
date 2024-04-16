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
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.JdtSemanticTokensProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;

public class JdtDataQuerySemanticTokensProvider implements JdtSemanticTokensProvider {
	
	private static final String QUERY = "Query";
	private static final String FQN_QUERY = "org.springframework.data.jpa.repository." + QUERY;

	private final JpqlSemanticTokens jpqlProvider;
	private final HqlSemanticTokens hqlProvider;
	private final JpqlSupportState supportState;
	
	public JdtDataQuerySemanticTokensProvider(JpqlSemanticTokens jpqlProvider, HqlSemanticTokens hqlProvider, JpqlSupportState supportState) {
		this.jpqlProvider = jpqlProvider;
		this.hqlProvider = hqlProvider;
		this.supportState = supportState;
	}

	@Override
	public List<String> getTokenTypes() {
		return Stream.concat(jpqlProvider.getTokenTypes().stream(), hqlProvider.getTokenTypes().stream()).distinct().collect(Collectors.toList());
	}

	@Override
	public List<String> getTokenModifiers() {
		return Stream.concat(jpqlProvider.getTypeModifiers().stream(), hqlProvider.getTypeModifiers().stream()).distinct().collect(Collectors.toList());
	}

	@Override
	public List<SemanticTokenData> computeTokens(IJavaProject jp, CompilationUnit cu) {
		List<SemanticTokenData> tokensData = new ArrayList<>();
		
		SemanticTokensDataProvider provider = SpringProjectUtil.hasDependencyStartingWith(jp, "hibernate-core", null) ? hqlProvider : jpqlProvider;
		
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation a) {
				if (isQueryAnnotation(a)) {
					Expression queryValueNode = ((List<?>) a.values()).stream()
							.filter(MemberValuePair.class::isInstance)
							.map(MemberValuePair.class::cast)
							.filter(p -> "value".equals(p.getName().getIdentifier()))
							.findFirst().map(p -> p.getValue())
							.get();
					if (queryValueNode instanceof StringLiteral) {
						IAnnotationBinding annotationBinding = a.resolveAnnotationBinding();
						String query = getJpaQuery(annotationBinding);
						if (query != null && !query.isBlank()) {
							int valueOffset = queryValueNode.getStartPosition() + 1;
							tokensData.addAll(provider.computeTokens(query, valueOffset));
						}
					}
				}
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation a) {
				if (isQueryAnnotation(a) && a.getValue() instanceof StringLiteral) {
					IAnnotationBinding annotationBinding = a.resolveAnnotationBinding();
					String query = getJpaQuery(annotationBinding);
					if (query != null && !query.isBlank()) {
						int valueOffset = a.getValue().getStartPosition() + 1;
						tokensData.addAll(provider.computeTokens(query, valueOffset));
					}
				}
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				if ("createQuery".equals(node.getName().getIdentifier()) && node.arguments().size() <= 2 && node.arguments().get(0) instanceof StringLiteral queryExpr) {
					IMethodBinding methodBinding = node.resolveMethodBinding();
					if ("jakarta.persistence.EntityManager".equals(methodBinding.getDeclaringClass().getQualifiedName())) {
						if (methodBinding.getParameterTypes().length <= 2 && "java.lang.String".equals(methodBinding.getParameterTypes()[0].getQualifiedName())) {
							tokensData.addAll(provider.computeTokens(queryExpr.getLiteralValue(), queryExpr.getStartPosition() + 1));
						}
					}
				}
				return super.visit(node);
			}
			
		});
		
		return tokensData;
	}
	
	private static boolean isQueryAnnotation(Annotation a) {
		return FQN_QUERY.equals(a.getTypeName().getFullyQualifiedName())
				|| QUERY.equals(a.getTypeName().getFullyQualifiedName());
	}
	
	private static String getJpaQuery(IAnnotationBinding annotationBinding) {
		if (annotationBinding != null && annotationBinding.getAnnotationType() != null) {
			if (FQN_QUERY.equals(annotationBinding.getAnnotationType().getQualifiedName())) {
				String query = null;
				boolean isNative = false;
				for (IMemberValuePairBinding pair : annotationBinding.getAllMemberValuePairs()) {
					switch (pair.getName()) {
					case "value":
						query = (String) pair.getValue();
						break;
					case "nativeQuery":
						isNative = (Boolean) pair.getValue();
						break;
					default:
					}
				}
				return isNative ? null : query;
			}
		}
		return null;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return supportState.isEnabled() && SpringProjectUtil.hasDependencyStartingWith(project, "spring-data-jpa", null);
	}

}
