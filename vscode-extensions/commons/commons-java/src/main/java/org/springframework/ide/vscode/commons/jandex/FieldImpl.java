/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

import java.util.stream.Stream;

import org.jboss.jandex.FieldInfo;
import org.springframework.ide.vscode.commons.java.Flags;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

class FieldImpl implements IField {
	
	private JandexIndex index;
	private FieldInfo field;
	private IJavadocProvider javadocProvider;
	
	FieldImpl(JandexIndex index, FieldInfo field, IJavadocProvider javadocProvider) {
		this.index = index;
		this.field = field;
		this.javadocProvider = javadocProvider;
	}

	@Override
	public int getFlags() {
		return field.flags();
	}

	@Override
	public IType getDeclaringType() {
		return Wrappers.wrap(index, field.declaringClass(), javadocProvider);
	}

	@Override
	public String getElementName() {
		return field.name();
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
	public Stream<IAnnotation> getAnnotations() {
		return field.annotations().stream().map(a -> {
			return Wrappers.wrap(a, javadocProvider);
		});
	}

	@Override
	public boolean isEnumConstant() {
		return Flags.isEnum(field.flags());
	}
	
	@Override
	public String toString() {
		return field.toString();
	}

	@Override
	public int hashCode() {
		return field.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldImpl) {
			return field.toString().equals(((FieldImpl)obj).field.toString());
		}
		return super.equals(obj);
	}
	
	

}
