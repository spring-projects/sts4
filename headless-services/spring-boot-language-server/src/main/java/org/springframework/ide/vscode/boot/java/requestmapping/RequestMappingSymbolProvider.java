/*******************************************************************************
 * Copyright (c) 2017, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider extends AbstractSymbolProvider {

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {

		if (node.getParent() instanceof MethodDeclaration) {
			try {
				Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
				String[] path = getPath(node, context);
				String[] parentPath = getParentPath(node, context);
				String[] methods = getMethod(node, context);
				String[] contentTypes = getContentTypes(node, context);
				String[] acceptTypes = getAcceptTypes(node, context);

				Stream<String> stream = parentPath == null ? Stream.of("") : Arrays.stream(parentPath);
				stream.filter(Objects::nonNull)
						.flatMap(parent -> (path == null ? Stream.<String>empty() : Arrays.stream(path))
								.filter(Objects::nonNull).map(p -> {
									return calculatePath(parent, p);
								}))
						.map(p -> RouteUtils.createRouteSymbol(location, p, methods, contentTypes, acceptTypes, null))
						.forEach((enhancedSymbol) -> context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String calculatePath(String parent, String path) {
		String separator = !parent.endsWith("/") && !path.startsWith("/") && !path.isEmpty() ? "/" : "";
		String resultPath = parent + separator + path;

		String result = resultPath.startsWith("/") ? resultPath : "/" + resultPath;
		return result;
	}

	private String[] getMethod(Annotation node, SpringIndexerJavaContext context) {
		String[] methods = null;

		// extract from annotation params
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("method")) {
						Expression expression = pair.getValue();
						methods = ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
						break;
					}
				}
			}
		}
		
		// extract from annotation type
		if (methods == null) {
			methods = getRequestMethod(node);
		}

		// extract from parent annotations
		if (methods == null && node.getParent() instanceof MethodDeclaration) {
			Annotation parentAnnotation = getParentAnnotation(node);
			if (parentAnnotation != null) {
				methods = getMethod(parentAnnotation, context);
			}
			else {
				methods = getAttributeValuesFromSupertypes("method", node, context);
			}
		}

		return methods;
	}
	
	private String[] getPath(Annotation node, SpringIndexerJavaContext context) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && (valueName.equals("value") || valueName.equals("path"))) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		} else if (node.isSingleMemberAnnotation()) {
			SingleMemberAnnotation singleNode = (SingleMemberAnnotation) node;
			Expression expression = singleNode.getValue();
			return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
		}

		return new String[] { "" };
	}

	private String[] getParentPath(Annotation node, SpringIndexerJavaContext context) {
		Annotation parentAnnotation = getParentAnnotation(node);
		if (parentAnnotation != null) {
			return getPath(parentAnnotation, context);
		}
		else {
			return getPathFromSupertypes(node, context);
		}
	}

	private String[] getPathFromSupertypes(Annotation node, SpringIndexerJavaContext context) {
		IAnnotationBinding annotationBinding = getAnnotationFromSupertypes(node, context);
		IMemberValuePairBinding valuePair = getValuePair(annotationBinding, "value", "path");
		
		if (valuePair != null) {
			Object value = valuePair.getValue();
			if (value instanceof Object[]) {
				Object[] values = (Object[]) value;
				String[] result = new String[values.length];
				for (int k = 0; k < result.length; k++) {
					result[k] = values[k].toString();
				}
				return result;

			}
			else if (value instanceof String[]) {
				return (String[]) value;
			}
			else if (value != null) {
				return new String[] {value.toString()};
			}
		}
		
		return null;
	}
	
	private Annotation getParentAnnotation(Annotation node) {
		
		// lookup class level request mapping annotation
		ASTNode parent = node.getParent() != null ? node.getParent().getParent() : null;
		while (parent != null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParent();
		}

		if (parent != null) {
			TypeDeclaration type = (TypeDeclaration) parent;
			List<?> modifiers = type.modifiers();
			Iterator<?> iterator = modifiers.iterator();
			while (iterator.hasNext()) {
				Object modifier = iterator.next();
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					ITypeBinding resolvedType = annotation.resolveTypeBinding();
					String annotationType = resolvedType.getQualifiedName();
					if (annotationType != null && Annotations.SPRING_REQUEST_MAPPING.equals(annotationType)) {
						return annotation;
					}
				}
			}
		}
		
		return null;
	}

	private String[] getRequestMethod(Annotation annotation) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			switch (type.getQualifiedName()) {
			case Annotations.SPRING_GET_MAPPING:
				return new String[] { "GET" };
			case Annotations.SPRING_POST_MAPPING:
				return new String[] { "POST" };
			case Annotations.SPRING_DELETE_MAPPING:
				return new String[] { "DELETE" };
			case Annotations.SPRING_PUT_MAPPING:
				return new String[] { "PUT" };
			case Annotations.SPRING_PATCH_MAPPING:
				return new String[] { "PATCH" };
			}
		}
		return null;
	}

	private String[] getAcceptTypes(Annotation node, SpringIndexerJavaContext context) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("consumes")) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		}
		
		// lookup accept types on class level (same class or supertypes)
		if (node.getParent() instanceof MethodDeclaration) {
			Annotation parentAnnotation = getParentAnnotation(node);
			if (parentAnnotation != null) {
				return getAcceptTypes(parentAnnotation, context);
			}
			else {
				return getAttributeValuesFromSupertypes("consumes", node, context);
			}
		}

		return new String[0];
	}
	
	private String[] getContentTypes(Annotation node, SpringIndexerJavaContext context) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("produces")) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		}
		
		// lookup content types on class level (same class or supertypes)
		if (node.getParent() instanceof MethodDeclaration) {
			Annotation parentAnnotation = getParentAnnotation(node);
			if (parentAnnotation != null) {
				return getContentTypes(parentAnnotation, context);
			}
			else {
				return getAttributeValuesFromSupertypes("produces", node, context);
			}
		}

		return new String[0];
	}

	private String[] getAttributeValuesFromSupertypes(String attributeName, Annotation node, SpringIndexerJavaContext context) {
		IAnnotationBinding annotationBinding = getAnnotationFromSupertypes(node, context);
		IMemberValuePairBinding valuePair = getValuePair(annotationBinding, attributeName);
		
		if (valuePair != null) {
			Object value = valuePair.getValue();
			if (value instanceof Object[]) {
				Object[] values = (Object[]) value;
				String[] result = new String[values.length];
				for (int k = 0; k < result.length; k++) {
					
					Object v = values[k];
					if (v instanceof IVariableBinding) {
						IVariableBinding varBinding = (IVariableBinding) v;
						result[k] = varBinding.getName();
					}
					else if (v instanceof String) {
						result[k] = (String) v;
					}
				}
				return result;

			}
			else if (value instanceof String[]) {
				return (String[]) value;
			}
			else if (value != null) {
				return new String[] {value.toString()};
			}
		}
		
		return null;
	}

	private IMemberValuePairBinding getValuePair(IAnnotationBinding annotationBinding, String... names) {
		if (annotationBinding != null) {
			IMemberValuePairBinding[] valuePairs = annotationBinding.getDeclaredMemberValuePairs();
			
			if (valuePairs != null ) {
				
				for (int j = 0; j < valuePairs.length; j++) {
					String valueName = valuePairs[j].getName();

					if (valueName != null) {
						for (int i = 0; i < names.length; i++) {
							if (names[i] != null && names[i].equals(valueName)) {
								return valuePairs[j];
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private IAnnotationBinding getAnnotationFromSupertypes(Annotation node, SpringIndexerJavaContext context) {
		ASTNode parent = node.getParent() != null ? node.getParent().getParent() : null;
		while (parent != null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParent();
		}
		
		if (parent != null) {
			TypeDeclaration type = (TypeDeclaration) parent;
			ITypeBinding typeBinding = type.resolveBinding();
			if (typeBinding != null) {
				return findFirstRequestMappingAnnotation(typeBinding);
			}
		}
		
		return null;
	}
	
	private IAnnotationBinding findFirstRequestMappingAnnotation(ITypeBinding start) {
		if (start == null) {
			return null;
		}
		
		IAnnotationBinding found = getRequestMappingAnnotation(start);
		if (found != null) {
			return found;
		}
		else {
			// search interfaces first
			ITypeBinding[] interfaces = start.getInterfaces();
			if (interfaces != null) {
				for (int i = 0; i < interfaces.length; i++) {
					found = findFirstRequestMappingAnnotation(interfaces[i]);
					if (found != null) {
						return found;
					}
				}
			}
			
			// search superclass second
			ITypeBinding superclass = start.getSuperclass();
			if (superclass != null) {
				return findFirstRequestMappingAnnotation(superclass);
			}
			
			// nothing found
			return null;
		}
	}

	private IAnnotationBinding getRequestMappingAnnotation(ITypeBinding typeBinding) {
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].getAnnotationType() != null && Annotations.SPRING_REQUEST_MAPPING.equals(annotations[i].getAnnotationType().getQualifiedName())) {
				return annotations[i];
			}
		}
		return null;
	}
	


}
