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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ComponentInjectionsHoverProvider extends AbstractInjectedIntoHoverProvider {

	public ComponentInjectionsHoverProvider(BootJavaLanguageServerComponents server) {
		super(server);
	}

	@Override
	protected void addAutomaticallyWiredContructor(StringBuilder hover, Annotation annotation, LiveBeansModel beans, LiveBean bean, IJavaProject project) {
		TypeDeclaration typeDecl = ASTUtils.findDeclaringType(annotation);
		if (typeDecl != null) {
			MethodDeclaration[] constructors = ASTUtils.findConstructors(typeDecl);

			if (constructors != null && constructors.length == 1 && !hasAutowiredAnnotation(constructors[0])) {
				String[] dependencies = bean.getDependencies();

				if (dependencies != null && dependencies.length > 0) {
					hover.append("\n\n");
					hover.append(LiveHoverUtils.showBean(bean) + " got autowired with:\n\n");

					boolean firstDependency = true;
					for (String injectedBean : dependencies) {
						if (!firstDependency) {
							hover.append("\n");
						}
						List<LiveBean> dependencyBeans = beans.getBeansOfName(injectedBean);
						for (LiveBean dependencyBean : dependencyBeans) {
							hover.append("- " + LiveHoverUtils.showBeanWithResource(server, dependencyBean, "  ", project));
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
				if (typeBinding != null && typeBinding.getQualifiedName().equals(Annotations.AUTOWIRED)) {
					return true;
				}
			}
		}
		return false;
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
					return LiveBean.builder().id(id).type(beanType.getQualifiedName()).build();
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

	@Override
	public Collection<Range> getLiveHoverHints(TypeDeclaration typeDeclaration, TextDocument doc,
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
				Log.log(e);
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
				StringBuilder hover = new StringBuilder();
				hover.append("**Injection report for " + LiveHoverUtils.showBean(definedBean) + "**\n\n");

				boolean hasInterestingApp = false;
				for (SpringBootApp app : runningApps) {
					LiveBeansModel beans = app.getBeans();
					List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(app, definedBean).collect(Collectors.toList());

					if (!relevantBeans.isEmpty()) {
						if (!hasInterestingApp) {
							hasInterestingApp = true;
						} else {
							hover.append("\n\n");
						}
						hover.append(LiveHoverUtils.niceAppName(app) + ":");

						for (LiveBean bean : relevantBeans) {
							addInjectedInto(definedBean, hover, beans, bean, project);
						}
					}
				}
				if (hasInterestingApp) {
					return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
				}
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
