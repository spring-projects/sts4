/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Optionals;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BeanInjectedIntoHoverProvider extends AbstractInjectedIntoHoverProvider {

	public BeanInjectedIntoHoverProvider(BootJavaLanguageServerComponents server) {
		super(server);
	}

	@Override
	protected LiveBean getDefinedBean(Annotation annotation) {
		MethodDeclaration beanMethod = ASTUtils.getAnnotatedMethod(annotation);
		if (beanMethod!=null) {
			Optional<String> beanId = getBeanId(annotation, beanMethod);
			if (beanId.isPresent()) {
				//TODO: we could try to be more precise here and determine the bean type from the
				//ITypeBinding beanType = null; //null means unknown
				// method signature, however, this will typically give us a more abstract type than
				// the actual bean type at runtime. So if we we do that we have to deal with that
				// somehow. Therefore, for the time being we leave the beanType as `unknown` and
				// so do not use the bean type in determining relevant beans.
				//		Type unresolvedBeanType = beanMethod.getReturnType2();
				//		if (unresolvedBeanType!=null) {
				//			beanType = unresolvedBeanType.resolveBinding();
				//		}
				return LiveBean.builder()
						.id(beanId.get())
						// .type(type)
						.build();
			}
		}
		return null;
	}

	private Optional<String> getBeanId(Annotation annotation, MethodDeclaration beanMethod) {
		//Note: must handle all these cases:
		//  @Bean
		//  @Bean("beanId")
		//  @Bean({"beanId", "alias1"})
		//  @Bean(value="beanId")
		//  @Bean(value={"beanId", "alias1"})
		//  @Bean(name="beanId", ...)
		//  @Bean(name={"beanId", "alias1"}, ...)
		return Optionals.tryInOrder(
				() -> ASTUtils.getAttribute(annotation, "value").flatMap(ASTUtils::getFirstString),
				() -> ASTUtils.getAttribute(annotation, "name").flatMap(ASTUtils::getFirstString),
				() -> Optional.ofNullable(beanMethod.getName().getIdentifier())
		);
	}

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	@Override
	public Collection<Range> getLiveHoverHints(TypeDeclaration typeDeclaration, TextDocument doc,
			SpringBootApp[] runningApps) {
		return null;
	}

}
