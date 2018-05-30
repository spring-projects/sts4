/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.javadoc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.JavadocParams;
import org.springframework.ide.vscode.commons.languageserver.JavadocResponse;
import org.springframework.ide.vscode.commons.languageserver.STS4LanguageClient;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;

public class JdtLsJavadocProvider implements IJavadocProvider {

	private static final Logger log = LoggerFactory.getLogger(JdtLsJavadocProvider.class);

	private STS4LanguageClient client;
	private String projectUri;

	public JdtLsJavadocProvider(STS4LanguageClient client, String projectUri) {
		super();
		this.client = client;
		this.projectUri = projectUri;
	}

	private IJavadoc produceJavadocFromMd(JavadocResponse response) {
		String md = response.getContent();
		if (md != null) {
			final Renderable renderableDoc = Renderables.mdBlob(md);
			return new IJavadoc() {

				@Override
				public String raw() {
					throw new UnsupportedOperationException("Raw content unavailable");
				}

				@Override
				public Renderable getRenderable() {
					return renderableDoc;
				}

			};
		}
		return null;
	}

	private IJavadoc javadoc(IJavaElement element) {
		try {
			JavadocResponse response = client.javadoc(new JavadocParams(projectUri, element.getBindingKey())).get(10, TimeUnit.SECONDS);
			return produceJavadocFromMd(response);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public IJavadoc getJavadoc(IType type) {
		return javadoc(type);
	}

	@Override
	public IJavadoc getJavadoc(IField field) {
		return javadoc(field);
	}

	@Override
	public IJavadoc getJavadoc(IMethod method) {
		return javadoc(method);
	}

	@Override
	public IJavadoc getJavadoc(IAnnotation annotation) {
		return javadoc(annotation);
	}

}
