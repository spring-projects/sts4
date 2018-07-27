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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public abstract class AbstractInjectedIntoHoverProvider implements HoverProvider {

	protected BootJavaLanguageServerComponents server;

	public AbstractInjectedIntoHoverProvider(BootJavaLanguageServerComponents server) {
		this.server = server;
	}

	@Override
	public Collection<Range> getLiveHoverHints(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		// Highlight if any running app contains an instance of this component
		try {
			if (runningApps.length > 0) {
				LiveBean definedBean = getDefinedBean(annotation);
				if (definedBean != null) {
					if (Stream.of(runningApps).anyMatch(app -> LiveHoverUtils.hasRelevantBeans(app, definedBean))) {
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

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {

			LiveBean definedBean = getDefinedBean(annotation);
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

	protected abstract LiveBean getDefinedBean(Annotation annotation);

	protected void addInjectedInto(LiveBean definedBean, StringBuilder hover, LiveBeansModel beans, LiveBean bean, IJavaProject project) {
		hover.append("\n\n");
		List<LiveBean> dependers = beans.getBeansDependingOn(bean.getId());
		if (dependers.isEmpty()) {
			hover.append(LiveHoverUtils.showBean(bean) + " exists but is **Not injected anywhere**\n");
		} else {
			hover.append(LiveHoverUtils.showBean(bean) + " injected into:\n\n");
			boolean firstDependency = true;
			for (LiveBean dependingBean : dependers) {
				if (!firstDependency) {
					hover.append("\n");
				}
				hover.append("- " + LiveHoverUtils.showBeanWithResource(server, dependingBean, "  ", project));
				firstDependency = false;
			}
		}
	}
}
