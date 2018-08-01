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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ComponentInjectionsHoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 * @author Alex Boyko
 */
public class AutowiredHoverProvider implements HoverProvider {

	final static Logger log = LoggerFactory.getLogger(AutowiredHoverProvider.class);

	private static final int MAX_INLINE_BEANS_STRING_LENGTH = 60;
	private static final String INLINE_BEANS_STRING_SEPARATOR = " ";

	private BootJavaLanguageServerComponents server;

	public AutowiredHoverProvider(BootJavaLanguageServerComponents server) {
		this.server = server;
	}

	@Override
	public Collection<Range> getLiveHoverHints(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {
			LiveBean definedBean = getDefinedBeanForTypeDeclaration(ASTUtils.findDeclaringType(annotation));
			// Annotation is MarkerNode, parent is some field, method, variable declaration node.
			ASTNode declarationNode = annotation.getParent();
			try {
				Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
				return getLiveHoverHints(project, declarationNode, hoverRange, runningApps, definedBean);
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		return null;
	}

	private Collection<Range> getLiveHoverHints(IJavaProject project, ASTNode declarationNode, Range range,
			SpringBootApp[] runningApps, LiveBean definedBean) {
		if (declarationNode != null && definedBean != null) {
			for (SpringBootApp app : runningApps) {
				List<LiveBean> relevantBeans = getRelevantAutowiredBeans(project, declarationNode, app, definedBean);
				if (!relevantBeans.isEmpty()) {
					return ImmutableList.of(range);
				}
			}
		}
		return null;
	}

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {
			LiveBean definedBean = getDefinedBeanForTypeDeclaration(ASTUtils.findDeclaringType(annotation));
			// Annotation is MarkerNode, parent is some field, method, variable declaration node.
			ASTNode declarationNode = annotation.getParent();
			return provideHover(definedBean, declarationNode, offset, doc, project, runningApps);
		}
		return null;
	}

	private Hover provideHover(LiveBean definedBean, ASTNode declarationNode, int offset, TextDocument doc,
			IJavaProject project, SpringBootApp[] runningApps) {
		if (definedBean != null) {

			StringBuilder hover = new StringBuilder();

			boolean hasContent = false;

			for (SpringBootApp app : runningApps) {

				List<LiveBean> autowiredBeans = getRelevantAutowiredBeans(project, declarationNode, app, definedBean);

				if (!autowiredBeans.isEmpty()) {
					if (!hasContent) {
						hasContent = true;
					} else {
						hover.append("  \n  \n");
					}
					hover.append("**Autowired `");
					hover.append(definedBean.getId());
					hover.append("` &rarr; ");
					if (LiveHoverUtils.doBeansFitInline(autowiredBeans, MAX_INLINE_BEANS_STRING_LENGTH - definedBean.getId().length(),
							INLINE_BEANS_STRING_SEPARATOR)) {
						hover.append(autowiredBeans.stream().map(b -> LiveHoverUtils.showBeanInline(server, project, b))
								.collect(Collectors.joining(INLINE_BEANS_STRING_SEPARATOR)));
						hover.append("**\n");
					} else {
						hover.append(autowiredBeans.size());
						hover.append(" bean");
						if (autowiredBeans.size() > 1) {
							hover.append('s');
						}
						hover.append("**\n");
					}
//								if (autowiredBeans.size() == 1) {
//									hover.append(LiveHoverUtils.showBeanIdAndTypeInline(server, project, autowiredBeans.get(0)));
//								} else {
//									hover.append(autowiredBeans.size());
//									hover.append(" beans**\n");
//								}
					hover.append(autowiredBeans.stream()
							.map(b -> "- " + LiveHoverUtils.showBeanWithResource(server, b, "  ", project))
							.collect(Collectors.joining("\n")));
					hover.append("\n  \n");
					hover.append(LiveHoverUtils.niceAppName(app));
				}

			}
			if (hasContent) {
				return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
			}
		}
		return null;
	}

	private List<LiveBean> getRelevantAutowiredBeans(IJavaProject project, ASTNode declarationNode, SpringBootApp app, LiveBean definedBean) {
		LiveBeansModel beans = app.getBeans();
		List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(app, definedBean);

		if (!relevantBeans.isEmpty()) {
			List<LiveBean> allDependencyBeans = relevantBeans.stream()
					.flatMap(b -> Arrays.stream(b.getDependencies())).distinct()
					.flatMap(d -> beans.getBeansOfName(d).stream()).collect(Collectors.toList());

			if (!allDependencyBeans.isEmpty()) {

				List<LiveBean> autowiredBeans = findAutowiredBeans(project, declarationNode,
						allDependencyBeans);
				if (autowiredBeans.isEmpty()) {
					// Show all relevant dependency beans
					autowiredBeans = allDependencyBeans;
				} else {
					return autowiredBeans;
				}
			}
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	private List<LiveBean> findAutowiredBeans(IJavaProject project, ASTNode declarationNode, Collection<LiveBean> beans) {
		if (declarationNode instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration)declarationNode;
			return ((List<Object>)methodDeclaration.parameters()).stream()
					.filter(p -> p instanceof SingleVariableDeclaration)
					.map(p -> (SingleVariableDeclaration)p)
					.flatMap(p -> findAutowiredBeans(project, p, beans).stream())
					.collect(Collectors.toList());
		} else if (declarationNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration)declarationNode;
			return matchBeans(project, beans, fieldDeclaration.getType().resolveBinding());
		} else if (declarationNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)declarationNode;
			return matchBeans(project, beans, singleVariableDeclaration.getType().resolveBinding());
		}
		return Collections.emptyList();
	}

