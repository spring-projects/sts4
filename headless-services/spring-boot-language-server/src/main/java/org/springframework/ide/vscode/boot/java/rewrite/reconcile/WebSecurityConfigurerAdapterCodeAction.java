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
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import java.util.Collection;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class WebSecurityConfigurerAdapterCodeAction implements RecipeCodeActionDescriptor {
	
	private static final String ID = "org.openrewrite.java.spring.boot2.WebSecurityConfigurerAdapter";
	
    private static final String FQN_WEB_SECURITY_CONFIGURER_ADAPTER = "org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter";

	private static final String PROBLEM_LABEL = "Class extends 'WebSecurityConfigurerAdapter' which is removed in Spring-Security 6.x";

	private static final String FIX_LABEL = "Refactor class into a Configuration bean not extending 'WebSecurityConfigurerAdapter'";
	
	private static final String STUB_WEB_SECURITY_CONFIG_ADAPTER = """
			package org.springframework.security.config.annotation.web.configuration;
			
			import org.springframework.security.authentication.AuthenticationManager;
			import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
			import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
			import org.springframework.security.config.annotation.web.builders.HttpSecurity;
			import org.springframework.security.config.annotation.web.builders.WebSecurity;
			import org.springframework.security.core.userdetails.UserDetailsService;
			
			public abstract class WebSecurityConfigurerAdapter {
			
				public void init(WebSecurity web) throws Exception {}
			
				public AuthenticationManager authenticationManagerBean() throws Exception { return null; }
				
				public UserDetailsService userDetailsServiceBean() throws Exception { return null; }
				
				protected void configure(HttpSecurity http) throws Exception {}
				
				public void configure(WebSecurity web) throws Exception {}
				
				protected void configure(AuthenticationManagerBuilder auth) throws Exception {}
			}
			""";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.WEB_SECURITY_CONFIGURER_ADAPTER;
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaMarkerVisitor<>() {

			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				TypeTree superClass = c.getExtends();
				boolean isExtendingWebSecurityConfigurerAdapter = false;
				boolean isUnresolved = false;
				if (superClass != null) {
					if (superClass.getType() instanceof JavaType.Unknown) {
						String strType = superClass.printTrimmed(getCursor());
						isExtendingWebSecurityConfigurerAdapter = "WebSecurityConfigurerAdapter".equals(strType) || FQN_WEB_SECURITY_CONFIGURER_ADAPTER.equals(strType);
						isUnresolved = true;
					} else if (superClass.getType() instanceof JavaType.FullyQualified) {
						isExtendingWebSecurityConfigurerAdapter = FQN_WEB_SECURITY_CONFIGURER_ADAPTER.equals( ((JavaType.FullyQualified)superClass.getType()).getFullyQualifiedName());
					}
				}
				if (isExtendingWebSecurityConfigurerAdapter) {					
					if (isAnnotatedWith(c.getLeadingAnnotations(), Annotations.CONFIGURATION)) {
						String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
						String[] typeStubs = new String[0];
						if (isUnresolved) {
							typeStubs = new String[] { STUB_WEB_SECURITY_CONFIG_ADAPTER };
						}
						FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), ID).withLabel(PROBLEM_LABEL)
								.withFixes(
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.FILE))
												.withRecipeScope(RecipeScope.FILE)
												.withTypeStubs(typeStubs),
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.PROJECT))
												.withRecipeScope(RecipeScope.PROJECT)
												.withTypeStubs(typeStubs));
						c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(marker)));
					}
				}
				return c;
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-security-config");
		return version != null && version.compareTo(new Version(5, 7, 0, null)) >= 0 && version.compareTo(new Version(6, 1, 0, null)) < 0;
	}
	
	private static boolean isAnnotatedWith(Collection<J.Annotation> annotations, String annotationType) {
		return annotations.stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), annotationType));
	}

}
