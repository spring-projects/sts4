package org.springframework.ide.vscode.commons.jandex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.springframework.ide.vscode.commons.java.Flags;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

class TypeImpl implements IType {
	
	private ClassInfo info;
	private JandexIndex index;
	private IJavadocProvider javadocProvider;
	
	TypeImpl(JandexIndex index, ClassInfo info, IJavadocProvider javadocProvider) {
		this.info = info;
		this.index = index;
		this.javadocProvider = javadocProvider;
	}

	@Override
	public int getFlags() {
		return info.flags();
	}

	@Override
	public IType getDeclaringType() {
		DotName enclosingClass = info.enclosingClass();
		return enclosingClass == null ? null : index.getClassByName(enclosingClass);
	}

	@Override
	public String getElementName() {
		return info.simpleName() == null ? info.name().local() : info.simpleName();
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
		// TODO: check correctness!
		List<AnnotationInstance> annotations = info.annotations().get(info.name());
		return annotations == null ? Stream.empty() : annotations.stream().map(a -> Wrappers.wrap(a, javadocProvider));
	}

	@Override
	public boolean isClass() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return Flags.isEnum(info.flags());
	}

	@Override
	public boolean isInterface() {
		return Flags.isInterface(info.flags());
	}

	@Override
	public boolean isAnnotation() {
		return Flags.isAnnotation(info.flags());
	}
				
	@Override
	public String getFullyQualifiedName() {
		return info.name().toString();
	}

	@Override
	public IField getField(String name) {
		return Wrappers.wrap(index, info.field(name), javadocProvider);
	}

	@Override
	public Stream<IField> getFields() {
		return info.fields().stream().map(f -> {
			return Wrappers.wrap(index, f, javadocProvider);
		});
	}

	@Override
	public IMethod getMethod(String name, Stream<IJavaType> parameters) {
		List<Type> typeParameters = parameters.map(Wrappers::from).collect(Collectors.toList());
		return Wrappers.wrap(index, info.method(name, typeParameters.toArray(new Type[typeParameters.size()])), javadocProvider);
	}

	@Override
	public Stream<IMethod> getMethods() {
		return info.methods().stream().map(m -> {
			return Wrappers.wrap(index, m, javadocProvider);
		});
	}

	@Override
	public String toString() {
		return info.toString();
	}

	@Override
	public int hashCode() {
		return info.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeImpl) {
			return info.equals(((TypeImpl)obj).info);
		}
		return super.equals(obj);
	}

	
}
