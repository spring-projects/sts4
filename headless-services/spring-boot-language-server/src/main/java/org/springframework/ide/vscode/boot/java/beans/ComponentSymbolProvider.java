/*******************************************************************************
 * Copyright (c) 2017, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.events.EventListenerIndexElement;
import org.springframework.ide.vscode.boot.java.events.EventListenerIndexer;
import org.springframework.ide.vscode.boot.java.events.EventPublisherIndexElement;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingIndexer;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.DefaultSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.InjectionPoint;
import org.springframework.ide.vscode.commons.protocol.spring.SimpleSymbolElement;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class ComponentSymbolProvider implements SymbolProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentSymbolProvider.class);

	@Override
	public void addSymbols(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		try {
			if (node != null && node.getParent() != null && node.getParent() instanceof TypeDeclaration) {
				createSymbol(node, annotationType, metaAnnotations, context, doc);
			}
			else if (Annotations.NAMED_ANNOTATIONS.contains(annotationType.getQualifiedName())) {
				WorkspaceSymbol symbol = DefaultSymbolProvider.provideDefaultSymbol(node, doc);
				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), symbol));
				context.getBeans().add(new CachedBean(context.getDocURI(), new SimpleSymbolElement(symbol)));
			}
		}
		catch (BadLocationException e) {
			log.error("", e);
		}
	}

	protected void createSymbol(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) throws BadLocationException {
		String annotationTypeName = annotationType.getName();
		
		Collection<String> metaAnnotationNames = metaAnnotations.stream()
				.map(ITypeBinding::getName)
				.collect(Collectors.toList());
		
		TypeDeclaration type = (TypeDeclaration) node.getParent();

		String beanName = BeanUtils.getBeanNameFromComponentAnnotation(node, type);
		ITypeBinding beanType = type.resolveBinding();

		Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
		
		WorkspaceSymbol symbol = new WorkspaceSymbol(
				beanLabel("+", annotationTypeName, metaAnnotationNames, beanName, beanType.getName()), SymbolKind.Interface,
				Either.forLeft(location));
		
		boolean isConfiguration = Annotations.CONFIGURATION.equals(annotationType.getQualifiedName())
				|| metaAnnotations.stream().anyMatch(t -> Annotations.CONFIGURATION.equals(t.getQualifiedName()));

		InjectionPoint[] injectionPoints = ASTUtils.findInjectionPoints(type, doc);
		
		Set<String> supertypes = new HashSet<>();
		ASTUtils.findSupertypes(beanType, supertypes);
		
		Collection<Annotation> annotationsOnType = ASTUtils.getAnnotations(type);
		
		AnnotationMetadata[] annotations = Stream.concat(
				Arrays.stream(ASTUtils.getAnnotationsMetadata(annotationsOnType, doc))
				,
				metaAnnotations.stream()
				.map(an -> new AnnotationMetadata(an.getQualifiedName(), true, null, null)))
				.toArray(AnnotationMetadata[]::new);
		
		Bean beanDefinition = new Bean(beanName, beanType.getQualifiedName(), location, injectionPoints, supertypes, annotations, isConfiguration, symbol.getName());
		
		// event publisher checks
		boolean usesEventPublisher = false;
		for (InjectionPoint injectionPoint : injectionPoints) {
			if (Annotations.EVENT_PUBLISHER.equals(injectionPoint.getType())) {
				usesEventPublisher = true;
			}
		}
		
		if (usesEventPublisher) {
			if (context.isFullAst()) {
				scanEventPublisherInvocations(beanDefinition, node, annotationType, metaAnnotations, context, doc);
			}
			else {
				throw new RequiredCompleteAstException();
			}
		}
		
		indexBeanMethods(beanDefinition, type, annotationType, metaAnnotations, context, doc);
		indexEventListeners(beanDefinition, type, annotationType, metaAnnotations, context, doc);
		indexEventListenerInterfaceImplementation(beanDefinition, type, context, doc);
		indexRequestMappings(beanDefinition, type, annotationType, metaAnnotations, context, doc);
		indexConfigurationProperties(beanDefinition, type, context, doc);

		context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), symbol));
		context.getBeans().add(new CachedBean(context.getDocURI(), beanDefinition));
	}
	
	private void indexConfigurationProperties(Bean beanDefinition, TypeDeclaration type, SpringIndexerJavaContext context, TextDocument doc) {
		AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(type);

		if (annotationHierarchies.isAnnotatedWith(type.resolveBinding(), Annotations.CONFIGURATION_PROPERTIES)) {
			ConfigurationPropertiesSymbolProvider.indexConfigurationProperties(beanDefinition, type, context, doc);
		}
	}

	private void indexBeanMethods(Bean bean, TypeDeclaration type, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(type);
		if (bean.isConfiguration()) {
			MethodDeclaration[] methods = type.getMethods();
			if (methods == null) {
				return;
			}
			
			for (int i = 0; i < methods.length; i++) {
				MethodDeclaration methodDecl = methods[i];
				Collection<Annotation> annotations = ASTUtils.getAnnotations(methodDecl);
				
				for (Annotation annotation : annotations) {
					ITypeBinding typeBinding = annotation.resolveTypeBinding();
					
					boolean isBeanMethod = annotationHierarchies.isAnnotatedWith(typeBinding, Annotations.BEAN);
					if (isBeanMethod) {
						BeansIndexer.indexBeanMethod(bean, annotation, context, doc);
					}
				}
			}
		}
	}

	private void indexEventListeners(Bean bean, TypeDeclaration type, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(type);		
		
		MethodDeclaration[] methods = type.getMethods();
		if (methods == null) {
			return;
		}

		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration methodDecl = methods[i];
			Collection<Annotation> annotations = ASTUtils.getAnnotations(methodDecl);

			for (Annotation annotation : annotations) {
				ITypeBinding typeBinding = annotation.resolveTypeBinding();

				boolean isEventListenerAnnotation = annotationHierarchies.isAnnotatedWith(typeBinding, Annotations.EVENT_LISTENER);
				if (isEventListenerAnnotation) {
					EventListenerIndexer.indexEventListener(bean, annotation, context, doc);
				}
			}
		}
	}

	private void indexRequestMappings(Bean controller, TypeDeclaration type, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(type);		
		boolean isController = annotationHierarchies.isAnnotatedWith(annotationType, Annotations.CONTROLLER);
		
		if (isController) {
			MethodDeclaration[] methods = type.getMethods();
			if (methods == null) {
				return;
			}
			
			for (int i = 0; i < methods.length; i++) {
				MethodDeclaration methodDecl = methods[i];
				Collection<Annotation> annotations = ASTUtils.getAnnotations(methodDecl);
				
				for (Annotation annotation : annotations) {
					ITypeBinding typeBinding = annotation.resolveTypeBinding();
					
					boolean isRequestMappingAnnotation = annotationHierarchies.isAnnotatedWith(typeBinding, Annotations.SPRING_REQUEST_MAPPING);
					if (isRequestMappingAnnotation) {
						RequestMappingIndexer.indexRequestMapping(controller, annotation, context, doc);
					}
				}
			}
		}
	}

	private void scanEventPublisherInvocations(Bean component, Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {
		TypeDeclaration type = (TypeDeclaration) node.getParent();
		type.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(MethodInvocation methodInvocation) {
				try {
					String methodName = methodInvocation.getName().toString();
					if ("publishEvent".equals(methodName)) {

						IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
						boolean doesInvokeEventPublisher = Annotations.EVENT_PUBLISHER.equals(methodBinding.getDeclaringClass().getQualifiedName());
						if (doesInvokeEventPublisher) {
							List<?> arguments = methodInvocation.arguments();
							if (arguments != null && arguments.size() == 1) {

								ITypeBinding eventTypeBinding = ((Expression) arguments.get(0)).resolveTypeBinding();
								if (eventTypeBinding != null) {

									DocumentRegion nodeRegion = ASTUtils.nodeRegion(doc, methodInvocation);
									Location location;
									location = new Location(doc.getUri(), nodeRegion.asRange());

									Set<String> typesFromhierarchy = new HashSet<>();
									ASTUtils.findSupertypes(eventTypeBinding, typesFromhierarchy);

									EventPublisherIndexElement eventPublisherIndexElement = new EventPublisherIndexElement(eventTypeBinding.getQualifiedName(), location, typesFromhierarchy);
									component.addChild(eventPublisherIndexElement);

									// symbol
									String symbolLabel = "@EventPublisher (" + eventTypeBinding.getName() + ")";
									WorkspaceSymbol symbol = new WorkspaceSymbol(symbolLabel, SymbolKind.Interface, Either.forLeft(location));
									context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), symbol));
								}
							}
						}
					}
				
				} catch (BadLocationException e) {
					log.error("", e);
				}
				return super.visit(methodInvocation);
			}
		});
	}
	
	@Override
	public void addSymbols(TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(typeDeclaration);
		boolean isComponment = annotationHierarchies.isAnnotatedWith(typeDeclaration.resolveBinding(), Annotations.COMPONENT);
		
		// check for event listener implementations on classes that are not annotated with component, but created via bean methods (for example)
		if (!isComponment) {
			indexEventListenerInterfaceImplementation(null, typeDeclaration, context, doc);
		}
		
	}
	
	private void indexEventListenerInterfaceImplementation(Bean bean, TypeDeclaration typeDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		try {
			ITypeBinding typeBinding = typeDeclaration.resolveBinding();
			if (typeBinding == null) return;
			
			ITypeBinding inTypeHierarchy = ASTUtils.findInTypeHierarchy(typeDeclaration, doc, typeBinding, Set.of(Annotations.APPLICATION_LISTENER));
			if (inTypeHierarchy == null) return;
	
			MethodDeclaration handleEventMethod = findHandleEventMethod(typeDeclaration);
			if (handleEventMethod == null) return;
	
			IMethodBinding methodBinding = handleEventMethod.resolveBinding();
			ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
			if (parameterTypes != null && parameterTypes.length == 1) {
	
				ITypeBinding eventType = parameterTypes[0];
				String eventTypeFq = eventType.getQualifiedName();
	
				DocumentRegion nodeRegion = ASTUtils.nodeRegion(doc, handleEventMethod.getName());
				Location handleMethodLocation = new Location(doc.getUri(), nodeRegion.asRange());
	
				Collection<Annotation> annotationsOnHandleEventMethod = ASTUtils.getAnnotations(handleEventMethod);
				AnnotationMetadata[] handleEventMethodAnnotations = ASTUtils.getAnnotationsMetadata(annotationsOnHandleEventMethod, doc);
	
				EventListenerIndexElement eventElement = new EventListenerIndexElement(eventTypeFq, handleMethodLocation, typeBinding.getQualifiedName(), handleEventMethodAnnotations);
				
				if (bean != null) {
					bean.addChild(eventElement);
				}
				else {
					context.getBeans().add(new CachedBean(context.getDocURI(), eventElement));
				}
			}
		} catch (BadLocationException e) {
			log.error("", e);
		}
	}

	private MethodDeclaration findHandleEventMethod(TypeDeclaration type) {
		MethodDeclaration[] methods = type.getMethods();
		
		for (MethodDeclaration method : methods) {
			IMethodBinding binding = method.resolveBinding();
			String name = binding.getName();
			
			if (name != null && name.equals("onApplicationEvent")) {
				return method;
			}
		}
		return null;
	}
	
	public static String beanLabel(String searchPrefix, String annotationTypeName, Collection<String> metaAnnotationNames, String beanName, String beanType) {
		StringBuilder symbolLabel = new StringBuilder();
		symbolLabel.append("@");
		symbolLabel.append(searchPrefix);
		symbolLabel.append(' ');
		symbolLabel.append('\'');
		symbolLabel.append(beanName);
		symbolLabel.append('\'');
		symbolLabel.append(" (@");
		symbolLabel.append(annotationTypeName);
		if (!metaAnnotationNames.isEmpty()) {
			symbolLabel.append(" <: ");
			boolean first = true;
			for (String ma : metaAnnotationNames) {
				if (!first) {
					symbolLabel.append(", ");
				}
				symbolLabel.append("@");
				symbolLabel.append(ma);
				first = false;
			}
		}
		symbolLabel.append(") ");
		symbolLabel.append(beanType);
		return symbolLabel.toString();
	}
	
}
