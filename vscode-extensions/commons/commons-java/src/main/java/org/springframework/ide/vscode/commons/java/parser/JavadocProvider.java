package org.springframework.ide.vscode.commons.java.parser;

import java.net.URL;
import java.util.Optional;

import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.javadoc.RawJavadoc;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProvider;
import org.springframework.ide.vscode.commons.util.Log;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class JavadocProvider implements IJavadocProvider {
	
	private SourceUrlProvider sourceUrlProvider;
	
	public JavadocProvider(SourceUrlProvider sourceUrlProvider) {
		this.sourceUrlProvider = sourceUrlProvider;
	}
	
	public IJavadoc getJavadoc(IType type) {
		if (type.isEnum()) {
			EnumDeclaration declaration = getEnumDeclaration(type);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		} else {
			ClassOrInterfaceDeclaration declaration = getClassOrInterfaceDeclaration(type);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		}
	}
	
	public IJavadoc getJavadoc(IField field) {
		IType declaringType = field.getDeclaringType();
		if (declaringType.isEnum()) {
			FieldDeclaration declaration = createVisitorToFindField(field).visit(getEnumDeclaration(declaringType), null);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		} else {
			FieldDeclaration declaration = createVisitorToFindField(field).visit(getClassOrInterfaceDeclaration(declaringType), null);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		}
	}
	
	public IJavadoc getJavadoc(IMethod method) {
		if (method.parameters().findFirst().isPresent()) {
			throw new UnsupportedOperationException("Only methods with no parameters are supported");
		}
		IType declaringType = method.getDeclaringType();
		if (declaringType.isEnum()) {
			MethodDeclaration declaration = createVisitorToFindMethod(method).visit(getEnumDeclaration(declaringType), null);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		} else {
			MethodDeclaration declaration = createVisitorToFindMethod(method).visit(getClassOrInterfaceDeclaration(declaringType), null);
			return declaration.getJavaDoc() == null ? null : new RawJavadoc(declaration.getJavaDoc().toString());
		}
	}
	
	public IJavadoc getJavadoc(IAnnotation annotation) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private CompilationUnit getCompilationUnit(IType type) {
		try {
			URL sourceUrl = sourceUrlProvider.sourceUrl(type);
			return CompilationUnitIndex.DEFAULT.getCompilationUnit(sourceUrl);
		} catch (Exception e) {
			Log.log("Invalid source URL for type " + type, e);
			return null;
		}
	}
	
	private EnumDeclaration getEnumDeclaration(IType type) {
		IType parent = type.getDeclaringType();
		if (parent == null) {
			CompilationUnit cu = getCompilationUnit(type);
			return createVisitorToFindEnum(type).visit(cu, null);
		} else {
			if (parent.isEnum()) {
				EnumDeclaration declaration = getEnumDeclaration(parent);
				return createVisitorToFindEnum(type).visit(declaration, null);
			} else {
				ClassOrInterfaceDeclaration declaration = getClassOrInterfaceDeclaration(parent);
				return createVisitorToFindEnum(type).visit(declaration, null);
			}
		}
	}
	
	private ClassOrInterfaceDeclaration getClassOrInterfaceDeclaration(IType type) {
		IType parent = type.getDeclaringType();
		if (parent == null) {
			CompilationUnit cu = getCompilationUnit(type);
			return createVisitorToFindClassOrInterface(type).visit(cu, null);
		} else {
			if (parent.isEnum()) {
				EnumDeclaration declaration = getEnumDeclaration(parent);
				return createVisitorToFindClassOrInterface(type).visit(declaration, null);
			} else {
				ClassOrInterfaceDeclaration declaration = getClassOrInterfaceDeclaration(parent);
				return createVisitorToFindClassOrInterface(type).visit(declaration, null);
			}
		}
	}

	private GenericVisitorAdapter<ClassOrInterfaceDeclaration, Object> createVisitorToFindClassOrInterface(IType type) {
		return new GenericVisitorAdapter<ClassOrInterfaceDeclaration, Object>() {

			@Override
			public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration n, Object arg) {
				if (n.getName().equals(type.getElementName())) {
					return n;
				} else {
					return super.visit(n, arg);
				}
			}
			
		};
	}
	
	private GenericVisitorAdapter<EnumDeclaration, Object> createVisitorToFindEnum(IType type) {
		return new GenericVisitorAdapter<EnumDeclaration, Object>() {

			@Override
			public EnumDeclaration visit(EnumDeclaration n, Object arg) {
				if (n.getName().equals(type.getElementName())) {
					return n;
				} else {
					return super.visit(n, arg);
				}
			}
			
		};
	}
	
	private GenericVisitorAdapter<MethodDeclaration, Object> createVisitorToFindMethod(IMethod method) {
		return new GenericVisitorAdapter<MethodDeclaration, Object>() {
			@Override
			public MethodDeclaration visit(MethodDeclaration n, Object arg) {
				if (n.getParameters().isEmpty() && n.getName().equals(method.getElementName())) {
					return n;
				}
				return null;
			}
		};
	}
	
	private GenericVisitorAdapter<FieldDeclaration, Object> createVisitorToFindField(IField field) {
		return new GenericVisitorAdapter<FieldDeclaration, Object>() {
			@Override
			public FieldDeclaration visit(FieldDeclaration n, Object arg) {
				Optional<VariableDeclarator> variable = n.getVariables().stream().filter(v -> v.getId().getName().equals(field.getElementName())).findFirst();
				return variable.isPresent() ? n : null;
			}
		};
	}
	
}
