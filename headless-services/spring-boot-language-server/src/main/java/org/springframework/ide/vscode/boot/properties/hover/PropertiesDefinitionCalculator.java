/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.hover;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo.PropertySource;
import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.metadata.types.TypeParser;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class PropertiesDefinitionCalculator {

	private static final Logger log = LoggerFactory.getLogger(PropertiesDefinitionCalculator.class);

	public static List<Location> getLocations(JavaElementLocationProvider locationProvider, IJavaProject project,
			Collection<IMember> propertyJavaElements) {
		return propertyJavaElements.stream().map(member -> locationProvider.findLocation(project, member))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static Type getPropertyType(PropertyFinder propertyFinder, String propertyKey) {
		PropertyInfo best = propertyFinder.findBestHoverMatch(propertyKey);
		if (best != null) {
			return TypeParser.parse(best.getType());
		}
		return null;
	}

	public static Collection<IMember> getPropertyJavaElements(PropertyFinder propertyFinder, IJavaProject project, String propertyKey) {
		PropertyInfo best = propertyFinder.findBestHoverMatch(propertyKey);
		if (best != null) {
			return getPropertyJavaElement(project, best);
		}
		return ImmutableList.of();
	}

	public static Collection<IMember> getPropertyJavaElement(IJavaProject project, PropertyInfo property) {
		List<PropertySource> sources = property.getSources();
		ImmutableList.Builder<IMember> elements = ImmutableList.builder();
		if (sources != null) {
			for (PropertySource source : sources) {
				String typeName = source.getSourceType();
				if (typeName!=null) {
					IType type = project.findType(typeName);
					IMethod method = null;
					if (type!=null) {
						String methodSig = source.getSourceMethod();
						if (methodSig!=null) {
							method = getMethod(type, methodSig);
						} else {
							method = getPropertyMethod(type, property.getName());
						}
					}
					if (method!=null) {
						elements.add(method);
					} else if (type!=null) {
						elements.add(type);
					}
				}
			}
		}
		return elements.build();
	}

	private static IMethod getMethod(IType type, String methodSig) {
		String name = getMethodName(methodSig);
		//TODO: This code assumes 0 arguments, which is the case currently for all
		//  'real' data in spring jars.
		IMethod m = type.getMethod(name, Stream.empty());
		if (m!=null) {
			return m;
		}
		//try  find a method  with the same name.
		return type.getMethods()
				.filter(meth -> name.equals(meth.getElementName()))
				.findFirst()
				.orElse(null);
	}

	private static String getMethodName(String methodSig) {
		String name;
		int nameEnd = methodSig.indexOf('(');
		if (nameEnd>=0) {
			name = methodSig.substring(0, nameEnd);
			int space = name.lastIndexOf(' ');
			if (space >= 0) {
				name = name.substring(space + 1);
			}
		} else {
			name = methodSig;
		}
		return name;
	}

	/**
	 * Attempt to find corresponding setter method for a given property.
	 * @return setter method, or null if not found.
	 */
	private static IMethod getAccessor(IType type, String getOrSet, String propName) {
		try {
			String setterName = getOrSet
				+Character.toUpperCase(propName.charAt(0))
				+toCamelCase(propName.substring(1));
			String sloppySetterName = setterName.toLowerCase();

			IMethod sloppyMatch = null;
			for (IMethod m : type.getMethods().collect(Collectors.toList())) {
				String mname = m.getElementName();
				if (setterName.equals(mname)) {
					//found 'exact' name match... done
					return m;
				} else if (mname.toLowerCase().equals(sloppySetterName)) {
					sloppyMatch = m;
				}
			}
			return sloppyMatch;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public static IMethod getPropertyMethod(IType type, String propName) {
		String[] accessors = { "set", "get", "is" };
		for (String a : accessors) {
			IMethod propertyMethod = getAccessor(type, a, propName);
			if (propertyMethod != null) {
				return propertyMethod;
			}
		}
		return null;
	}

	/**
	 * Convert hyphened name to camel case name. It is
	 * safe to call this on an already camel-cased name.
	 */
	private static String toCamelCase(String name) {
		if (name.isEmpty()) {
			return name;
		} else {
			StringBuilder camel = new StringBuilder();
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c=='-') {
					i++;
					if (i<chars.length) {
						camel.append(Character.toUpperCase(chars[i]));
					}
				} else {
					camel.append(chars[i]);
				}
			}
			return camel.toString();
		}
	}

	public static IField getEnumField(IType type, String value) {
		String[] enumValues = {
			value,
			StringUtil.hyphensToUpperCase(value),
			StringUtil.hyphensToUpperCase(StringUtil.camelCaseToHyphens(value))
		};
		for (String enumValue : enumValues) {
			IField field = type.getField(enumValue);
			if (field != null) {
				return field;
			}
		}
		return null;
	}

	private static List<Location> getEnumValueDefinitionLocation(JavaElementLocationProvider javaElementLocationProvider, IJavaProject project, Type type, String value) {
		IType javaType = project.findType(type.getErasure());
		if (javaType != null) {
			IField field = getEnumField(javaType, value);
			if (field != null) {
				Location location = javaElementLocationProvider.findLocation(project, field);
				if (location != null) {
					return ImmutableList.of(location);
				}
			}
		}
		return ImmutableList.of();
	}

	private static List<Location> getClassValueDefinitionLocation(JavaElementLocationProvider javaElementLocationProvider, IJavaProject project, String value) {
		IType javaType = project.findType(value);
		if (javaType != null) {
			Location location = javaElementLocationProvider.findLocation(project, javaType);
			if (location != null) {
				return ImmutableList.of(location);
			}
		}
		return ImmutableList.of();
	}

	public static List<Location> getValueDefinitionLocations(JavaElementLocationProvider javaElementLocationProvider, TypeUtil typeUtil, Type type, String value) {
		IJavaProject project = typeUtil.getJavaProject();

		if (TypeUtil.isClass(type)) {
			return getClassValueDefinitionLocation(javaElementLocationProvider, project, value);
		}

		if (typeUtil.isEnum(type)) {
			return getEnumValueDefinitionLocation(javaElementLocationProvider, project, type, value);
		}

		return ImmutableList.of();
	}

}
