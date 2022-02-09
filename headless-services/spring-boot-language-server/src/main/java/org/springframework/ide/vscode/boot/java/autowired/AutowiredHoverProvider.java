/*******************************************************************************
 * Copyright (c) 2017, 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.Array;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Parameterized;
import org.openrewrite.java.tree.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.beans.BeanUtils;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.ComponentInjectionsHoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
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

	public static final String BEANS_PREFIX_PLAIN_TEXT = "\u2190 ";
	public static final String BEANS_PREFIX_MARDOWN = "&#8592; ";

	private static final String JAVA_COLLECTION = "java.util.Collection";

	private final static Logger log = LoggerFactory.getLogger(AutowiredHoverProvider.class);

	private static final int MAX_INLINE_BEANS_STRING_LENGTH = 60;
	private static final String INLINE_BEANS_STRING_SEPARATOR = " ";
	private SourceLinks sourceLinks;

	public AutowiredHoverProvider(SourceLinks sourceLinks) {
		this.sourceLinks = sourceLinks;

	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc,
			SpringProcessLiveData[] processLiveData) {
		ImmutableList.Builder<CodeLens> builder = ImmutableList.builder();
		if (processLiveData.length > 0) {
			LiveBean definedBean = getDefinedBeanForTypeDeclaration(ORAstUtils.findDeclaringType(annotation));
			// Annotation is MarkerNode, parent is some field, method, variable declaration
			// node.
			J declarationNode = ORAstUtils.getParent(annotation);
			try {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(annotation);
				Range hoverRange = doc.toRange(r.getStart().getOffset(), r.length());
				for (SpringProcessLiveData liveData : processLiveData) {
					List<LiveBean> relevantBeans = getRelevantAutowiredBeans(project, declarationNode, liveData,
							definedBean);
					if (!relevantBeans.isEmpty()) {
						builder.addAll(LiveHoverUtils.createCodeLensesForBeans(hoverRange, relevantBeans,
								BEANS_PREFIX_PLAIN_TEXT, MAX_INLINE_BEANS_STRING_LENGTH,
								INLINE_BEANS_STRING_SEPARATOR));
					}
				}
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		return builder.build();
	}

	@Override
	public Hover provideHover(J node, Annotation annotation, int offset,
			TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {
			LiveBean definedBean = getDefinedBeanForTypeDeclaration(ORAstUtils.findDeclaringType(annotation));
			// Annotation is MarkerNode, parent is some field, method, variable declaration node.
			J declarationNode = ORAstUtils.getParent(annotation);
			Hover hover = provideHover(definedBean, declarationNode, offset, doc, project, processLiveData);
			if (hover != null) {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(annotation);
				try {
					hover.setRange(doc.toRange(r.getStart().getOffset(), r.length()));
				} catch (BadLocationException e) {
					log.error("", e);
				}
			}
			return hover;
		}
		return null;
	}

	private Hover provideHover(LiveBean definedBean, J declarationNode, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (definedBean != null) {

			StringBuilder hover = new StringBuilder();

			for (SpringProcessLiveData liveData : processLiveData) {

				List<LiveBean> autowiredBeans = getRelevantAutowiredBeans(project, declarationNode, liveData, definedBean);

				if (!autowiredBeans.isEmpty()) {
					if (hover.length() > 0) {
						hover.append("  \n  \n");
					}
					createHoverContentForBeans(sourceLinks, project, hover, autowiredBeans);
					hover.append("Bean id: `");
					hover.append(definedBean.getId());
					hover.append("`  \n");
					hover.append(LiveHoverUtils.niceAppName(liveData));
				}

			}
			if (hover.length() > 0) {
				return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
			}
		}
		return null;
	}

	public static void createHoverContentForBeans(SourceLinks sourceLinks, IJavaProject project, StringBuilder hover,
			List<LiveBean> autowiredBeans) {
		hover.append("**");
		hover.append(LiveHoverUtils.createBeansTitleMarkdown(sourceLinks, project, autowiredBeans, BEANS_PREFIX_MARDOWN, MAX_INLINE_BEANS_STRING_LENGTH, INLINE_BEANS_STRING_SEPARATOR));
		hover.append("**\n");
		hover.append(autowiredBeans.stream()
				.map(b -> "- " + LiveHoverUtils.showBeanWithResource(sourceLinks, b, "  ", project))
				.collect(Collectors.joining("\n")));
		hover.append("\n  \n");
	}

	public static List<LiveBean> getRelevantAutowiredBeans(IJavaProject project, J declarationNode, SpringProcessLiveData liveData, LiveBean definedBean) {
		List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(liveData, definedBean);
		return getRelevantAutowiredBeans(project, declarationNode, liveData, relevantBeans);
	}

	public static List<LiveBean> getRelevantAutowiredBeans(IJavaProject project, J declarationNode, SpringProcessLiveData liveData, List<LiveBean> relevantBeans) {
		if (!relevantBeans.isEmpty()) {
			List<LiveBean> allDependencyBeans = LiveHoverUtils.findAllDependencyBeans(liveData, relevantBeans);

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
	public static List<LiveBean> findAutowiredBeans(IJavaProject project, J declarationNode, Collection<LiveBean> beans) {
		if (declarationNode instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration)declarationNode;
			return methodDeclaration.getParameters().stream()
				.filter(VariableDeclarations.class::isInstance)
				.map(VariableDeclarations.class::cast)
				.flatMap(v -> findAutowiredBeans(project, v, beans).stream())
				.collect(Collectors.toList());
		} else if (declarationNode instanceof VariableDeclarations) {
			VariableDeclarations varDeclaration = (VariableDeclarations) declarationNode;
			JavaType varType = varDeclaration.getType();
			if (varType != null) {
				return matchBeans(project, beans, varType, varDeclaration.getLeadingAnnotations());
			}
		}
		return Collections.emptyList();
	}

	private static List<LiveBean> matchBeans(IJavaProject project, Collection<LiveBean> beans, JavaType type, List<Annotation> annotations) {
		Optional<String> beanId = ORAstUtils.beanId(annotations);
		Collection<LiveBean> searchScope = beanId.isPresent() ?
				beans.stream()
					.filter(b -> beanId.get().equals(b.getId()))
					.findFirst()
					.map(bean -> (Collection<LiveBean>) ImmutableList.of(bean))
					.orElse(ImmutableList.of())
				: beans;
		return matchBeansByTypeOrCollection(project, searchScope, type);
	}

	private static boolean isInstanceOfCollection(JavaType type) {
		return type == null ? false : TypeUtils.isAssignableTo(JAVA_COLLECTION, type);
	}

	private static List<LiveBean> matchBeansByTypeOrCollection(IJavaProject project, Collection<LiveBean> beans, JavaType type) {
		if (isInstanceOfCollection(type)) {
			// Raw collections shouldn't match any beans
			if (type instanceof Parameterized) {
				Parameterized parameterized = (Parameterized) type;
				if (parameterized.getTypeParameters().size() == 1) {
					FullyQualified parameterType = TypeUtils.asFullyQualified(parameterized.getTypeParameters().get(0));
					if (parameterType != null) {
						return matchBeansByType(project, beans, parameterType.getFullyQualifiedName(), false);
					}
				}
			}
			return ImmutableList.of();
		} else if (type instanceof Array && ((Array) type).getManagedReference() == 1) {
			Array array = (Array) type;
			FullyQualified arrayType = TypeUtils.asFullyQualified(array.getElemType()); 
			return matchBeansByType(project, beans, arrayType == null ? null : arrayType.getFullyQualifiedName(), false);
		} else {
			FullyQualified fq = TypeUtils.asFullyQualified(type); 
			return matchBeansByType(project, beans, fq == null ? null : fq.getFullyQualifiedName(), true);
		}
	}

	private static List<LiveBean> matchBeansByType(IJavaProject project, Collection<LiveBean> beans, String fqName, boolean allowOneMatchOnly) {
		if (fqName != null) {
			if (allowOneMatchOnly) {
					List<LiveBean> matches = beans.stream().filter(b -> AutowiredHoverProvider.isCompatibleBeanType(project, b, fqName))
							.limit(2).collect(Collectors.toList());
					if (!matches.isEmpty()) {
						return matches.size() == 1 ? matches : ImmutableList.of(LiveHoverUtils.CANT_MATCH_PROPER_BEAN);
					}
			} else {
				return beans.stream().filter(b -> AutowiredHoverProvider.isCompatibleBeanType(project, b, fqName)).collect(Collectors.toList());
			}
		}
		return ImmutableList.of();
	}

	private static boolean isCompatibleBeanType(IJavaProject jp, LiveBean bean, String bindingQualifiedName) {
		String rawLiveBeanFqName = bean.getType(true);
		int idx = rawLiveBeanFqName.indexOf('<');
		// Trim the generic parameters part if it's present
		String liveBeanTypeFQName = idx < 0 ? rawLiveBeanFqName : rawLiveBeanFqName.substring(0, idx);

		if (liveBeanTypeFQName != null) {
			if (liveBeanTypeFQName.equals(bindingQualifiedName)) {
				return true;
			}
			else {
				return jp.getIndex().allSuperTypesOf(liveBeanTypeFQName, true, false).map(IType::getFullyQualifiedName)
						.filter(fqn -> bindingQualifiedName.equals(fqn)).blockFirst() != null;
			}
		}
		return false;
	}

	private LiveBean getDefinedBeanForTypeDeclaration(ClassDeclaration declaringType) {
		if (declaringType != null) {
			for (Annotation annotation : ORAstUtils.getAnnotations(declaringType)) {
				if (AnnotationHierarchies.isSubtypeOf(annotation, Annotations.COMPONENT)) {
					return ComponentInjectionsHoverProvider.getDefinedBeanForComponent(annotation);
				}
			}
			// TODO: handler below is an attempt to do something that may work in many
			// cases, but is probably
			// missing logics for special cases where annotation attributes on the declaring
			// type matter.
			FullyQualified beanType = declaringType.getType();
			if (beanType != null) {
				String beanTypeName = beanType.getClassName();
				if (StringUtil.hasText(beanTypeName)) {
					return LiveBean.builder()
							.id(getId(beanTypeName))
							.type(beanTypeName).build();
				}
			}
		}
		return null;
	}

	private String getId(String beanTypeName) {
		return BeanUtils.getBeanNameFromType(beanTypeName);
	}

	@Override
	public Hover provideHover(MethodDeclaration methodDeclaration, int offset, TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		LiveBean definedBean = getDefinedBeanForImplicitAutowiredConstructor(methodDeclaration);
		Hover hover = provideHover(definedBean, methodDeclaration, offset, doc, project, processLiveData);
		if (hover != null) {
			Identifier name = methodDeclaration.getName();
			try {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(name);
				hover.setRange(doc.toRange(r.getStart().getOffset(), r.length()));
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		return hover;
	}

	@Override
	public Hover provideMethodParameterHover(VariableDeclarations parameter, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		MethodDeclaration method = (MethodDeclaration) ORAstUtils.getParent(parameter);
		LiveBean definedBean = getDefinedBeanForImplicitAutowiredConstructor(method);
		Hover hover = provideHover(definedBean, parameter, offset, doc, project, processLiveData);
		if (hover != null) {
			Identifier name = parameter.getVariables().get(0).getName();
			try {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(name);
				hover.setRange(doc.toRange(r.getStart().getOffset(), r.length()));
			} catch (BadLocationException e) {
				log.error("", e);
			}
		}
		return hover;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, MethodDeclaration methodDeclaration,
			TextDocument doc, SpringProcessLiveData[] processLiveData) {
		ImmutableList.Builder<CodeLens> builder = ImmutableList.builder();
		LiveBean definedBean = getDefinedBeanForImplicitAutowiredConstructor(methodDeclaration);
		if (definedBean != null) {
			try {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(methodDeclaration.getName());
				Range hoverRange = doc.toRange(r.getStart().getOffset(), r.length());

				for (SpringProcessLiveData liveData : processLiveData) {
					List<LiveBean> relevantBeans = getRelevantAutowiredBeans(project, methodDeclaration, liveData,
							definedBean);
					if (!relevantBeans.isEmpty()) {

						// CodeLens for the method
						builder.addAll(LiveHoverUtils.createCodeLensesForBeans(hoverRange, relevantBeans,
								BEANS_PREFIX_PLAIN_TEXT, MAX_INLINE_BEANS_STRING_LENGTH,
								INLINE_BEANS_STRING_SEPARATOR));

						// CodeLenses for the method parameters. Only ranges just to provide a highlight
						// for the hover
						builder.addAll(LiveHoverUtils.createCodeLensForMethodParameters(liveData, project, methodDeclaration, doc, relevantBeans));

					}
				}
			} catch (BadLocationException e) {
				log.error("", e);
			}

		}
		return builder.build();
	}

	private LiveBean getDefinedBeanForImplicitAutowiredConstructor(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.isConstructor() && !methodDeclaration.getParameters().isEmpty()) {
			ClassDeclaration typeDeclaration = ORAstUtils.findDeclaringType(methodDeclaration);
			if (typeDeclaration != null && ORAstUtils.hasExactlyOneConstructor(typeDeclaration) && !hasAutowiredAnnotation(methodDeclaration)) {
				return getDefinedBeanForTypeDeclaration(typeDeclaration);
			}
		}
		return null;
	}

	private boolean hasAutowiredAnnotation(MethodDeclaration constructor) {
		for (Annotation a : constructor.getLeadingAnnotations()) {
			FullyQualified type = TypeUtils.asFullyQualified(a.getType());
			if (type != null) {
				String fqName = type.getFullyQualifiedName();
				if (Annotations.AUTOWIRED.equals(fqName) || Annotations.INJECT.equals(fqName)) {
					return true;
				}
			}
		}
		return false;
	}

}
