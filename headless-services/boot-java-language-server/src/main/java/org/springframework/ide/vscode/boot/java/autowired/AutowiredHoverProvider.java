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
package org.springframework.ide.vscode.boot.java.autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ASTUtils;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class AutowiredHoverProvider implements HoverProvider {

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			for (SpringBootApp bootApp : runningApps) {
				try {
					LiveBeansModel liveBeans = bootApp.getBeans();
					if (liveBeans != null && !liveBeans.isEmpty()) {
						Range range = getLiveHoverHint(annotation, doc, liveBeans);
						if (range != null) {
							return ImmutableList.of(range);
						}
					}
				}
				catch (Exception e) {
					Log.log(e);
				}
			}
		}
		catch (Exception e) {
			Log.log(e);
		}

		return null;
	}

	public Range getLiveHoverHint(Annotation annotation, TextDocument doc, LiveBeansModel beansModel) {
		try {
			TypeDeclaration declaringType = ASTUtils.findDeclaringType(annotation);
			if (declaringType != null) {
				String type = declaringType.resolveBinding().getQualifiedName();
				if (type != null && beansModel != null) {
					List<LiveBean> beansOfType = beansModel.getBeansOfType(type);
					if (!beansOfType.isEmpty()) {
						Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
						return hoverRange;
					}
				}
			}
		}
		catch (BadLocationException e) {
			Log.log(e);
		}

		return null;
	}

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {

			StringBuilder hover = new StringBuilder();

			LiveBean definedBean = ASTUtils.getDefinedBean(annotation);
			if (definedBean != null) {

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
							hover.append("\n\n");
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

	private void addAutomaticallyWired(StringBuilder hover, Annotation annotation, LiveBeansModel beans, LiveBean bean) {
		TypeDeclaration typeDecl = ASTUtils.findDeclaringType(annotation);
		if (typeDecl != null) {
			String[] dependencies = bean.getDependencies();

			if (dependencies != null && dependencies.length > 0) {
				hover.append(LiveHoverUtils.showBean(bean) + " got autowired with:\n\n");

				boolean firstDependency = true;
				for (String injectedBean : dependencies) {
					if (!firstDependency) {
						hover.append("\n");
					}
					List<LiveBean> dependencyBeans = beans.getBeansOfName(injectedBean);
					for (LiveBean dependencyBean : dependencyBeans) {
						hover.append("- " + LiveHoverUtils.showBean(dependencyBean));
					}
					firstDependency = false;
				}
			}
		}
	}

}
