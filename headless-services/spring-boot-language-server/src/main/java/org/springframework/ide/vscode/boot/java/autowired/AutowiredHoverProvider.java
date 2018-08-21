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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
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

	public static final String BEANS_PREFIX = "\u21D0 ";

	private static final String JAVA_COLLECTION = "java.util.Collection";

	private final static Logger log = LoggerFactory.getLogger(AutowiredHoverProvider.class);

	private static final int MAX_INLINE_BEANS_STRING_LENGTH = 60;
	private static final String INLINE_BEANS_STRING_SEPARATOR = " ";

	private BootJavaLanguageServerComponents server;

	public AutowiredHoverProvider(BootJavaLanguageServerComponents server) {
		this.server = server;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
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

	private Collection<CodeLens> getLiveHoverHints(IJavaProject project, ASTNode declarationNode, Range range,
			SpringBootApp[] runningApps, LiveBean definedBean) {
		if (declarationNode != null && definedBean != null) {
			for (SpringBootApp app : runningApps) {
				List<LiveBean> relevantBeans = getRelevantAutowiredBeans(project, declarationNode, app, definedBean);
				return LiveHoverUtils.createCodeLensesForBeans(range, relevantBeans, BEANS_PREFIX, MAX_INLINE_BEANS_STRING_LENGTH, INLINE_BEANS_STRING_SEPARATOR);
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
			Hover hover = provideHover(definedBean, declarationNode, offset, doc, project, runningApps);
			if (hover != null) {
				try {
					hover.setRange(doc.toRange(annotation.getStartPosition(), annotation.getLength()));
				} catch (BadLocationException e) {
					log.error("", e);
				}
			}
			return hover;
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
					createHoverContentForBeans(server, definedBean, project, hover, autowiredBeans);
					hover.append(LiveHoverUtils.niceAppName(app));
				}

			}
			if (hasContent) {
				return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
			}
		}
		return null;
	}

	public static void createHoverContentForBeans(BootJavaLanguageServerComponents server, LiveBean definedBean, IJavaProject project, StringBuilder hover,
			List<LiveBean> autowiredBeans) {
		hover.append("**Autowired `");
		hover.append(definedBean.getId());
		hover.append("` &larr; ");
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
		hover.append(autowiredBeans.stream()
				.map(b -> "- " + LiveHoverUtils.showBeanWithResource(server, b, "  ", project))
				.collect(Collectors.joining("\n")));
		hover.append("\n  \n");
	}

	public static List<LiveBean> getRelevantAutowiredBeans(IJavaProject project, ASTNode declarationNode, SpringBootApp app, LiveBean definedBean) {
		List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(app, definedBean);

		if (!relevantBeans.isEmpty()) {
			List<LiveBean> allDependencyBeans = LiveHoverUtils.findAllDependencyBeans(app, relevantBeans);

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
	private static List<LiveBean> findAutowiredBeans(IJavaProject project, ASTNode declarationNode, Collection<LiveBean> beans) {
		if (declarationNode instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration)declarationNode;
			return ((List<Object>)methodDeclaration.parameters()).stream()
					.filter(p -> p instanceof SingleVariableDeclaration)
					.map(p -> (SingleVariableDeclaration)p)
					.map(singleVariableDeclaration -> {
						// Supposed to be a list of one bean for the variable declaration
						List<LiveBean> matches = findAutowiredBeans(project, singleVariableDeclaration, beans);
						return matches.isEmpty() ? null : matches.get(0);
					})
					.filter(matchedBean -> matchedBean != null)
					.collect(Collectors.toList());
		} else if (declarationNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration)declarationNode;
			ITypeBinding fieldType = fieldDeclaration.getType().resolveBinding();
			if (fieldType != null) {
				LiveBean matchedBean = matchBean(project, beans, fieldType, fieldDeclaration.modifiers());
				if (matchedBean != null) {
					return ImmutableList.of(matchedBean);
				}
			}
		} else if (declarationNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)declarationNode;
			ITypeBinding varType = singleVariableDeclaration.getType().resolveBinding();
			if (varType != null) {
				LiveBean matchedBean = matchBean(project, beans, varType, singleVariableDeclaration.modifiers());
				if (matchedBean != null) {
					return ImmutableList.of(matchedBean);
				}
			}
		}
		return Collections.emptyList();
	}

	private static LiveBean matchBean(IJavaProject project, Collection<LiveBean> beans, ITypeBinding typeBinding, List<Object> modifiers) {
		Optional<String> beanId = ASTUtils.beanId(modifiers);
		Collection<LiveBean> searchScope = beanId.isPresent() ?
				beans.stream()
					.filter(b -> beanId.get().equals(b.getId()))
					.findFirst()
					.map(bean -> (Collection<LiveBean>) ImmutableList.of(bean))
					.orElse(ImmutableList.of())
				: beans;
		return matchBeanByTypeOrCollection(project, searchScope, typeBinding);
	}

	private static boolean isInstanceOfCollection(ITypeBinding typeBinding) {
		if (typeBinding == null) {
			return false;
		} else {
			if (JAVA_COLLECTION.equals(typeBinding.getTypeDeclaration().getQualifiedName())) {
				return true;
			} else {
				for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
					if (isInstanceOfCollection(superInterface)) {
						return true;
					}
				}
				return isInstanceOfCollection(typeBinding.getSuperclass());
			}
		}
	}

	private static LiveBean matchBeanByTypeOrCollection(IJavaProject project, Collection<LiveBean> beans, ITypeBinding type) {
		if (isInstanceOfCollection(type)) {
			// Raw collections shouldn't match any beans
			return type.getTypeArguments().length == 1 ? matchBeanByType(project, beans, type.getTypeArguments()[0].getQualifiedName()) : null;
		} else if (type.isArray() && type.getDimensions() == 1) {
			return matchBeanByType(project, beans, type.getElementType().getQualifiedName());
		} else {
			return matchBeanByType(project, beans, type.getQualifiedName());
		}
	}

	private static LiveBean matchBeanByType(IJavaProject project, Collection<LiveBean> beans, String fqName) {
		if (fqName != null) {
			List<LiveBean> matches = matchBeansByFQName(project, beans, fqName, true);
			if (!matches.isEmpty()) {
				return matches.size() == 1 ? matches.get(0) : LiveHoverUtils.CANT_MATCH_PROPER_BEAN;
			} else {
				matches = beans.stream().filter(b -> AutowiredHoverProvider.isCompatibleBeanType(project, b, fqName))
						.limit(2).collect(Collectors.toList());
				if (!matches.isEmpty()) {
					return matches.size() == 1 ? matches.get(0) : LiveHoverUtils.CANT_MATCH_PROPER_BEAN;
				}
			}
		}
		return null;
	}

	private static boolean isCompatibleBeanType(IJavaProject jp, LiveBean bean, String bindingQualifiedName) {
		String liveBeanTypeFQName = bean.getType();
		if (liveBeanTypeFQName != null) {
			if (liveBeanTypeFQName.replace('$', '.').equals(bindingQualifiedName)) {
				return true;
			} else {
				IType type = jp.findType(liveBeanTypeFQName);
				String fqTypeName = bindingQualifiedName;
				if (type != null) {
					return jp.allSuperTypesOf(type).map(IType::getFullyQualifiedName)
							.filter(fqn -> fqTypeName.equals(fqn.replace('$', '.'))).blockFirst() != null;
				}
			}
		}
		return false;
	}


	private static List<LiveBean> matchBeansByFQName(IJavaProject project, Collection<LiveBean> beans, String fqName, boolean allDots) {
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
		Hover hover = provideHover(definedBean, methodDeclaration, offset, doc, project, runningApps);
		if (hover != null) {
			SimpleName name = methodDeclaration.getName();
			try {
				hover.setRange(doc.toRange(name.getStartPosition(), name.getLength()));
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		return hover;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, MethodDeclaration methodDeclaration, TextDocument doc,
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
