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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.autowired.AutowiredHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBeansModel;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public abstract class AbstractInjectedIntoHoverProvider implements HoverProvider {

	public static final String BEANS_PREFIX_PLAIN_TEXT = "\u2192 ";
	public static final String BEANS_PREFIX_MARKDOWN = "&#8594; ";

	private static Logger LOG = LoggerFactory.getLogger(AbstractInjectedIntoHoverProvider.class);

	private static final int MAX_INLINE_BEANS_STRING_LENGTH = 60;
	private static final String INLINE_BEANS_STRING_SEPARATOR = " ";

	private SourceLinks sourceLinks;

	public AbstractInjectedIntoHoverProvider(SourceLinks sourceLinks) {
		this.sourceLinks = sourceLinks;
	}
	
	@FunctionalInterface
	protected interface DefinedBeanProvider {
		LiveBean definedBean(SpringProcessLiveData liveData);
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc,
			SpringProcessLiveData[] processLiveData) {
		// Highlight if any running app contains an instance of this component
		try {
			if (processLiveData.length > 0) {
				LiveBean definedBean = getDefinedBean(annotation);
				if (definedBean != null) {
					if (Stream.of(processLiveData).anyMatch(app -> LiveHoverUtils.hasRelevantBeans(app, definedBean))) {
						Optional<Range> nameRange = ASTUtils.nameRange(doc, annotation);
						if (nameRange.isPresent()) {
							List<CodeLens> codeLenses = assembleCodeLenses(project, processLiveData, app -> definedBean, doc, nameRange.get(), annotation);
							return codeLenses;
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
		return ImmutableList.of();
	}

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {

			LiveBean definedBean = getDefinedBean(annotation);
			if (definedBean != null) {
				Hover hover = assembleHover(project, processLiveData, app -> definedBean, annotation, true, true);
				if (hover != null) {
					Optional<Range> nameRange = ASTUtils.nameRange(doc, annotation);
					if (nameRange.isPresent()) {
						hover.setRange(nameRange.get());
					}
				}
				return hover;
			}
		}
		return null;
	}

	protected List<CodeLens> assembleCodeLenses(IJavaProject project, SpringProcessLiveData[] processLiveData, DefinedBeanProvider definedBeanProvider,
			TextDocument doc, Range range, ASTNode node) {
		boolean beanFound = false;
		for (SpringProcessLiveData liveData : processLiveData) {
			
			LiveBean definedBean = definedBeanProvider.definedBean(liveData);
			if (definedBean == null) {
				continue;
			}

			beanFound = true;
			
			List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(liveData, definedBean);

			if (!relevantBeans.isEmpty()) {
				List<LiveBean> injectedBeans = getRelevantInjectedIntoBeans(project, liveData, definedBean, relevantBeans);
				ImmutableList.Builder<CodeLens> builder = ImmutableList.builder();
				if (!injectedBeans.isEmpty()) {
					// Break out of the loop. Just look for the first app with injected into beans
					List<CodeLens> injectedCodeLenses = LiveHoverUtils.createCodeLensesForBeans(range, injectedBeans,
							BEANS_PREFIX_PLAIN_TEXT, MAX_INLINE_BEANS_STRING_LENGTH, INLINE_BEANS_STRING_SEPARATOR);
					builder.addAll(
							injectedCodeLenses.isEmpty() ? ImmutableList.of(new CodeLens(range)) : injectedCodeLenses);
				}

				// Wired beans code lenses
				List<LiveBean> wiredBeans = findWiredBeans(project, liveData, relevantBeans, node);
				builder.addAll(assembleCodeLenseForAutowired(wiredBeans, project, liveData, doc, range, node));

				List<CodeLens> codeLenses = builder.build();
				return codeLenses.isEmpty() ? ImmutableList.of(new CodeLens(range)) : codeLenses;
			}
			
		}
		return beanFound ? ImmutableList.of(new CodeLens(range)) : null;
	}

	protected List<CodeLens> assembleCodeLenseForAutowired(List<LiveBean> wiredBeans, IJavaProject project, SpringProcessLiveData processLiveData, TextDocument doc, Range nameRange, ASTNode astNode) {
		return LiveHoverUtils.createCodeLensesForBeans(nameRange, wiredBeans,
				AutowiredHoverProvider.BEANS_PREFIX_PLAIN_TEXT, MAX_INLINE_BEANS_STRING_LENGTH,
				INLINE_BEANS_STRING_SEPARATOR);
	}

	protected List<LiveBean> findWiredBeans(IJavaProject project, SpringProcessLiveData liveData, List<LiveBean> relevantBeans, ASTNode astNode) {
		return Collections.emptyList();
	}

	protected Hover assembleHover(IJavaProject project, SpringProcessLiveData[] processLiveData, DefinedBeanProvider definedBeanProvider, ASTNode astNode, boolean injected, boolean wired) {
		StringBuilder hover = new StringBuilder();

		for (SpringProcessLiveData liveData : processLiveData) {

			LiveBean definedBean = definedBeanProvider.definedBean(liveData);
			
			if (definedBean == null) {
				continue;
			}

			List<LiveBean> relevantBeans = LiveHoverUtils.findRelevantBeans(liveData, definedBean);
			
			if (!relevantBeans.isEmpty()) {
				if (hover.length() > 0) {
					hover.append("  \n  \n");
				}

				if (injected) {
					List<LiveBean> injectedBeans = getRelevantInjectedIntoBeans(project, liveData, definedBean, relevantBeans);
					if (!injectedBeans.isEmpty()) {
						hover.append("**");
						hover.append(LiveHoverUtils.createBeansTitleMarkdown(sourceLinks, project, injectedBeans, BEANS_PREFIX_MARKDOWN, MAX_INLINE_BEANS_STRING_LENGTH, INLINE_BEANS_STRING_SEPARATOR));
						hover.append("**\n");
						hover.append(injectedBeans.stream()
								.map(b -> "- " + LiveHoverUtils.showBeanWithResource(sourceLinks, b, "  ", project))
								.collect(Collectors.joining("\n")));
						hover.append("\n  \n");
					}
				}

				if (wired) {
					List<LiveBean> wiredBeans = findWiredBeans(project, liveData, relevantBeans, astNode);
					if (!wiredBeans.isEmpty()) {
						AutowiredHoverProvider.createHoverContentForBeans(sourceLinks, project, hover, wiredBeans);
					}

					hover.append("Bean id: `");
					hover.append(definedBean.getId());
					hover.append("`  \n");
					hover.append(LiveHoverUtils.niceAppName(liveData));
				}
			}

		}
		if (hover.length() > 0) {
			return new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
		} else {
			return null;
		}

	}
	
	protected List<LiveBean> getRelevantInjectedIntoBeans(IJavaProject project, SpringProcessLiveData processLiveData, LiveBean definedBean, List<LiveBean> relevantBeans) {
		LiveBeansModel beans = processLiveData.getBeans();
		if (relevantBeans != null) {
			return relevantBeans.stream()
					.flatMap(b -> beans.getBeansDependingOn(b.getId()).stream())
					.distinct()
					.collect(Collectors.toList());

		}
		return Collections.emptyList();
	}

	protected abstract LiveBean getDefinedBean(Annotation annotation);

}
