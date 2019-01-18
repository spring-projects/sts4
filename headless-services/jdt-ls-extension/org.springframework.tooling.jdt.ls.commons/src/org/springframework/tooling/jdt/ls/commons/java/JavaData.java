/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;
import org.springframework.tooling.jdt.ls.commons.classpath.ClasspathUtil;
import org.springframework.tooling.jdt.ls.commons.java.TypeData.AnnotationData;
import org.springframework.tooling.jdt.ls.commons.java.TypeData.ClasspathEntryData;
import org.springframework.tooling.jdt.ls.commons.java.TypeData.FieldData;
import org.springframework.tooling.jdt.ls.commons.java.TypeData.MethodData;
import org.springframework.tooling.jdt.ls.commons.javadoc.JavadocUtils;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

import com.google.common.collect.ImmutableList;

public class JavaData {
	
	private Logger logger;
	
	public JavaData(Logger logger) {
		this.logger = logger;
	}
	
	public TypeData typeData(String projectUri, String bindingKey, boolean lookInOtherProjects) {
		try {
			IJavaElement element = findElement(projectUri == null ? null : URI.create(projectUri), bindingKey, lookInOtherProjects);
			if (element instanceof IType) {
				return createTypeData((IType) element);
			}
		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}
	
	public static IJavaElement findElement(URI projectUri, String bindingKey, boolean lookInOtherProjects) throws Exception {
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
		IJavaElement element = javaProject == null ? null : findElement(javaProject, bindingKey);
		if (lookInOtherProjects && element == null) {
			for (IJavaProject jp : ResourceUtils.allJavaProjects()) {
				if (jp != javaProject) {
					element = findElement(jp, bindingKey);
					if (element != null) {
						break;
					}
				}
			}
		}
		return element;
	}

	private static IJavaElement findElement(IJavaProject project, String bindingKey) {
		IJavaElement element = null;
		// JDT cannot find anonymous inner type from its binding key
		// Find its declaring type. If declaring type found then anonymous inner type is present in the binding key
		String declaringTypeFromAnonymousInnerType = delcaringTypeOfAnonymousInnerType(bindingKey);
		try {
			if (declaringTypeFromAnonymousInnerType == null) {
				element = project.findElement(bindingKey, null);
			} else {
				// Look for element inside the enclosing type that JDT can find. Brute force finding algorithm
				element = findInnerElement(project.findElement(declaringTypeFromAnonymousInnerType, null), bindingKey);
			}
		} catch (Throwable t) {
			// ignore
		}
		if (element == null) {
			// Try modifying the binding key to search for the alternate binding
			try {
				String alternateBinding = JavadocUtils.alternateBinding(bindingKey);
				if (alternateBinding != null) {
					element = project.findElement(alternateBinding, null);
				}
			} catch (Throwable t) {
				// ignore
			}
		}
		return element;
	}
	
	private static IJavaElement findInnerElement(IJavaElement container, String bindingKey) {
		if (container instanceof IField) {
			if (bindingKey.equals(((IField)container).getKey())) {
				return container;
			}
		} else if (container instanceof IMethod) {
			if (bindingKey.equals(((IMethod)container).getKey())) {
				return container;
			}			
		} else if (container instanceof IType) {
			if (bindingKey.equals(((IType)container).getKey())) {
				return container;
			}
		}
		
		if (container instanceof IParent) {
			try {
				for(IJavaElement e : ((IParent)container).getChildren()) {
					IJavaElement found = findInnerElement(e, bindingKey);
					if (found != null) {
						return found;
					}
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return null;
	}
	
	private static String delcaringTypeOfAnonymousInnerType(String bindingKey) {
		int idx = bindingKey.indexOf('$');
		if (idx >= 0) {
			String rest = bindingKey.substring(idx + 1);
			if (rest.charAt(rest.length() - 1) == ';') {
				rest = rest.substring(0, rest.length() - 1);
			}
			String[] tokens = rest.split("$");
			for (String token : tokens) {
				int dotIdx = token.indexOf('.');
				String typeToken = dotIdx < 0 ? token : token.substring(0, dotIdx);
				if (!typeToken.isEmpty()) {
					try {
						Integer.parseInt(typeToken);
						// Succeeded. There is inner anonymous type present.
						// Return it's enclosing type binding key which JDT should have no problem finding the element for
						int declaringTypeIdx = bindingKey.indexOf('$' + token);
						if (declaringTypeIdx < 0) {
							// Should not happen. Throw exception?
							return null;
						} else {
							return bindingKey.substring(0, declaringTypeIdx) + ';';
						}
					} catch (NumberFormatException e) {
						// ignore and continue looping
					}
				}
			}
		}
		return null;
	}
	
	private void fillJavaElementData(IJavaElement element, JavaElementData data) {
		data.setName(element.getElementName());
		data.setHandleIdentifier(element.getHandleIdentifier());
	}
	
	private FieldData createFieldData(IField field) {
		FieldData data = new FieldData();
		fillMemberData(field, data);
		data.setBindingKey(field.getKey());
		ImmutableList.Builder<AnnotationData> annotationsBuilder = ImmutableList.builder();
		try {
			for (IAnnotation annotation : field.getAnnotations()) {
				annotationsBuilder.add(createAnnotationData(annotation));
			}
		} catch (JavaModelException e) {
			logger.log(e);
		}
		data.setAnnotations(annotationsBuilder.build());
		return data;
	}
	
	private void fillMemberData(IMember member, MemberData data) {
		fillJavaElementData(member, data);
		IType declaringType = member.getDeclaringType();
		if (declaringType != null) {
			data.setDeclaringType(declaringType.getKey());
		}
		try {
			data.setFlags(member.getFlags());
		} catch (JavaModelException e) {
			logger.log(e);
		}
	}
	
	private MethodData createMethodData(IMethod method) {
		MethodData data = new MethodData();
		fillMemberData(method, data);
		data.setBindingKey(method.getKey());
		ImmutableList.Builder<AnnotationData> annotationsBuilder = ImmutableList.builder();
		try {
			data.setConstructor(method.isConstructor());
			for (IAnnotation annotation : method.getAnnotations()) {
				annotationsBuilder.add(createAnnotationData(annotation));
			}
		} catch (JavaModelException e) {
			logger.log(e);
		}
		data.setAnnotations(annotationsBuilder.build());
		return data;
	}

	private TypeData createTypeData(IType type) {
		TypeData data = new TypeData();
		fillTypeData(type, data);
		return data;
	}
	
	private void fillTypeData(IType type, TypeData data) {
		fillMemberData(type, data);
		
		data.setFqName(type.getFullyQualifiedName());
		
		data.setBindingKey(type.getKey());
		
		ImmutableList.Builder<FieldData> fieldsBuilder = ImmutableList.builder(); 
		ImmutableList.Builder<MethodData> methodsBuilder = ImmutableList.builder();
		ImmutableList.Builder<AnnotationData> annotationsBuilder = ImmutableList.builder();
		try {
			for (IField field : type.getFields()) {
				fieldsBuilder.add(createFieldData(field));
			}
			for (IMethod method : type.getMethods()) {
				methodsBuilder.add(createMethodData(method));
			}
			for (IAnnotation annotation : type.getAnnotations()) {
				annotationsBuilder.add(createAnnotationData(annotation));
			}
			data.setAnnotation(type.isAnnotation());
			data.setClass(type.isClass());
			data.setEnum(type.isEnum());
			data.setInterface(type.isInterface());
			data.setSuperClassName(type.getSuperclassName());
			data.setSuperInterfaceNames(type.getSuperInterfaceNames());
		} catch (JavaModelException e) {
			logger.log(e);
		}
		data.setFields(fieldsBuilder.build());
		data.setMethods(methodsBuilder.build());
		data.setAnnotations(annotationsBuilder.build());
		
		data.setClasspathEntry(createClasspathEntryData(type));
	}
	
	private ClasspathEntryData createClasspathEntryData(IMember member) {
		ClasspathEntryData data = new ClasspathEntryData();
		
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) member.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (packageFragmentRoot != null) {
			try {
				IClasspathEntry entry = packageFragmentRoot.getRawClasspathEntry();
				if (entry != null) {
					List<CPE> cpes = ClasspathUtil.createCpes(packageFragmentRoot.getJavaProject(), entry);
					Assert.isTrue(cpes.size() < 2);
					if (!cpes.isEmpty()) {
						data.setCpe(cpes.get(0));
					}
				}
			} catch (JavaModelException | MalformedURLException e) {
				logger.log(e);
			}
		}

		ITypeRoot typeRoot = member.getTypeRoot();
		try {
			if (typeRoot != null && typeRoot.getModule() != null) {
				data.setModule(typeRoot.getModule().getElementName());
			}
		} catch (JavaModelException e) {
			logger.log(e);
		}
		
		return data;
	}
	
	private AnnotationData createAnnotationData(IAnnotation annotation) {
		AnnotationData data = new AnnotationData();
		fillAnnotationData(annotation, data);
		return data;
	}
	
	private void fillAnnotationData(IAnnotation annotation, AnnotationData data) {
		fillJavaElementData(annotation, data);
		Map<String, Object> pairs = new HashMap<>();
		try {
			for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
				pairs.put(pair.getMemberName(), pair.getValue());
			}
		} catch (JavaModelException e) {
			logger.log(e);
		}
		data.setValuePairs(pairs);
	}

}
