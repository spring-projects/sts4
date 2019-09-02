/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.beans.BeanUtils;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBeansModel;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ComponentInjectionsHoverProvider extends AbstractInjectedIntoHoverProvider {

	private static Logger LOG = LoggerFactory.getLogger(ComponentInjectionsHoverProvider.class);

	public ComponentInjectionsHoverProvider(SourceLinks sourceLinks) {
		super(sourceLinks);
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
				String id = getBeanId(annotation, beanType, Flags.isStatic(declaringType.getModifiers()));
				if (StringUtil.hasText(id)) {
					return LiveBean.builder().id(id).type(getBeanType(beanType).toString()).build();
				}
			}
		}
		return null;
	}

	private static String getBeanId(Annotation annotation, ITypeBinding beanType, boolean isStatic) {
		return ASTUtils.getAttribute(annotation, "value").flatMap(ASTUtils::getFirstString).orElseGet(() -> {
			ITypeBinding declaringClass = beanType.getDeclaringClass();
			if (declaringClass == null) {
				return BeanUtils.getBeanNameFromType(beanType.getName());
			} else {
				if (isStatic) {
					// Static inner class case id `outerClass.InnerClass`
					String typeName = beanType.getBinaryName();
					// Trim package prefix and replace $ with . inner class separator
					int idx = typeName.lastIndexOf('.');
					if (idx >= 0) {
						typeName = typeName.substring(idx + 1).replace('$', '.');
					}
					return BeanUtils.getBeanNameFromType(typeName);
				} else {
					// Non-static inner class id case is binary type name
					return getBeanType(beanType).toString();
				}
			}
		});
	}

	private static String getBeanType(ITypeBinding beanType) {
		return beanType.getBinaryName();
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, TypeDeclaration typeDeclaration,
			TextDocument doc, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0 && !isComponentAnnotatedType(typeDeclaration)) {
			try {
				ITypeBinding beanType = typeDeclaration.resolveBinding();
				if (beanType != null) {
					String id = getBeanId(null, beanType, Flags.isStatic(typeDeclaration.getModifiers()));
					Optional<Range> nameRange = Optional.of(ASTUtils.nodeRegion(doc, typeDeclaration.getName()).asRange());
					if (nameRange.isPresent()) {
						List<CodeLens> codeLenses = assembleCodeLenses(project, processLiveData, liveData -> definedBean(liveData, getBeanType(beanType), id), doc,
								nameRange.get(), typeDeclaration);
						if (codeLenses != null) {
							return codeLenses.isEmpty() ? ImmutableList.of(new CodeLens(nameRange.get())) : codeLenses;
						}
					}
				}
			} catch (Exception e) {
				LOG.error("", e);
			}
		}
		return ImmutableList.of();
	}
	
	private LiveBean definedBean(SpringProcessLiveData liveData, String beanType, String possibleId) {
		LiveBeansModel beans = liveData.getBeans();
		if (beans != null) {
			if (beans.getBeansOfName(possibleId).isEmpty()) {
				// try bean type if there is only one bean of such type
				List<LiveBean> liveBeanCandidates = beans.getBeansOfType(beanType);
				if (liveBeanCandidates.size() == 1) {
					return liveBeanCandidates.get(0);
				}
			} else {
				// Just construct defined bean ourselves
				return LiveBean.builder().id(possibleId).type(beanType).build();
			}
		}
		return null;
	}

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {

		if (processLiveData.length > 0 && !isComponentAnnotatedType(typeDeclaration)) {
			ITypeBinding beanType = typeDeclaration.resolveBinding();
			if (beanType != null) {
				String id = getBeanId(null, beanType, Flags.isStatic(typeDeclaration.getModifiers()));
	
				Hover hover = assembleHover(project, processLiveData, app -> definedBean(app, getBeanType(beanType), id), typeDeclaration, true, true);
				if (hover != null) {
					SimpleName name = typeDeclaration.getName();
					try {
						hover.setRange(doc.toRange(name.getStartPosition(), name.getLength()));
					} catch (BadLocationException e) {
						LOG.error("", e);
					}
				}
				return hover;
			}
		}
		return null;
	}

	@Override
	protected List<LiveBean> findWiredBeans(IJavaProject project, SpringProcessLiveData liveData, List<LiveBean> relevantBeans,
			ASTNode astNode) {
		TypeDeclaration typeDeclaration = null;
		if (astNode instanceof TypeDeclaration) {
			typeDeclaration = (TypeDeclaration) astNode;
		} else if (astNode instanceof Annotation) {
			typeDeclaration = ASTUtils.getAnnotatedType((Annotation) astNode);
		}
		return typeDeclaration == null ? Collections.emptyList() : LiveHoverUtils.findAllDependencyBeans(liveData, relevantBeans);
	}

	private boolean isComponentAnnotatedType(TypeDeclaration typeDeclaration) {
		List<?> modifiers = typeDeclaration.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				ITypeBinding typeBinding = ((Annotation) modifier).resolveTypeBinding();
				if (isComponentAnnotation(typeBinding)) {
					return true;
				}
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
