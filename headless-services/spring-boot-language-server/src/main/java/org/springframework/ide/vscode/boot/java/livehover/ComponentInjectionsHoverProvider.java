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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ComponentInjectionsHoverProvider extends AbstractInjectedIntoHoverProvider {

	private static Logger LOG = LoggerFactory.getLogger(ComponentInjectionsHoverProvider.class);

	public ComponentInjectionsHoverProvider(BootJavaLanguageServerComponents server) {
		super(server);
	}

	@Override
	protected LiveBean getDefinedBean(Annotation annotation) {
		return getDefinedBeanForComponent(annotation);
	}

	public static LiveBean getDefinedBeanForComponent(Annotation annotation) {
		//Move to ASTUtils?
		TypeDeclaration declaringType = ASTUtils.getAnnotatedType(annotation);
		return getDefinedBeanForType(declaringType, annotation);
	}

	private static LiveBean getDefinedBeanForType(TypeDeclaration declaringType, Annotation annotation) {
		if (declaringType != null) {
			ITypeBinding beanType = declaringType.resolveBinding();
			if (beanType != null) {
				String id = getBeanId(annotation, beanType);
				if (StringUtil.hasText(id)) {
					return LiveBean.builder().id(id).type(getBeanType(beanType).toString()).build();
				}
			}
		}
		return null;
	}

	private static String getBeanId(Annotation annotation, ITypeBinding beanType) {
		return ASTUtils.getAttribute(annotation, "value").flatMap(ASTUtils::getFirstString)
		.orElseGet(() ->  {
			String typeName = beanType.getName();

			ITypeBinding declaringClass = beanType.getDeclaringClass();
			while (declaringClass != null) {
				typeName = declaringClass.getName() + "." + typeName;
				declaringClass = declaringClass.getDeclaringClass();
			}

			if (StringUtil.hasText(typeName)) {
				return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
			}
			return null;
		});
	}

	private static StringBuilder getBeanType(ITypeBinding beanType) {
		ITypeBinding declaringClass = beanType.getDeclaringClass();
		if (declaringClass == null) {
			return new StringBuilder(beanType.getQualifiedName());
		} else {
			StringBuilder sb = getBeanType(declaringClass);
			sb.append('$');
			sb.append(beanType.getName());
			return sb;
		}
	}

	@Override
	public Collection<Range> getLiveHoverHints(IJavaProject project, TypeDeclaration typeDeclaration, TextDocument doc,
			SpringBootApp[] runningApps) {
		if (runningApps.length > 0 && !isComponentAnnotatedType(typeDeclaration)) {
			try {
				LiveBean definedBean = getDefinedBeanForType(typeDeclaration, null);
				if (definedBean != null) {
					if (Stream.of(runningApps).anyMatch(app -> LiveHoverUtils.hasRelevantBeans(app, definedBean))) {
						Optional<Range> nameRange = Optional.of(ASTUtils.nodeRegion(doc, typeDeclaration.getName()).asRange());
						if (nameRange.isPresent()) {
							return ImmutableList.of(nameRange.get());
						}
					}
				}
			} catch (Exception e) {
				LOG.error("", e);
			}
		}
		return ImmutableList.of();
	}

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {

		if (runningApps.length > 0 && !isComponentAnnotatedType(typeDeclaration)) {

			LiveBean definedBean = getDefinedBeanForType(typeDeclaration, null);
			if (definedBean != null) {
				return assembleHover(project, runningApps, definedBean);
			}
		}
		return null;
	}

	private boolean isComponentAnnotatedType(TypeDeclaration typeDeclaration) {
		List<?> modifiers = typeDeclaration.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				ITypeBinding typeBinding = ((Annotation) modifier).resolveTypeBinding();
				return isComponentAnnotation(typeBinding);
			}
		}
		return false;
	}

	private boolean isComponentAnnotation(ITypeBinding type) {
		Set<String> transitiveSuperAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(type);
		for (String annotationType : transitiveSuperAnnotations) {
			if (Annotations.COMPONENT.equals(annotationType)) {
				return true;
			}
		}

		return false;
	}

}
