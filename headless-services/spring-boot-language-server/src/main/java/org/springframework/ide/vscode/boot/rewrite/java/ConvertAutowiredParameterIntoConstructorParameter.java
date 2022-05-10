/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.rewrite.java;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.RemoveAnnotationVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Block;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.Empty;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.rewrite.ORAstUtils;

public class ConvertAutowiredParameterIntoConstructorParameter extends Recipe {

	private String classFqName;
	private String fieldName;

	public ConvertAutowiredParameterIntoConstructorParameter(String classFqName, String fieldName) {
		super();
		this.classFqName = classFqName;
		this.fieldName = fieldName;
	}

	@Override
	public String getDisplayName() {
		return "Convert autowired field into constructor parameter";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaVisitor<ExecutionContext>() {

			@Override
			public J visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				if (classFqName.equals(classDecl.getType().getFullyQualifiedName())) {
					return super.visitClassDeclaration(classDecl, p);
				}
				return classDecl;
			}

			@Override
			public J visitVariableDeclarations(VariableDeclarations multiVariable, ExecutionContext p) {
				Cursor blockCursor = getCursor().dropParentUntil(Block.class::isInstance);
				VariableDeclarations mv = multiVariable;
				if (blockCursor != null && blockCursor.getParent().getValue() instanceof ClassDeclaration
						&& multiVariable.getVariables().size() == 1
						&& fieldName.equals(multiVariable.getVariables().get(0).getName().printTrimmed())) {
					
					mv = (VariableDeclarations) new RemoveAnnotationVisitor(new AnnotationMatcher("@" + Annotations.AUTOWIRED)).visit(multiVariable, p);
					doAfterVisit(new AddContructorParameterVisitor(classFqName, fieldName, multiVariable.getTypeExpression()));
				}
				return mv;
			}

		};
	}
	
	private static class AddContructorParameterVisitor extends JavaVisitor<ExecutionContext> {
		
		private String classFqName;
		private String fieldName;
		private TypeTree type;
		
		public AddContructorParameterVisitor(String classFqName, String fieldName, TypeTree type) {
			super();
			this.classFqName = classFqName;
			this.fieldName = fieldName;
			this.type = type;
		}

		@Override
		public J visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
			ClassDeclaration c = classDecl;
			if (classFqName.equals(c.getType().getFullyQualifiedName())) {
				List<MethodDeclaration> constructors = ORAstUtils.getMethods(c).stream().filter(m -> m.isConstructor()).collect(Collectors.toList());
				if (constructors.isEmpty()) {
					doAfterVisit(new AddConstructorVisitor(c.getSimpleName(), fieldName, type));
				} else {
					Optional<MethodDeclaration> autowiredConstructor = constructors.stream().filter(constr -> constr.getLeadingAnnotations().stream()
							.map(a -> TypeUtils.asFullyQualified(a.getType()))
							.filter(Objects::nonNull)
							.map(fq -> fq.getFullyQualifiedName())
							.filter(fqn -> Annotations.AUTOWIRED.equals(fqn))
							.findFirst()
							.isPresent()
						)
						.findFirst();
					if (autowiredConstructor.isPresent()) {
						// Autowired constructor found - add argument to it
						doAfterVisit(new AddMethodParameter(autowiredConstructor.get(), fieldName, type));
					} else {
						if (constructors.size() == 1) {
							doAfterVisit(new AddMethodParameter(constructors.get(0), fieldName, type));
						}
					}
				}
			}
			return c;
		}
		
	}
	
	private static class AddConstructorVisitor extends JavaVisitor<ExecutionContext> {
		private String className;
		private String fieldName;
		private TypeTree type;
		
		public AddConstructorVisitor(String className, String fieldName, TypeTree type) {
			this.className = className;
			this.fieldName = fieldName;
			this.type = type;
		}

		@Override
		public J visitBlock(Block block, ExecutionContext p) {
			if (getCursor().getParent() != null) {
				Object n = getCursor().getParent().getValue();
				if (n instanceof ClassDeclaration) {
					ClassDeclaration classDecl = (ClassDeclaration) n;
					if (classDecl.getKind() == ClassDeclaration.Kind.Type.Class && className.equals(classDecl.getSimpleName())) {
						JavaTemplate.Builder template = JavaTemplate.builder(() -> getCursor(), ""
								+ classDecl.getSimpleName() + "(" + type.printTrimmed() + " " + fieldName + ") {\n"
								+ "this." + fieldName + " = " + fieldName + ";\n"
								+ "}\n"
						);
						FullyQualified fq = TypeUtils.asFullyQualified(type.getType());
						if (fq != null) {
							template.imports(fq.getFullyQualifiedName());
							maybeAddImport(fq);
						}
						Optional<Statement> firstMethod = block.getStatements().stream().filter(MethodDeclaration.class::isInstance).findFirst();
						if (firstMethod.isPresent()) {
							return block.withTemplate(template.build(), firstMethod.get().getCoordinates().before());
						} else {
							return block.withTemplate(template.build(), block.getCoordinates().lastStatement());
						}
					}
				}
			}
			return block;
		}
	}
	
	private static class AddMethodParameter extends JavaIsoVisitor<ExecutionContext> {
		
		private MethodDeclaration method;
		private String fieldName;
		private TypeTree type;
		
		public AddMethodParameter(MethodDeclaration method, String fieldName, TypeTree type) {
			this.method = method;
			this.fieldName = fieldName;
			this.type = type;
		}

		@Override
		public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext p) {
			if (method == this.method) {
				String paramsStr = Stream.concat(method.getParameters().stream().filter(s -> !Empty.class.isInstance(s)).map(s -> s.printTrimmed()), Stream.of(type.printTrimmed() + " " + fieldName)).collect(Collectors.joining(", "));
				JavaTemplate.Builder paramsTemplate = JavaTemplate.builder(() -> getCursor(), paramsStr);
				JavaTemplate.Builder statementTemplate = JavaTemplate.builder(() -> getCursor(), "this." + fieldName + " = " + fieldName + ";\n");
				return method
					.withTemplate(paramsTemplate.build(), method.getCoordinates().replaceParameters())
					.withTemplate(statementTemplate.build(), method.getBody().getCoordinates().lastStatement());
			}
			return method;
		}
		
		

	}

}