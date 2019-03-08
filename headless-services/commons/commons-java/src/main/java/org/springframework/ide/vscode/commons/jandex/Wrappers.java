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

package org.springframework.ide.vscode.commons.jandex;

import static org.springframework.ide.vscode.commons.util.Assert.isNotNull;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMemberValuePair;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IPrimitiveType;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.IVoidType;

public class Wrappers {

	public static IType wrap(JandexIndex index, IJavaModuleData moduleContainer, ClassInfo info, IJavadocProvider javadocProvider) {
		if (info == null) {
			return null;
		}
		return new TypeImpl(index, moduleContainer, info, javadocProvider);
	}

	public static IField wrap(IType declaringType, FieldInfo field, IJavadocProvider javadocProvider) {
		if (field == null) {
			return null;
		}
		return new FieldImpl(declaringType, field, javadocProvider);
	}

	public static IMethod wrap(IType declaringType, MethodInfo method, IJavadocProvider javadocProvider) {
		isNotNull(declaringType);
		isNotNull(method);
		return new MethodImpl(declaringType, method, javadocProvider);
	}

	public static IAnnotation wrap(AnnotationInstance annotation, IJavadocProvider javadocProvider) {
		isNotNull(annotation);
		return new AnnotationImpl(annotation, javadocProvider);
	}

	public static IMemberValuePair wrap(AnnotationValue annotationValue) {
		if (annotationValue == null) {
			return null;
		}
		return new IMemberValuePair() {

			@Override
			public String getMemberName() {
				return annotationValue.name();
			}

			@Override
			public Object getValue() {
				return annotationValue.value();
			}

			@Override
			public String toString() {
				return annotationValue.toString();
			}
		};
	}

	public static IPrimitiveType wrap(PrimitiveType type) {
		switch (type.primitive()) {
		case SHORT:
			return IPrimitiveType.SHORT;
		case LONG:
			return IPrimitiveType.LONG;
		case BYTE:
			return IPrimitiveType.BYTE;
		case DOUBLE:
			return IPrimitiveType.DOUBLE;
		case BOOLEAN:
			return IPrimitiveType.BOOLEAN;
		case CHAR:
			return IPrimitiveType.CHAR;
		case FLOAT:
			return IPrimitiveType.FLOAT;
		case INT:
			return IPrimitiveType.INT;
		}
		throw new IllegalArgumentException("Invalid Java primitive type! " + type.toString());
	}

	@SuppressWarnings("unchecked")
	static Type from(IJavaType type) {
		if (type == IPrimitiveType.BOOLEAN) {
			return PrimitiveType.BOOLEAN;
		} else if (type == IPrimitiveType.BYTE) {
			return PrimitiveType.BYTE;
		} else if (type == IPrimitiveType.CHAR) {
			return PrimitiveType.CHAR;
		} else if (type == IPrimitiveType.DOUBLE) {
			return PrimitiveType.DOUBLE;
		} else if (type == IPrimitiveType.FLOAT) {
			return PrimitiveType.FLOAT;
		} else if (type == IPrimitiveType.INT) {
			return PrimitiveType.INT;
		} else if (type == IPrimitiveType.LONG) {
			return PrimitiveType.LONG;
		} else if (type == IPrimitiveType.SHORT) {
			return PrimitiveType.SHORT;
		} else if (type == IVoidType.DEFAULT) {
			return Type.create(null, Kind.VOID);
		} else if (type instanceof TypeWrapper) {
			return ((TypeWrapper<Type>)type).getType();
		}
		throw new IllegalArgumentException("Not a Jandex wrapped typed!");
	}

	public static IJavaType wrap(Type type) {
		switch (type.kind()) {
		case ARRAY:
			return new ArrayTypeWrapper(type.asArrayType());
		case CLASS:
			return new ClassTypeWrapper(type.asClassType());
		case PARAMETERIZED_TYPE:
			return new ParameterizedTypeWrapper(type.asParameterizedType());
		case PRIMITIVE:
			return wrap(type.asPrimitiveType());
		case TYPE_VARIABLE:
			return new TypeVariableWrapper(type.asTypeVariable());
		case UNRESOLVED_TYPE_VARIABLE:
			return new UnresolvedTypeVariableWrapper(type.asUnresolvedTypeVariable());
		case VOID:
			return IVoidType.DEFAULT;
		case WILDCARD_TYPE:
			return new WildcardTypeWrapper(type.asWildcardType());
		}
		throw new IllegalArgumentException("Invalid Java Type " + type.toString());
	}

	public static String simpleName(DotName dotName) {
		String fqName = dotName.toString();
		int idx = fqName.lastIndexOf('.');
		return idx < 0 ? fqName : fqName.substring(idx + 1);
	}

}
