/*******************************************************************************
 * Copyright (c) 2016-2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.jandex;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jboss.jandex.FieldInfo;
import org.springframework.ide.vscode.commons.java.Flags;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

class FieldImpl implements IField {

	private FieldInfo field;
	private IJavadocProvider javadocProvider;
	private IType declaringType;

	FieldImpl(IType declaringType, FieldInfo field, IJavadocProvider javadocProvider) {
		this.declaringType = declaringType;
		this.field = field;
		this.javadocProvider = javadocProvider;
	}

	@Override
	public int getFlags() {
		return field.flags();
	}

	@Override
	public IType getDeclaringType() {
		return declaringType;
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

	@Override
	public String getBindingKey() {
		return BindingKeyUtils.getBindingKey(field);
	}

	@Override
	public IJavaType type() {
		return Wrappers.wrap(field.type());
	}

	@Override
	public String signature() {
		String jandexSignature = field.toString();
		int typeEndIdx = jandexSignature.indexOf(' ');
		if (typeEndIdx < 0) {
			return jandexSignature;
		} else {
			if (isEnumConstant()) {
				// Chop off field type completely for enum constant
				return jandexSignature.substring(typeEndIdx + 1);
			} else {
				// Chop off prefix of the FQ name of the field type
				return Pattern.compile(field.type().name().toString(), Pattern.LITERAL).matcher(jandexSignature).replaceFirst(Wrappers.simpleName(field.type().name()));
			}
		}
	}

}
