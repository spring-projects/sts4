package org.springframework.ide.vscode.commons.java.roaster;

import org.jboss.forge.roaster.model.Field;
import org.jboss.forge.roaster.model.FieldHolder;
import org.jboss.forge.roaster.model.JavaDocCapable;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.JavaUnit;
import org.jboss.forge.roaster.model.Method;
import org.jboss.forge.roaster.model.MethodHolder;
import org.jboss.forge.roaster.model.TypeHolder;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProvider;
import org.springframework.ide.vscode.commons.util.Log;

public class RoasterJavadocProvider implements IJavadocProvider {
	
	private SourceUrlProvider sourceUrlProvider;

	public RoasterJavadocProvider(SourceUrlProvider sourceUrlProvider) {
		this.sourceUrlProvider = sourceUrlProvider;
	}

	@Override
	public IJavadoc getJavadoc(IType type) {
		try {
			JavaClassSource declaration = (JavaClassSource) getDeclaration(type);
			return new RoasterJavadoc(declaration.getJavaDoc());
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	@Override
	public IJavadoc getJavadoc(IField field) {
		try {
			JavaType<?> typeDeclaration = getDeclaration(field.getDeclaringType());
			if (typeDeclaration instanceof FieldHolder) {
				Field<?> fieldDeclaration = ((FieldHolder<?>)typeDeclaration).getField(field.getElementName());
				if (fieldDeclaration instanceof JavaDocCapable) {
					return new RoasterJavadoc(((JavaDocCapable<?>)fieldDeclaration).getJavaDoc());
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}			
		return null;
	}

	@Override
	public IJavadoc getJavadoc(IMethod method) {
		if (method.parameters().findFirst().isPresent()) {
			throw new UnsupportedOperationException("Only methods with no parameters are supported");
		}
		try {
			JavaType<?> typeDeclaration = getDeclaration(method.getDeclaringType());
			if (typeDeclaration instanceof MethodHolder) {
				Method<?, ?> methodDeclaration = ((MethodHolder<?>)typeDeclaration).getMethod(method.getElementName());
				if (methodDeclaration instanceof JavaDocCapable) {
					return new RoasterJavadoc(((JavaDocCapable<?>)methodDeclaration).getJavaDoc());
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}			
		return null;
	}

	@Override
	public IJavadoc getJavadoc(IAnnotation annotation) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private JavaUnit getJavaUnit(IType type) throws Exception {
		return JavaUnitIndex.DEFAULT.getJavaUnit(sourceUrlProvider.sourceUrl(type));
	}
	
	private JavaType<?> getDeclaration(IType type) throws Exception {
		if (type == null) {
			return null;
		}
		IType declaringType = type.getDeclaringType();
		if (declaringType == null) {
			JavaUnit ju = getJavaUnit(type);
			return ju.getTopLevelTypes().stream().filter(jt -> jt.getName().equals(type.getElementName())).findFirst().orElse(null);
		} else {
			JavaType<?> declaringTypeDeclaration = getDeclaration(declaringType);
			if (declaringTypeDeclaration instanceof TypeHolder) {
				return ((TypeHolder<?>)declaringTypeDeclaration).getNestedType(type.getElementName());
			} else {
				return null;
			}
		}
	}

}
