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
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.beans.ConfigBeanSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

public class NotRegisteredBeansProblem implements RecipeCodeActionDescriptor {
		
	private static final String DEFINE_METHOD_RECIPE = "org.springframework.ide.vscode.commons.rewrite.java.DefineMethod";
		
	private static final List<String> AOT_BEANS = List.of(
			"org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor",
			"org.springframework.beans.factory.aot.BeanRegistrationAotProcessor"
	);

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {
			
			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				FullyQualified type = c.getType();
				if (type != null) {
					String beanClassName = type.getFullyQualifiedName();
					boolean applicable = AOT_BEANS.stream().filter(fqName -> TypeUtils.isAssignableTo(fqName, type)).findFirst().isPresent();
					if (applicable) {
						SpringSymbolIndex index = applicationContext.getBean(SpringSymbolIndex.class);
						List<WorkspaceSymbol> beanSymbols = index.getSymbols(data -> {
							SymbolAddOnInformation[] additionalInformation = data.getAdditionalInformation();
							if (additionalInformation != null) {
								for (SymbolAddOnInformation info : additionalInformation) {
									if (info instanceof BeansSymbolAddOnInformation) {
										BeansSymbolAddOnInformation info2 = (BeansSymbolAddOnInformation) info;
										return beanClassName.equals(info2.getBeanType()); 
									}
								}
							}
							return false;
						}).limit(1).collect(Collectors.toList());
						Builder<FixDescriptor> fixListBuilder = ImmutableList.builder();
						List<JavaType.Method> constructors = c.getType().getMethods().stream().filter(m -> m.isConstructor()).collect(Collectors.toList());
						if (beanSymbols.isEmpty()) {
							SourceFile source = getCursor().firstEnclosing(SourceFile.class);
        					String uri = source.getSourcePath().toUri().toString();
        					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId());
        					JavaProjectFinder projectFinder = applicationContext.getBean(JavaProjectFinder.class);
        					if (projectFinder != null) {
        						IJavaProject project = projectFinder.find(new TextDocumentIdentifier(uri)).orElse(null);
        						if (project != null) {
        						
        							for (EnhancedSymbolInformation s : index.getEnhancedSymbols(project)) {
        								if (s.getAdditionalInformation() != null) {
        									ConfigBeanSymbolAddOnInformation configInfo = Arrays.stream(s.getAdditionalInformation()).filter(ConfigBeanSymbolAddOnInformation.class::isInstance).map(ConfigBeanSymbolAddOnInformation.class::cast).findFirst().orElse(null);
        									if (configInfo != null) {
            									for (JavaType.Method constructor : constructors) {
													String constructorParamsSignature = "(" + constructor.getParameterTypes().stream().map(pt -> typePattern(pt)).collect(Collectors.joining(",")) + ")";
													String beanMethodName = "get" + type.getClassName();
													String pattern = beanMethodName + constructorParamsSignature;
													String contructorParamsLabel = "(" + constructor.getParameterTypes().stream().map(NotRegisteredBeansProblem::typeStr).collect(Collectors.joining(", ")) + ")";
														
													Builder<String> paramBuilder = ImmutableList.builder();
													for (int i = 0; i < constructor.getParameterNames().size() && i < constructor.getParameterTypes().size(); i++) {
														JavaType paramType = constructor.getParameterTypes().get(i);
														String paramName = constructor.getParameterNames().get(i); 
														paramBuilder.add(typeStr(paramType) + ' ' + paramName);
													}
													String paramsStr = String.join(", ", paramBuilder.build().toArray(String[]::new));
	            										
            										fixListBuilder.add(new FixDescriptor(DEFINE_METHOD_RECIPE, List.of(s.getSymbol().getLocation().getLeft().getUri()), "Define bean in config '" + configInfo.getBeanID() + "' with constructor " + contructorParamsLabel)
            												.withRecipeScope(RecipeScope.FILE)
            												.withParameters(Map.of(
            														"targetFqName", configInfo.getBeanType(),
            														"signature", pattern,
            														"template", "@Bean\n"
            															+ type.getClassName() + " " + beanMethodName + "(" + paramsStr + ") {\n"
            															+ "return new " +  type.getClassName() + "(" + constructor.getParameterNames().stream().collect(Collectors.joining(", ")) + ");\n"
            															+ "}\n",
            														"imports", allFQTypes(constructor).toArray(String[]::new),
            														"typeStubs", new String[] { source.printAll() },
            														"classpath", IClasspathUtil.getAllBinaryRoots(project.getClasspath()).stream().map(f -> f.toPath().toString()).toArray(String[]::new)
	            														
            												))
            										);
            									}
        									}
        								}
        							}
        						}
        					}
        					marker.withFixes(fixListBuilder.build().toArray(FixDescriptor[]::new));
							c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(marker)));
						}
					}
				}
				return c;
			}
			
		};
	}
	
	private static Set<String> allFQTypes(JavaType type) {
		ImmutableSet.Builder<String> b = ImmutableSet.builder();
		if (type instanceof JavaType.FullyQualified) {
			b.add(((JavaType.FullyQualified) type).getFullyQualifiedName());
			if (type instanceof JavaType.Parameterized) {
				((JavaType.Parameterized) type).getTypeParameters().forEach(t -> b.addAll(allFQTypes(t)));
			}
		} else if (type instanceof JavaType.Array) {
			b.addAll(allFQTypes(((JavaType.Array) type).getElemType()));
		} else if (type instanceof JavaType.Method) {
			JavaType.Method m = (JavaType.Method) type;
			b.addAll(allFQTypes(m.getDeclaringType()));
			b.addAll(allFQTypes(m.getReturnType()));
			m.getParameterTypes().forEach(pt -> b.addAll(allFQTypes(pt)));
		}
		return b.build();
	}
		
	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT;
	}
	
    private static String typePattern(JavaType type) {
        if (type instanceof JavaType.Primitive) {
            if (type.equals(JavaType.Primitive.String)) {
                return ((JavaType.Primitive) type).getClassName();
            }
            return ((JavaType.Primitive) type).getKeyword();
        } else if (type instanceof JavaType.FullyQualified) {
            return ((JavaType.FullyQualified) type).getFullyQualifiedName();
        } else if (type instanceof JavaType.Array) {
            JavaType elemType = ((JavaType.Array) type).getElemType();
            return typePattern(elemType) + "[]";
        }
        return null;
    }
    
    private static String typeStr(JavaType type) {
        if (type instanceof JavaType.Primitive) {
            if (type.equals(JavaType.Primitive.String)) {
                return ((JavaType.Primitive) type).getClassName();
            }
            return ((JavaType.Primitive) type).getKeyword();
        } else if (type instanceof JavaType.Parameterized) {
        	JavaType.Parameterized parametereized = (JavaType.Parameterized) type;
        	return parametereized.getClassName() + "<" + parametereized.getTypeParameters().stream().map(NotRegisteredBeansProblem::typeStr).collect(Collectors.joining(", ")) + ">";
        } else if (type instanceof JavaType.FullyQualified) {
            return ((JavaType.FullyQualified) type).getClassName();
        } else if (type instanceof JavaType.Array) {
            JavaType elemType = ((JavaType.Array) type).getElemType();
            return typeStr(elemType) + "[]";
        }
        return null;
    }


}
