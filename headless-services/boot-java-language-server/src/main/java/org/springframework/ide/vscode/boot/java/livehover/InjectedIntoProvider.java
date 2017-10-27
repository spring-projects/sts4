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
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class InjectedIntoProvider implements HoverProvider {

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		//Highlight if any running app contains an instance of this component
		try {
			if (runningApps.length > 0) {
				String beanType = getAnnotatedType(annotation);
				if (beanType!=null) {
					if (Stream.of(runningApps).anyMatch(app -> !app.getBeans().getBeansOfType(beanType).isEmpty())) {
						return ImmutableList.of(doc.toRange(annotation.getStartPosition(), annotation.getLength()));
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return ImmutableList.of();
	}

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, SpringBootApp[] runningApps) {
		if (runningApps.length>0) {
			String beanType = getAnnotatedType(annotation);
			if (beanType!=null) {
				StringBuilder hover = new StringBuilder();
				hover.append("**Injection report for `"+beanType+"'**\n\n");
				boolean hasInterestingApp = false;
				for (SpringBootApp app  : runningApps) {
					LiveBeansModel beans = app.getBeans();
					boolean isInteresting = !app.getBeans().getBeansOfType(beanType).isEmpty();
					if (isInteresting) {
						hasInterestingApp = true;
						hover.append(niceAppName(app)+":");
						for (LiveBean bean : beans.getBeansOfType(beanType)) {
							hover.append("\n\n");
							hover.append("Bean with id: `"+bean.getId()+"`\n");
							List<LiveBean> dependers = beans.getBeansDependingOn(bean.getId());
							if (dependers.isEmpty()) {
								hover.append("**Not injected anywhere**\n");
							} else {
								hover.append("injected into:\n\n");
								for (LiveBean dependingBean : dependers) {
									hover.append("- "+dependingBean.getType()+"\n");
								}
							}
						}
					}
				}
				if (hasInterestingApp) {
					System.out.println(hover);
					return CompletableFuture.completedFuture(new Hover(
							ImmutableList.of(Either.forLeft(hover.toString()))
					));
				}
			}
		}
		return null;
	}

	private String getAnnotatedType(Annotation annotation) {
		ASTNode parent = annotation.getParent();
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) parent;
			ITypeBinding binding = typeDecl.resolveBinding();
			if (binding!=null) {
				return binding.getQualifiedName();
			}
		}
		return null;
	}

	private String niceAppName(SpringBootApp app) {
		return "Process [PID="+app.getProcessID()+", name=`"+app.getProcessName()+"`]";
	}

}
