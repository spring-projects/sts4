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
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.springframework.ide.vscode.boot.java.SpringJavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class UnnecessarySpringExtensionReconciler implements AnnotationReconciler {
	
    private static final List<String> SPRING_BOOT_TEST_ANNOTATIONS = Arrays.asList(
            "org.springframework.boot.test.context.SpringBootTest",
            "org.springframework.boot.test.autoconfigure.jdbc.JdbcTest",
            "org.springframework.boot.test.autoconfigure.web.client.RestClientTest",
            "org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest",
            "org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest",
            "org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest",
            "org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest",
            "org.springframework.boot.test.autoconfigure.jooq.JooqTest",
            "org.springframework.boot.test.autoconfigure.json.JsonTest",
            "org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest",
            "org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest",
            "org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest",
            "org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest",
            "org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest",
            "org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest",
            "org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest"
    	);
    
    private static final String EXTEND_WITH_ANNOTATION = "org.junit.jupiter.api.extension.ExtendWith";
    
    private static final String SPRING_EXTENSION_ANNOTATION = "org.springframework.test.context.junit.jupiter.SpringExtension";
    
	@Override
	public void visit(IJavaProject project, IDocument doc, Annotation node, ITypeBinding typeBinding,
			IProblemCollector problemCollector) {
		if (isUnnecessarySpringExtensionAnnotation(project, node, typeBinding)) {
			ReconcileProblemImpl problem = new ReconcileProblemImpl(
					SpringJavaProblemType.JAVA_TEST_SPRING_EXTENSION, "Unnecessary @SpringExtension",
					node.getStartPosition(), node.getLength());
			problemCollector.accept(problem);
		}
	}
	
	public static boolean isUnnecessarySpringExtensionAnnotation(IJavaProject project, Annotation node, ITypeBinding typeBinding) {
		if (EXTEND_WITH_ANNOTATION.equals(typeBinding.getQualifiedName())) {
			Version v = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
			// Since Boot 2.1
			if ((v.getMajor() == 2 && v.getMinor() >= 1) || v.getMajor() > 2) {
				return hasSpringTestSiblingAnnotation(node) && hasSpringExtensionAnnotationParameter(node);
			}
		}
		return false;
	}
	
	private static boolean hasSpringTestSiblingAnnotation(Annotation node) {
		if (node.getParent() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = ((TypeDeclaration) node.getParent());
			for (Object m : typeDecl.modifiers()) {
				if (m instanceof Annotation) {
					IAnnotationBinding annotationBinding = ((Annotation)m).resolveAnnotationBinding();
					if (annotationBinding != null) {
						ITypeBinding annotationType = annotationBinding.getAnnotationType();
						if (annotationType != null && SPRING_BOOT_TEST_ANNOTATIONS.contains(annotationBinding.getAnnotationType().getQualifiedName())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean hasSpringExtensionAnnotationParameter(Annotation node) {
		if (node instanceof SingleMemberAnnotation) {
			return isSpringExtensionExpression(((SingleMemberAnnotation) node).getValue());
		} else if (node instanceof NormalAnnotation) {
			List<MemberValuePair> params = ((NormalAnnotation) node).values();
			for (MemberValuePair param : params) {
				if ("value".equals(param.getName().getIdentifier())) {
					return isSpringExtensionExpression(param.getValue());
				}
			}
		}
		return false;
	}
	
	private static boolean isSpringExtensionExpression(Expression o) {
		if (o instanceof TypeLiteral) {
			ITypeBinding binding = ((TypeLiteral) o).getType().resolveBinding();
			return binding != null && SPRING_EXTENSION_ANNOTATION.equals(binding.getQualifiedName());
		}
		return false;
	}

}
