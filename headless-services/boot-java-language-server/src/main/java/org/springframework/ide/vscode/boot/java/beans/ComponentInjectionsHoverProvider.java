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
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.ide.vscode.boot.java.autowired.Constants;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ComponentInjectionsHoverProvider implements HoverProvider {

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		// Highlight if any running app contains an instance of this component
		try {
			if (runningApps.length > 0) {
				LiveBean definedBean = getDefinedBean(annotation);
				if (definedBean != null) {
					if (Stream.of(runningApps).anyMatch(app -> hasRelevantBeans(app, definedBean))) {
						Optional<Range> nameRange = ASTUtils.nameRange(doc, annotation);
						if (nameRange.isPresent()) {
							return ImmutableList.of(nameRange.get());
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return ImmutableList.of();
	}

	private boolean hasRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return findRelevantBeans(app, definedBean).findAny().isPresent();
	}

	private Stream<LiveBean> findRelevantBeans(SpringBootApp app, LiveBean definedBean) {
		return app.getBeans().getBeansOfName(definedBean.getId()).stream()
				.filter(bean -> definedBean.getType().equals(bean.getType()));
	}

	private LiveBean getDefinedBean(Annotation annotation) {
		ITypeBinding beanType = getAnnotatedType(annotation);
		if (beanType != null) {
			String id = getBeanId(annotation, beanType);
			if (StringUtil.hasText(id)) {
				return LiveBean.builder().id(id).type(beanType.getQualifiedName()).build();
			}
		}
		return null;
	}

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {

			LiveBean definedBean = getDefinedBean(annotation);
			if (definedBean != null) {
				StringBuilder hover = new StringBuilder();
				hover.append("**Injection report for " + showBean(definedBean) + "**\n\n");

				boolean hasInterestingApp = false;
				for (SpringBootApp app : runningApps) {
					LiveBeansModel beans = app.getBeans();
					List<LiveBean> relevantBeans = findRelevantBeans(app, definedBean).collect(Collectors.toList());

					if (!relevantBeans.isEmpty()) {
						if (!hasInterestingApp) {
							hasInterestingApp = true;
						} else {
							hover.append("\n\n");
						}
						hover.append(niceAppName(app) + ":");

						for (LiveBean bean : relevantBeans) {
							addInjectedInto(definedBean, hover, beans, bean);
							addAutomaticallyWired(hover, annotation, beans, bean);
						}
					}
				}
				if (hasInterestingApp) {
					System.out.println(hover);
					return CompletableFuture
							.completedFuture(new Hover(ImmutableList.of(Either.forLeft(hover.toString()))));
				}
			}
		}
		return null;
	}

	private void addInjectedInto(LiveBean definedBean, StringBuilder hover, LiveBeansModel beans, LiveBean bean) {
		hover.append("\n\n");
		List<LiveBean> dependers = beans.getBeansDependingOn(bean.getId());
		if (dependers.isEmpty()) {
			hover.append(showBean(bean) + " exists but is **Not injected anywhere**\n");
		} else {
			hover.append(showBean(definedBean) + " injected into:\n\n");
			boolean firstDependency = true;
			for (LiveBean dependingBean : dependers) {
				if (!firstDependency) {
					hover.append("\n");
				}
				hover.append("- " + showBean(dependingBean));
				firstDependency = false;
			}
		}
	}

	private void addAutomaticallyWired(StringBuilder hover, Annotation annotation, LiveBeansModel beans, LiveBean bean) {
		TypeDeclaration typeDecl = ASTUtils.findDeclaringType(annotation);
		if (typeDecl != null) {
			MethodDeclaration[] constructors = ASTUtils.findConstructors(typeDecl);

			if (constructors != null && constructors.length == 1 && !hasAutowiredAnnotation(constructors[0])) {
				String[] dependencies = bean.getDependencies();

				if (dependencies != null && dependencies.length > 0) {
					hover.append(showBean(bean) + " got autowired with:\n\n");

					boolean firstDependency = true;
					for (String injectedBean : dependencies) {
						if (!firstDependency) {
							hover.append("\n");
						}
						List<LiveBean> dependencyBeans = beans.getBeansOfName(injectedBean);
						for (LiveBean dependencyBean : dependencyBeans) {
							hover.append("- " + showBean(dependencyBean));
						}
						firstDependency = false;
					}
				}
			}
		}
	}

	private String getBeanId(Annotation annotation, ITypeBinding beanType) {
		Optional<String> explicitId = ASTUtils.getValueAttribute(annotation);
		if (explicitId.isPresent()) {
			return explicitId.get();
		}
		String typeName = beanType.getName();
		if (StringUtil.hasText(typeName)) {
			return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
		}
		return null;
	}

	private String showBean(LiveBean bean) {
		return "Bean [id: " + bean.getId() + ", type: `" + bean.getType() + "`]";
	}

	private ITypeBinding getAnnotatedType(Annotation annotation) {
		ASTNode parent = annotation.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) parent;
			return typeDecl.resolveBinding();
		}
		return null;
	}

	private String niceAppName(SpringBootApp app) {
		return "Process [PID=" + app.getProcessID() + ", name=`" + app.getProcessName() + "`]";
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

}