	private List<LiveBean> matchBeans(IJavaProject project, Collection<LiveBean> beans, ITypeBinding type) {
		List<LiveBean> relevant = Collections.emptyList();
		if (type != null) {
			String fqName = type.getQualifiedName();
			if (fqName != null) {
				relevant = matchBeans(project, beans, fqName, true);
				if (relevant.isEmpty()) {
					IType indexType = project.findType(fqName);
					if (indexType != null) {
						relevant = project.allSubtypesOf(indexType)
							.map(subType -> matchBeans(project, beans, subType.getFullyQualifiedName(), false))
							.filter(relevantBeans -> !relevantBeans.isEmpty())
							.blockFirst();
						if (relevant == null) {
							relevant = Collections.emptyList();
						}
					}
				}
			}
		}
		return relevant;
	}

	private List<LiveBean> matchBeans(IJavaProject project, Collection<LiveBean> beans, String fqName, boolean allDots) {
		if (fqName != null) {
			if (allDots) {
				return beans.stream().filter(b -> fqName.equals(b.getType(true).replace('$', '.'))).collect(Collectors.toList());
			} else {
				return beans.stream().filter(b -> fqName.equals(b.getType(true))).collect(Collectors.toList());
			}
		} else {
			return Collections.emptyList();
		}
	}

	private LiveBean getDefinedBeanForTypeDeclaration(TypeDeclaration declaringType) {
		if (declaringType != null) {
			for (Annotation annotation : ASTUtils.getAnnotations(declaringType)) {
				if (AnnotationHierarchies.isSubtypeOf(annotation, Annotations.COMPONENT)) {
					return ComponentInjectionsHoverProvider.getDefinedBeanForComponent(annotation);
				}
			}
			// TODO: handler below is an attempt to do something that may work in many
			// cases, but is probably
			// missing logics for special cases where annotation attributes on the declaring
			// type matter.
			ITypeBinding beanType = declaringType.resolveBinding();
			if (beanType != null) {
				String beanTypeName = beanType.getName();
				if (StringUtil.hasText(beanTypeName)) {
					return LiveBean.builder()
							.id(Character.toLowerCase(beanTypeName.charAt(0)) + beanTypeName.substring(1))
							.type(beanTypeName).build();
				}
			}
		}
		return null;
	}

	@Override
	public Hover provideHover(MethodDeclaration methodDeclaration, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		LiveBean definedBean = getDefinedBeanForImplicitAutowiredConstructor(methodDeclaration);
		return provideHover(definedBean, methodDeclaration, offset, doc, project, runningApps);
	}

	@Override
	public Collection<Range> getLiveHoverHints(IJavaProject project, MethodDeclaration methodDeclaration, TextDocument doc,
			SpringBootApp[] runningApps) {
		LiveBean definedBean = getDefinedBeanForImplicitAutowiredConstructor(methodDeclaration);
		try {
			Range hoverRange = doc.toRange(methodDeclaration.getName().getStartPosition(), methodDeclaration.getName().getLength());
			return getLiveHoverHints(project, methodDeclaration, hoverRange, runningApps, definedBean);
		} catch (BadLocationException e) {
			log.error("", e);
		}
		return null;
	}

	private LiveBean getDefinedBeanForImplicitAutowiredConstructor(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.isConstructor() && !methodDeclaration.parameters().isEmpty()) {
			TypeDeclaration typeDeclaration = ASTUtils.findDeclaringType(methodDeclaration);
			if (typeDeclaration != null && ASTUtils.hasExactlyOneConstructor(typeDeclaration) && !hasAutowiredAnnotation(methodDeclaration)) {
				return getDefinedBeanForTypeDeclaration(typeDeclaration);
			}
		}
		return null;
	}

	private boolean hasAutowiredAnnotation(MethodDeclaration constructor) {
		List<?> modifiers = constructor.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof MarkerAnnotation) {
				ITypeBinding typeBinding = ((MarkerAnnotation) modifier).resolveTypeBinding();
				if (typeBinding != null) {
					String fqName = typeBinding.getQualifiedName();
					return Annotations.AUTOWIRED.equals(fqName) || Annotations.INJECT.equals(fqName);
				}
			}
		}
		return false;
	}

}
