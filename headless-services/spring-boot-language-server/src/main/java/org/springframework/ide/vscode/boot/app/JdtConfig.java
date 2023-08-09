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
package org.springframework.ide.vscode.boot.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.reconcilers.AddConfigurationIfBeansPresentReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.AnnotationNodeReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.AutowiredFieldIntoConstructorParameterReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.BeanMethodNotPublicReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.Boot3NotSupportedTypeReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.ModulithTypeReferenceViolationReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NoAutowiredOnConstructorReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NoRepoAnnotationReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NoRequestMappingAnnotationReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.PreciseBeanTypeReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.UnnecessarySpringExtensionReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.WebSecurityConfigurerAdapterReconciler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration(proxyBeanMethods = false)
public class JdtConfig {
	
	@Bean AnnotationNodeReconciler annotationNodeReconciler(BootJavaConfig config) {
		return new AnnotationNodeReconciler(config);
	}
	
	@Bean BeanMethodNotPublicReconciler beanMethodNotPublicReconciler(SimpleLanguageServer server) {
		return new BeanMethodNotPublicReconciler(server.getQuickfixRegistry());
	}
	
	@Bean AddConfigurationIfBeansPresentReconciler addConfigurationIfBeansPresentReconciler(SimpleLanguageServer server) {
		return new AddConfigurationIfBeansPresentReconciler(server.getQuickfixRegistry());
	}
	
	@Bean AutowiredFieldIntoConstructorParameterReconciler autowiredFieldIntoConstructorParameterReconciler(SimpleLanguageServer server) {
		return new AutowiredFieldIntoConstructorParameterReconciler(server.getQuickfixRegistry());
	}
	
	@Bean Boot3NotSupportedTypeReconciler boot3NotSupportedTypeReconciler() {
		return new Boot3NotSupportedTypeReconciler();
	}
	
	@Bean NoAutowiredOnConstructorReconciler noAutowiredOnConstructorReconciler(SimpleLanguageServer server) {
		return new NoAutowiredOnConstructorReconciler(server.getQuickfixRegistry());
	}
	
	@Bean WebSecurityConfigurerAdapterReconciler webSecurityConfigurerAdapterReconciler(SimpleLanguageServer server) {
		return new WebSecurityConfigurerAdapterReconciler(server.getQuickfixRegistry());
	}
	
	@Bean PreciseBeanTypeReconciler preciseBeanTypeReconciler(SimpleLanguageServer server) {
		return new PreciseBeanTypeReconciler(server.getQuickfixRegistry());
	}
	
	@Bean NoRequestMappingAnnotationReconciler noRequestMappingAnnotationReconciler(SimpleLanguageServer server) {
		return new NoRequestMappingAnnotationReconciler(server.getQuickfixRegistry());
	}
	
	@Bean ModulithTypeReferenceViolationReconciler modulithTypeReferenceViolationReconciler() {
		return new ModulithTypeReferenceViolationReconciler();
	}
	
	@Bean NoRepoAnnotationReconciler noRepoAnnotationReconciler(SimpleLanguageServer server) {
		return new NoRepoAnnotationReconciler(server.getQuickfixRegistry());
	}
	
	@Bean UnnecessarySpringExtensionReconciler unnecessarySpringExtensionReconciler(SimpleLanguageServer server) {
		return new UnnecessarySpringExtensionReconciler(server.getQuickfixRegistry());
	}

}
