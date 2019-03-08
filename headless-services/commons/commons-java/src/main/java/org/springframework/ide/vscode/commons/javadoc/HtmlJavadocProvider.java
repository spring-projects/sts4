/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;

import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.internal.JavadocContents;
import org.springframework.ide.vscode.commons.util.Log;

public class HtmlJavadocProvider implements IJavadocProvider {
	
	private SourceUrlProvider htmlUrlProvider;
	
	public HtmlJavadocProvider(SourceUrlProvider htmlUrlProvider) {
		this.htmlUrlProvider = htmlUrlProvider;
	}

	@Override
	public IJavadoc getJavadoc(IType type) {
		try {
			JavadocContents javadocContents = findHtml(type);
			String html = javadocContents == null ? null : javadocContents.getTypeDoc(type);
			return html == null ? null : new HtmlJavadoc(html);
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	@Override
	public IJavadoc getJavadoc(IField field) {
		try {
			IType declaringType = field.getDeclaringType();
			JavadocContents javadocContents = findHtml(declaringType);
			String html = javadocContents == null ? null : javadocContents.getFieldDoc(field);
			return html == null ? null : new HtmlJavadoc(html);
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	@Override
	public IJavadoc getJavadoc(IMethod method) {
		try {
			IType declaringType = method.getDeclaringType();
			JavadocContents javadocContents = findHtml(declaringType);
			String html = javadocContents == null ? null : javadocContents.getMethodDoc(method);
			return html == null ? null : new HtmlJavadoc(html);
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	@Override
	public IJavadoc getJavadoc(IAnnotation annotation) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private JavadocContents findHtml(IType type) throws Exception {
		URL url = htmlUrlProvider.sourceUrl(type);
		return HtmlJavadocIndex.DEFAULT.getHtmlJavadoc(url);
	}
	
}
