/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.beans.ConfigBeanSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.DefineMethod;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

public class NotRegisteredBeansReconciler implements JdtAstReconciler, ApplicationContextAware {

	private static final List<String> AOT_BEANS = List.of(
			"org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor",
			"org.springframework.beans.factory.aot.BeanRegistrationAotProcessor"
	);

	private ApplicationContext applicationContext;

	private QuickfixRegistry registry;
	
	public NotRegisteredBeansReconciler(QuickfixRegistry registry) {
		this.registry = registry;
		
	}
	
	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) throws RequiredCompleteAstException {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				if (!node.isInterface() && !Modifier.isAbstract(node.getModifiers())) {
					ITypeBinding type = node.resolveBinding();
					if (type != null && ReconcileUtils.implementsAnyType(AOT_BEANS, type)) {
						String beanClassName =type.getQualifiedName();
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
						
						if (beanSymbols.isEmpty()) {
							Builder<FixDescriptor> fixListBuilder = ImmutableList.builder();
    						for (EnhancedSymbolInformation s : index.getEnhancedSymbols(project)) {
    							if (s.getAdditionalInformation() != null) {
    								ConfigBeanSymbolAddOnInformation configInfo = Arrays.stream(s.getAdditionalInformation()).filter(ConfigBeanSymbolAddOnInformation.class::isInstance).map(ConfigBeanSymbolAddOnInformation.class::cast).findFirst().orElse(null);
    								if (configInfo != null) {
    									for (IMethodBinding constructor : type.getDeclaredMethods()) {
    										if (constructor.isConstructor()) {
    											String constructorParamsSignature = "(" + Arrays.stream(constructor.getParameterTypes()).map(pt -> typePattern(pt)).collect(Collectors.joining(",")) + ")";
    											String beanMethodName = "get" + type.getName();
    											String pattern = beanMethodName + constructorParamsSignature;
    											String contructorParamsLabel = "(" + Arrays.stream(constructor.getParameterTypes()).map(NotRegisteredBeansReconciler::typeStr).collect(Collectors.joining(", ")) + ")";
    												
    											Builder<String> paramBuilder = ImmutableList.builder();
    											for (int i = 0; i < constructor.getParameterNames().length && i < constructor.getParameterTypes().length; i++) {
    												ITypeBinding paramType = constructor.getParameterTypes()[i];
    												String paramName = constructor.getParameterNames()[i]; 
    												paramBuilder.add(typeStr(paramType) + ' ' + paramName);
    											}
    											String paramsStr = String.join(", ", paramBuilder.build().toArray(String[]::new));
                									
            									fixListBuilder.add(new FixDescriptor(DefineMethod.class.getName(), List.of(s.getSymbol().getLocation().getLeft().getUri()), "Define bean in config '" + configInfo.getBeanID() + "' with constructor " + contructorParamsLabel)
            											.withRecipeScope(RecipeScope.FILE)
            											.withParameters(Map.of(
            													"targetFqName", configInfo.getBeanType(),
            													"signature", pattern,
            													"template", "@Bean\n"
            														+ type.getName() + " " + beanMethodName + "(" + paramsStr + ") {\n"
            														+ "return new " +  type.getName() + "(" + Arrays.stream(constructor.getParameterNames()).collect(Collectors.joining(", ")) + ");\n"
            														+ "}\n",
            													"imports", allFQTypes(constructor).toArray(String[]::new),
            													"typeStubs", new String[0]/*new String[] { source.printAll() }*/,
            													"classpath", IClasspathUtil.getAllBinaryRoots(project.getClasspath()).stream().map(f -> f.toPath().toString()).toArray(String[]::new)
                													
            											))
            									);
    										}
    									}
    								}
    							}
    						}
    						ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), getProblemType().getLabel(), node.getName().getStartPosition(), node.getName().getLength());
    						ReconcileUtils.setRewriteFixes(registry, problem, fixListBuilder.build());
    						problemCollector.accept(problem);
						}
					}
				}
				return super.visit(node);
			}
			
		});
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return SpringAotJavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	private static Set<String> allFQTypes(IBinding binding) {
		ImmutableSet.Builder<String> b = ImmutableSet.builder();
		if (binding instanceof IMethodBinding) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			b.addAll(allFQTypes(methodBinding.getDeclaringClass()));
			for (ITypeBinding paramType : methodBinding.getParameterTypes()) {
				b.addAll(allFQTypes(paramType));
			}
		} else if (binding instanceof ITypeBinding) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			if (!typeBinding.isPrimitive()) {
				for (ITypeBinding paramType : typeBinding.getTypeParameters()) {
					b.addAll(allFQTypes(paramType));
				}
				for (ITypeBinding argType : typeBinding.getTypeArguments()) {
					b.addAll(allFQTypes(argType));
					
				}
				b.addAll(allFQTypes(typeBinding.getBound()));
				ITypeBinding erasure = typeBinding.getErasure();
				if (erasure == typeBinding) {
					b.add(typeBinding.getQualifiedName());
				} else {
					b.addAll(allFQTypes(erasure));
				}
			}
		}
		return b.build();
	}


    private static String typePattern(ITypeBinding type) {
    	if (type.isArray()) {
    		return typePattern(type.getErasure()) + "[]";
    	} else {
    		return ReconcileUtils.getDeepErasureType(type).getQualifiedName();
    	}
    }
    
    private static String typeStr(ITypeBinding type) {
    	return type.getName();
    }

}
