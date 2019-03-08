/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.javadoc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.lsp4j.MarkupContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.protocol.STS4LanguageClient;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
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

	private IJavadoc produceJavadocFromMd(String md) {
		if (md != null) {
			final Renderable renderableDoc = Renderables.mdBlob(md);
			return new IJavadoc() {

				@Override
				public Renderable getRenderable() {
					return renderableDoc;
				}

			};
		}
		return null;
	}

	private IJavadoc javadoc(IJavaElement element) {
		long start = System.currentTimeMillis();
		try {
			log.info("Fetching javadoc {}", element.getBindingKey());
			MarkupContent md = client.javadoc(new JavaDataParams(projectUri, element.getBindingKey(), false)).get(10, TimeUnit.SECONDS);
			log.info("Fetching javadoc {} took {} ms", element.getBindingKey(), System.currentTimeMillis()-start);
			return produceJavadocFromMd(md == null ? null : md.getValue());
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("Fetching javadoc {} failed", element.getBindingKey(), e);
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
