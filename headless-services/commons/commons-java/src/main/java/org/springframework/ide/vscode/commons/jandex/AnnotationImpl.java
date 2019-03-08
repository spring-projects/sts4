/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMemberValuePair;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public class AnnotationImpl implements IAnnotation {

	private AnnotationInstance annotation;
	private IJavadocProvider javadocProvider;

	AnnotationImpl(AnnotationInstance annotation, IJavadocProvider javadocProvider) {
		this.annotation = annotation;
		this.javadocProvider = javadocProvider;
	}

	@Override
	public String getElementName() {
		return fqName();
	}

	@Override
	public IJavadoc getJavaDoc() {
		return javadocProvider == null ? null : javadocProvider.getJavadoc(this);
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public Stream<IMemberValuePair> getMemberValuePairs() {
		return annotation.values().stream().map(av -> {
			return Wrappers.wrap(av);
		});
	}

	@Override
	public String toString() {
		return annotation.toString();
	}

	@Override
	public int hashCode() {
		return annotation.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnnotationImpl) {
			return annotation.toString().equals(((AnnotationImpl)obj).annotation.toString());
		}
		return super.equals(obj);
	}

	@Override
	public String getBindingKey() {
		return BindingKeyUtils.getBindingKey(annotation);
	}

	@Override
	public String fqName() {
		return annotation.name().toString();
	}

}
