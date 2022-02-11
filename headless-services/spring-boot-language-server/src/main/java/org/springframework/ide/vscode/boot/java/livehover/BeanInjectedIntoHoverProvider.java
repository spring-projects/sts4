/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.autowired.AutowiredHoverProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Optionals;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class BeanInjectedIntoHoverProvider extends AbstractInjectedIntoHoverProvider {

	private static final Logger log = LoggerFactory.getLogger(BeanInjectedIntoHoverProvider.class);

	public BeanInjectedIntoHoverProvider(SourceLinks sourceLinks) {
		super(sourceLinks);
	}

	@Override
	protected LiveBean getDefinedBean(Annotation annotation) {
		MethodDeclaration beanMethod = ORAstUtils.getAnnotatedMethod(annotation);
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
				() -> ORAstUtils.getAttribute(annotation, "value").flatMap(ORAstUtils::getFirstString),
				() -> ORAstUtils.getAttribute(annotation, "name").flatMap(ORAstUtils::getFirstString),
				() -> Optional.ofNullable(beanMethod.getName().getSimpleName())
		);
	}

	@Override
	protected List<LiveBean> findWiredBeans(IJavaProject project, SpringProcessLiveData liveData, List<LiveBean> relevantBeans, J astNode) {
		if (astNode instanceof Annotation) {
			// @Bean annotation case
			MethodDeclaration beanMethod = ORAstUtils.getAnnotatedMethod((Annotation) astNode);
			if (beanMethod != null) {
				return AutowiredHoverProvider.getRelevantAutowiredBeans(project, beanMethod, liveData, relevantBeans);
			}
		} else if (astNode instanceof VariableDeclarations) {
			// Bean method parameter case
			return AutowiredHoverProvider.getRelevantAutowiredBeans(project, astNode, liveData, relevantBeans);
		}
		return Collections.emptyList();
	}

	@Override
	protected List<CodeLens> assembleCodeLenseForAutowired(List<LiveBean> wiredBeans, IJavaProject project,
			SpringProcessLiveData liveData, TextDocument doc, Range nameRange, J astNode) {
		ImmutableList.Builder<CodeLens> builder = ImmutableList.builder();

		// Code lens for the @Bean annotation
		builder.addAll(super.assembleCodeLenseForAutowired(wiredBeans, project, liveData, doc, nameRange, astNode));

		if (astNode instanceof Annotation) {
			// Add code lenses for method parameters
			MethodDeclaration beanMethod = ORAstUtils.getAnnotatedMethod((Annotation) astNode);
			if (beanMethod != null) {
				builder.addAll(LiveHoverUtils.createCodeLensForMethodParameters(liveData, project, beanMethod, doc, wiredBeans));
			}
		}

		return builder.build();
	}

	@Override
	public Hover provideMethodParameterHover(VariableDeclarations parameter, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		try {
			if (processLiveData.length > 0) {
				Range range = ORAstUtils.nodeRegion(doc, parameter.getVariables().get(0)).asRange();
				MethodDeclaration method = (MethodDeclaration) ORAstUtils.getParent(parameter);
				Annotation beanAnnotation = ORAstUtils.getBeanAnnotation(method);
				if (beanAnnotation != null) {
					LiveBean definedBean = getDefinedBean(beanAnnotation);
					if (definedBean != null) {
						Hover hover = assembleHover(project, processLiveData, app -> definedBean, parameter, false, true);
						if (hover != null) {
							hover.setRange(range);
						}
						return hover;
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}


}
