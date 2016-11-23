package org.springframework.ide.vscode.commons.jandex;

import java.util.stream.Stream;

import org.jboss.jandex.MethodInfo;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public class MethodImpl implements IMethod {
	
	private static final String JANDEX_CONTRUCTOR_NAME = "<init>";

	
	private JandexIndex index;
	private MethodInfo method;
	private IJavadocProvider javadocProvider;
	
	MethodImpl(JandexIndex index, MethodInfo method, IJavadocProvider javadocProvider) {
		this.index = index;
		this.method = method;
		this.javadocProvider =javadocProvider;
	}

	@Override
	public int getFlags() {
		return method.flags();
	}
	
	@Override
	public boolean isConstructor() {
		return method.name().equals(JANDEX_CONTRUCTOR_NAME);
	}

	@Override
	public IType getDeclaringType() {
		return Wrappers.wrap(index, method.declaringClass(), javadocProvider);
	}

	@Override
	public String getElementName() {
		return isConstructor() ? getDeclaringType().getElementName() : method.name();
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
		return method.annotations().stream().map(a -> Wrappers.wrap(a, javadocProvider));
	}

	@Override
	public IJavaType getReturnType() {
		return Wrappers.wrap(method.returnType());
	}

//	@Override
//	public String getSignature() {
//		StringBuilder sb = new StringBuilder();
//		sb.append('(');
//		method.parameters().forEach(p -> sb.append(signature(p)));
//		sb.append(')');
//		sb.append(getReturnType());
//		return sb.toString();
//	}
	
	@Override
	public String toString() {
		return method.toString();
	}

	@Override
	public Stream<IJavaType> parameters() {
		return method.parameters().stream().map(Wrappers::wrap);
	}

	@Override
	public int hashCode() {
		return method.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodImpl) {
			return method.toString().equals(((MethodImpl)obj).method.toString());
		}
		return super.equals(obj);
	}


}
