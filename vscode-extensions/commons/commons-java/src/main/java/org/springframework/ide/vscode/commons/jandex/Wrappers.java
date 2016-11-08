package org.springframework.ide.vscode.commons.jandex;

import static org.springframework.ide.vscode.commons.util.Assert.isNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IMemberValuePair;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IPrimitiveType;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.IVoidType;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;

public class Wrappers {
	
	private static final int AccEnum = 0x4000;
	
	public static IType wrap(IndexView index, ClassInfo info) {
		if (info == null) {
			return null;
		}
		return new IType() {

			@Override
			public int getFlags() {
				return info.flags();
			}

			@Override
			public IType getDeclaringType() {
				DotName enclosingClass = info.enclosingClass();
				return enclosingClass == null ? null : wrap(index, index.getClassByName(enclosingClass));
			}

			@Override
			public String getElementName() {
				return info.simpleName();
			}

			@Override
			public HtmlSnippet getJavaDoc() {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				// TODO: check correctness!
				return info.annotations().get(info.name()).stream().map(Wrappers::wrap);
			}

			@Override
			public boolean isClass() {
				return true;
			}

			@Override
			public boolean isEnum() {
				return (info.flags() & AccEnum) != 0;
			}

			@Override
			public boolean isInterface() {
				return false;
			}

			@Override
			public String getFullyQualifiedName() {
				return info.name().toString();
			}

			@Override
			public IField getField(String name) {
				return wrap(index, info.field(name));
			}

			@Override
			public Stream<IField> getFields() {
				return info.fields().stream().map(f -> {
					return wrap(index, f);
				});
			}

			@Override
			public IMethod getMethod(String name, Stream<IJavaType> parameters) {
				List<Type> typeParameters = parameters.map(Wrappers::from).collect(Collectors.toList());
				return wrap(index, info.method(name, typeParameters.toArray(new Type[typeParameters.size()])));
			}

			@Override
			public Stream<IMethod> getMethods() {
				return info.methods().stream().map(m -> {
					return wrap(index, m);
				});
			}

			@Override
			public String toString() {
				return info.toString();
			}
						
		};
	}
	
	public static IField wrap(IndexView index, FieldInfo field) {
		if (field == null) {
			return null;
		}
		return new IField() {

			@Override
			public int getFlags() {
				return field.flags();
			}

			@Override
			public IType getDeclaringType() {
				return wrap(index, field.declaringClass());
			}

			@Override
			public String getElementName() {
				return field.name();
			}

			@Override
			public HtmlSnippet getJavaDoc() {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				return field.annotations().stream().map(a -> {
					return wrap(a);
				});
			}

			@Override
			public boolean isEnumConstant() {
				return (field.flags() & AccEnum) != 0;
			}
			
			@Override
			public String toString() {
				return field.toString();
			}
		};
	}

	public static IMethod wrap(IndexView index, MethodInfo method) {
		isNotNull(index);
		isNotNull(method);
		return new IMethod() {

			@Override
			public int getFlags() {
				return method.flags();
			}

			@Override
			public IType getDeclaringType() {
				return wrap(index, method.declaringClass());
			}

			@Override
			public String getElementName() {
				return method.name();
			}

			@Override
			public HtmlSnippet getJavaDoc() {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IAnnotation> getAnnotations() {
				return method.annotations().stream().map(Wrappers::wrap);
			}

			@Override
			public IJavaType getReturnType() {
				return wrap(method.returnType());
			}

//			@Override
//			public String getSignature() {
//				StringBuilder sb = new StringBuilder();
//				sb.append('(');
//				method.parameters().forEach(p -> sb.append(signature(p)));
//				sb.append(')');
//				sb.append(getReturnType());
//				return sb.toString();
//			}
			
			@Override
			public String toString() {
				return method.toString();
			}

			@Override
			public Stream<IJavaType> parameters() {
				return method.parameters().stream().map(Wrappers::wrap);
			}
		};
	}
	
	public static IAnnotation wrap(AnnotationInstance annotation) {
		isNotNull(annotation);
		return new IAnnotation() {

			@Override
			public String getElementName() {
				return annotation.name().toString();
			}

			@Override
			public HtmlSnippet getJavaDoc() {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Stream<IMemberValuePair> getMemberValuePairs() {
				return annotation.values().stream().map(av -> {
					return wrap(av);
				});
			}
			
			@Override
			public String toString() {
				return annotation.toString();
			}
		};
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
	
	public static Type createParameterTypeFromSignature(IndexView index, String signature) {
		if (signature == null) {
			return null;
		}
		throw new UnsupportedOperationException("Not yet implemented");
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
	private static Type from(IJavaType type) {
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
	
}
