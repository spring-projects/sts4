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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
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
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc, SpringBootApp[] runningApps) {

		SpringBootAppProvider[] bootApps = new SpringBootAppProvider[runningApps.length];
		for (int i = 0; i < runningApps.length; i++) {
			bootApps[i] = new SpringBootAppProviderImpl(runningApps[i]);
		}
		return provideHover(node, annotation, type, offset, doc, bootApps);
	}

	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc, SpringBootAppProvider[] runningApps) {
		try {
			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

			for (SpringBootAppProvider bootApp : runningApps) {
				try {
					LiveBeansModel liveBeans = bootApp.getBeans();
					if (liveBeans != null && !liveBeans.isEmpty()) {
						addLiveHoverContent(annotation, doc, liveBeans, bootApp, hoverContent);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (hoverContent.size() > 0) {
				Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
				Hover hover = new Hover();

				hover.setContents(hoverContent);
				hover.setRange(hoverRange);

				return CompletableFuture.completedFuture(hover);
			}
		} catch (Exception e) {
			Log.log(e);
		}

		return null;
	}

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
					e.printStackTrace();
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
			String type = findDeclaredType(annotation);
			if (type != null && beansModel != null) {
				LiveBean[] beansOfType = beansModel.getBeansOfType(type);
				if (beansOfType.length > 0) {
					Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
					return hoverRange;
				}
			}
		}
		catch (BadLocationException e) {
			Log.log(e);
		}

		return null;
	}

	public void addLiveHoverContent(Annotation annotation, TextDocument doc, LiveBeansModel beansModel, SpringBootAppProvider bootApp, List<Either<String, MarkedString>> hoverContent) {
		String type = findDeclaredType(annotation);
		if (type != null && beansModel != null) {
			LiveBean[] beansOfType = beansModel.getBeansOfType(type);

			if (beansOfType.length > 0) {
				String processId = bootApp.getProcessID();
				String processName = bootApp.getProcessName();

				for (LiveBean liveBean : beansOfType) {
					String[] dependencies = liveBean.getDependencies();

					if (dependencies != null && dependencies.length > 0) {
						hoverContent.add(Either.forLeft("bean: " + liveBean.getId()));
						hoverContent.add(Either.forLeft("injected beans:"));

						for (String dependency : dependencies) {
							LiveBean[] dependencyBeans = beansModel.getBeansOfName(dependency);
							for (LiveBean dependencyBean : dependencyBeans) {
								hoverContent.add(Either.forLeft("- '" + dependencyBean.getId() + "' - from: " + dependencyBean.getResource()));
							}
						}
					}
					else {
						// TODO: no dependencies found
					}
				}

				hoverContent.add(Either.forLeft("Process ID: " + processId));
				hoverContent.add(Either.forLeft("Process Name: " + processName));
			}
		}
	}

	private String findDeclaredType(Annotation annotation) {
		ASTNode node = annotation;
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}

		if (node != null) {
			TypeDeclaration typeDecl = (TypeDeclaration) node;
			return typeDecl.resolveBinding().getQualifiedName();
		}
		else {
			return null;
		}
	}

}
