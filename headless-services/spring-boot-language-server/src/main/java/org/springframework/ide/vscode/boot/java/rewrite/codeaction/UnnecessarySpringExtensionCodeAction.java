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
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.Arrays;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class UnnecessarySpringExtensionCodeAction implements RecipeCodeActionDescriptor {

	private static final String LABEL = "Remove unnecessary @SpringExtension";
	private static final String ID = "org.openrewrite.java.spring.boot2.UnnecessarySpringExtension";
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
    private static final AnnotationMatcher SPRING_EXTENSION_ANNOTATIN_MATCHER = new AnnotationMatcher("@org.junit.jupiter.api.extension.ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)");

	@Override
	public String getRecipeId() {
		return ID;
	}

	@Override
	public String getLabel(RecipeScope s) {
		return RecipeCodeActionDescriptor.buildLabel(LABEL, s);
	}

	@Override
	public RecipeScope[] getScopes() {
		return new RecipeScope[] { RecipeScope.PROJECT };
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor() {
		return new JavaIsoVisitor<>() {

			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				if (c.getLeadingAnnotations().stream().anyMatch(a -> {
					FullyQualified fq = TypeUtils.asFullyQualified(a.getType());
					return fq != null && SPRING_BOOT_TEST_ANNOTATIONS.contains(fq.getFullyQualifiedName());
				})) {
					Range range = c.getMarkers().findFirst(Range.class).get();
					c = c.withLeadingAnnotations(ListUtils.map(c.getLeadingAnnotations(), a -> {
						if (SPRING_EXTENSION_ANNOTATIN_MATCHER.matches(a)) {
							return a.withMarkers(a.getMarkers().add(new FixAssistMarker(Tree.randomId()).withRecipeId(ID).withScope(range)));
						}
						return a;
					}));
				}
				return c;
			}
			
		};
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 1, 0).test(project);
	}

}
