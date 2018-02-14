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
package org.springframework.ide.vscode.boot.java.autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ComponentInjectionsHoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class AutowiredHoverProvider implements HoverProvider {
	
	private BootJavaLanguageServer server;
	
	public AutowiredHoverProvider(BootJavaLanguageServer server) {
		this.server = server;
	}

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			LiveBean definedBean = getDefinedBean(annotation);
			if (definedBean != null) {
				for (SpringBootApp app : runningApps) {
					try {
						List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(app, definedBean).collect(Collectors.toList());

						if (!relevantBeans.isEmpty()) {
							for (LiveBean bean : relevantBeans) {
								String[] dependencies = bean.getDependencies();
								if (dependencies != null && dependencies.length > 0) {
									Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
									return ImmutableList.of(hoverRange);
								}
							}
						}
					}
					catch (Exception e) {
						Log.log(e);
					}
				}
			}
		}
		catch (Exception e) {
			Log.log(e);
		}

		return null;
	}

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {

			StringBuilder hover = new StringBuilder();

			LiveBean definedBean = getDefinedBean(annotation);
			if (definedBean != null) {

				hover.append("**Injection report for " + LiveHoverUtils.showBean(definedBean) + "**\n\n");

				boolean hasInterestingApp = false;
				boolean hasAutowiring = false;

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
							hover.append("\n\n");
							hasAutowiring |= addAutomaticallyWired(hover, annotation, beans, bean, project);
						}
					}
				}
				if (hasInterestingApp && hasAutowiring) {
					return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
				}
			}
		}
		return null;
	}

	private LiveBean getDefinedBean(Annotation autowiredAnnotation) {
		TypeDeclaration declaringType = ASTUtils.findDeclaringType(autowiredAnnotation);
		if (declaringType != null) {
			for (Annotation annotation : ASTUtils.getAnnotations(declaringType)) {
				if (AnnotationHierarchies.isSubtypeOf(annotation, Annotations.COMPONENT)) {
					return ComponentInjectionsHoverProvider.getDefinedBeanForComponent(annotation);
				}
			}
			//TODO: handler below is an attempt to do something that may work in many cases, but is probably
			// missing logics for special cases where annotation attributes on the declaring type matter.
			ITypeBinding beanType = declaringType.resolveBinding();
			if (beanType!=null) {
				String beanTypeName = beanType.getName();
				if (StringUtil.hasText(beanTypeName)) {
					return LiveBean.builder()
							.id(Character.toLowerCase(beanTypeName.charAt(0)) + beanTypeName.substring(1))
							.type(beanTypeName)
							.build();
				}
			}
			return null;
		}
		return null;
	}

	private boolean addAutomaticallyWired(StringBuilder hover, Annotation annotation, LiveBeansModel beans, LiveBean bean, IJavaProject project) {
		boolean result = false;
		String[] dependencies = bean.getDependencies();

		if (dependencies != null && dependencies.length > 0) {
			result = true;
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
		return result;
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
