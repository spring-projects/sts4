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
package org.springframework.ide.vscode.boot.java.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.autowired.Constants;
import org.springframework.ide.vscode.boot.java.autowired.LiveBean;
import org.springframework.ide.vscode.boot.java.autowired.LiveBeansModel;
import org.springframework.ide.vscode.boot.java.autowired.SpringBootAppProvider;
import org.springframework.ide.vscode.boot.java.autowired.SpringBootAppProviderImpl;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class ComponentHoverProvider implements HoverProvider {

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
			Range range = null;

			TypeDeclaration typeDecl = findDeclaredType(annotation);
			if (typeDecl != null) {
				MethodDeclaration constructor = findConstructor(typeDecl);

				if (constructor != null && !hasAutowiredAnnotation(constructor)) {
					range = doc.toRange(constructor.getName().getStartPosition(), constructor.getName().getLength());

					List<Either<String, MarkedString>> hoverContent = new ArrayList<>();
					for (SpringBootAppProvider bootApp : runningApps) {
						try {
							String liveBeans = bootApp.getBeans();
							if (liveBeans != null && liveBeans.length() > 0) {
								addLiveHoverContent(typeDecl, doc, liveBeans, bootApp, hoverContent);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (hoverContent.size() > 0) {
						Hover hover = new Hover();

						hover.setContents(hoverContent);
						hover.setRange(range);

						return CompletableFuture.completedFuture(hover);
					}
				}
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
					String liveBeans = bootApp.getBeans();
					if (liveBeans != null && liveBeans.length() > 0) {
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

	public Range getLiveHoverHint(Annotation annotation, TextDocument doc, String liveBeansJSON) {
		try {
			TypeDeclaration type = findDeclaredType(annotation);
			if (type != null && liveBeansJSON != null) {
				String typeName = type.resolveBinding().getQualifiedName();

				LiveBeansModel beansModel = LiveBeansModel.parse(liveBeansJSON);
				LiveBean[] beansOfType = beansModel.getBeansOfType(typeName);

				if (beansOfType.length > 0) {
					MethodDeclaration constructor = findConstructor(type);
					if (constructor != null && !hasAutowiredAnnotation(constructor)) {
						Range hoverRange = doc.toRange(constructor.getName().getStartPosition(), constructor.getName().getLength());
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

	private boolean hasAutowiredAnnotation(MethodDeclaration constructor) {
		List<?> modifiers = constructor.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof MarkerAnnotation) {
				ITypeBinding typeBinding = ((MarkerAnnotation) modifier).resolveTypeBinding();
				if (typeBinding != null && typeBinding.getQualifiedName().equals(Constants.SPRING_AUTOWIRED)) {
					return true;
				}
			}
		}
		return false;
	}


	public void addLiveHoverContent(TypeDeclaration declaringType, TextDocument doc, String liveBeansJSON, SpringBootAppProvider bootApp, List<Either<String, MarkedString>> hoverContent) {
		String type = declaringType.resolveBinding().getQualifiedName();
		if (type != null && liveBeansJSON != null) {

			LiveBeansModel beansModel = LiveBeansModel.parse(liveBeansJSON);
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

	private TypeDeclaration findDeclaredType(Annotation annotation) {
		ASTNode node = annotation;
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}

		return node != null ? (TypeDeclaration) node : null;
	}

	private MethodDeclaration findConstructor(TypeDeclaration typeDecl) {
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if (methodDeclaration.isConstructor()) {
				return methodDeclaration;
			}
		}

		return null;
	}

}
