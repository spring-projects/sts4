/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.java.autowired.Constants;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.util.StringUtil;

public class ComponentInjectionsHoverProvider extends AbstractInjectedIntoHoverProvider {

	@Override protected void addAutomaticallyWiredContructor(StringBuilder hover, Annotation annotation, LiveBeansModel beans, LiveBean bean) {
		TypeDeclaration typeDecl = ASTUtils.findDeclaringType(annotation);
		if (typeDecl != null) {
			MethodDeclaration[] constructors = ASTUtils.findConstructors(typeDecl);

			if (constructors != null && constructors.length == 1 && !hasAutowiredAnnotation(constructors[0])) {
				String[] dependencies = bean.getDependencies();

				if (dependencies != null && dependencies.length > 0) {
					hover.append(LiveHoverUtils.showBean(bean) + " got autowired with:\n\n");

					boolean firstDependency = true;
					for (String injectedBean : dependencies) {
						if (!firstDependency) {
							hover.append("\n");
						}
						List<LiveBean> dependencyBeans = beans.getBeansOfName(injectedBean);
						for (LiveBean dependencyBean : dependencyBeans) {
							hover.append("- " + LiveHoverUtils.showBean(dependencyBean));
						}
						firstDependency = false;
					}
				}
			}
		}
	}

	private boolean hasAutowiredAnnotation(MethodDeclaration constructor) {
		List<?> modifiers = constructor.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof MarkerAnnotation) {
				ITypeBinding typeBinding = ((MarkerAnnotation) modifier).resolveTypeBinding();
				if (typeBinding != null && typeBinding.getQualifiedName().equals(Constants.SPRING_AUTOWIRED)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected LiveBean getDefinedBean(Annotation annotation) {
		TypeDeclaration declaringType = ASTUtils.getAnnotatedType(annotation);
		if (declaringType != null) {
			ITypeBinding beanType = declaringType.resolveBinding();
			if (beanType != null) {
				String id = getBeanId(annotation, beanType);
				if (StringUtil.hasText(id)) {
					return LiveBean.builder().id(id).type(beanType.getQualifiedName()).build();
				}
			}
		}
		return null;
	}

	private String getBeanId(Annotation annotation, ITypeBinding beanType) {
		return ASTUtils.getAttribute(annotation, "value").flatMap(ASTUtils::getFirstString)
		.orElseGet(() ->  {
			String typeName = beanType.getName();
			if (StringUtil.hasText(typeName)) {
				return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
			}
			return null;
		});
	}

}
