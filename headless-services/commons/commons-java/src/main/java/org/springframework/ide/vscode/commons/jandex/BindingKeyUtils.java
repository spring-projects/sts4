/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.jandex;

import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.UnresolvedTypeVariable;
import org.jboss.jandex.VoidType;
import org.jboss.jandex.WildcardType;

class BindingKeyUtils {

	public static String getBindingKey(ClassInfo info) {
		StringBuilder sb = new StringBuilder();
		sb.append('L');
		sb.append(info.toString().replace('.', '/'));
		sb.append(';');
		return sb.toString();
	}

	public static String getBindingKey(AnnotationInstance annotation) {
		return annotation.name().toString();
	}

	public static String getBindingKey(FieldInfo field) {
		StringBuilder sb = new StringBuilder();
		sb.append(getBindingKey(field.declaringClass()));
		sb.append('.');
		sb.append(field.name());
		sb.append(')');
		sb.append(getGeneralTypeBindingKey(field.type()));
		return sb.toString();
	}

	public static String getBindingKey(MethodInfo method) {
		StringBuilder sb = new StringBuilder();
		sb.append(getBindingKey(method.declaringClass()));
		sb.append('.');
		sb.append(method.name());
		sb.append('(');
		for (Type parameter : method.parameters()) {
			sb.append(getGeneralTypeBindingKey(parameter));
		}
		sb.append(')');
		sb.append(getGeneralTypeBindingKey(method.returnType()));
		return sb.toString();
	}

	public static String getGeneralTypeBindingKey(Type type) {
		switch (type.kind()) {
		case ARRAY:
			return getBindingKey(type.asArrayType());
		case CLASS:
			return getBindingKey(type.asClassType());
		case PARAMETERIZED_TYPE:
			return getBindingKey(type.asParameterizedType());
		case PRIMITIVE:
			return getBindingKey(type.asPrimitiveType());
		case TYPE_VARIABLE:
			return getBindingKey(type.asTypeVariable());
		case UNRESOLVED_TYPE_VARIABLE:
			return getBindingKey(type.asUnresolvedTypeVariable());
		case VOID:
			return getBindingKey(type.asVoidType());
		case WILDCARD_TYPE:
			return getBindingKey(type.asWildcardType());
		default:
			break;
		}
		return "";
	}

	private static String getBindingKey(WildcardType type) {
		if (type.extendsBound() != null) {
			return "+" + getGeneralTypeBindingKey(type.extendsBound());
		} else if (type.superBound() != null) {
			return "-" + getGeneralTypeBindingKey(type.superBound());
		} else {
			return "*";
		}
	}

	private static String getBindingKey(ParameterizedType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(type.owner() == null ? "L" + type.name().toString().replace('.', '/') : getGeneralTypeBindingKey(type.owner()));
		sb.append('<');
		for (Type argument : type.arguments()) {
			sb.append(getGeneralTypeBindingKey(argument));
		}
		sb.append('>');
		if (type.owner() == null) {
			sb.append(';');
		}
		return sb.toString();
	}

	private static String getBindingKey(TypeVariable type) {
		StringBuilder sb = new StringBuilder();
		sb.append('T');
		sb.append(type.identifier());
		sb.append(type.bounds().stream().map(BindingKeyUtils::getGeneralTypeBindingKey).collect(Collectors.joining(":")));
		sb.append(';');
		return sb.toString();
	}

	private static String getBindingKey(ArrayType type) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < type.dimensions(); i++) {
			sb.append('[');
		}
		sb.append(getGeneralTypeBindingKey(type.component()));
		return null;
	}

	private static String getBindingKey(ClassType type) {
		StringBuilder sb = new StringBuilder();
		sb.append('L');
		sb.append(type.name().toString().replace('.', '/'));
		sb.append(';');
		return sb.toString();
	}

	private static String getBindingKey(PrimitiveType primitive) {
        if (primitive == PrimitiveType.BYTE) {
            return "B";
        } else if (primitive == PrimitiveType.CHAR) {
            return "C";
        } else if (primitive == PrimitiveType.DOUBLE) {
            return "D";
        } else if (primitive == PrimitiveType.FLOAT) {
            return "F";
        } else if (primitive == PrimitiveType.INT) {
            return "I";
        } else if (primitive == PrimitiveType.LONG) {
            return "J";
        } else if (primitive == PrimitiveType.SHORT) {
            return "S";
        }

        // BOOLEAN
        return "Z";
	}

	private static String getBindingKey(UnresolvedTypeVariable type) {
		return "Q" + type.name().toString();
	}

	private static String getBindingKey(VoidType type) {
		return "V";
	}

}
