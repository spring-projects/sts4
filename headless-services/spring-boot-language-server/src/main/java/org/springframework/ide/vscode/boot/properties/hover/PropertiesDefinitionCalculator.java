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
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Key;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;

import com.google.common.collect.ImmutableList;

public class PropertiesDefinitionCalculator {

	private static final Logger log = LoggerFactory.getLogger(PropertiesDefinitionCalculator.class);

	final private PropertyFinder propertyFinder;
	final private IJavaProject project;
	final private JavaElementLocationProvider javaElementLocationProvider;

	public PropertiesDefinitionCalculator(JavaElementLocationProvider javaElementLocationProvider, FuzzyMap<PropertyInfo> index, TypeUtil typeUtil, IDocument doc, int offset) {
		this.javaElementLocationProvider = javaElementLocationProvider;
		this.propertyFinder = new PropertyFinder(index, typeUtil, doc, offset);
		this.project = typeUtil.getJavaProject();
	}

	public List<Location> calculate() {
		Node node = propertyFinder.findNode();
		if (node instanceof Key) {
			Collection<IMember> propertyJavaElements = getPropertyJavaElements(((Key) node).decode());
			return getPropertyDefintion(propertyJavaElements);
		} else if (node instanceof Value) {
			Value value = (Value) node;
			Key key = value.getParent().getKey();

			String javaTypeFqName = getPropertyJavaTypeFqName(key.decode());

			if (javaTypeFqName != null) {

				// Class reference value link
				if ("java.lang.Class".equals(javaTypeFqName)) {
					IType javaType = project.findType(value.decode());
					if (javaType != null) {
						Location location = findLocation(javaType);
						if (location != null) {
							return ImmutableList.of(location);
						}
					}
				}

				IType javaType = project.findType(javaTypeFqName);
				if (javaType != null) {
					// Enum value link
					if (javaType.isEnum()) {
						String enumValue = StringUtil.hyphensToUpperCase(StringUtil.camelCaseToHyphens(value.decode()));
						ImmutableList.Builder<Location> list = ImmutableList.builder();
						javaType.getFields().forEach(field -> {
							if (field.getElementName().equals(enumValue)) {
								Location location = findLocation(field);
								if (location != null) {
									list.add(location);
								}
							}
						});
						return list.build();
					}
				}

			}


		}
		return ImmutableList.of();
	}

	private List<Location> getPropertyDefintion(Collection<IMember> propertyJavaElements) {
		return propertyJavaElements.stream().map(this::findLocation).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private Location findLocation(IMember element) {
		return javaElementLocationProvider.findLocation(project, element);
	}

	private String getPropertyJavaTypeFqName(String propertyKey) {
		PropertyInfo best = propertyFinder.findBestHoverMatch(propertyKey);
		if (best != null) {
			String type = best.getType();
			// Trim down generic type if present
			int idx = type == null ? -1 : type.indexOf('<');
			return idx < 0 ? type : type.substring(0, idx);
		}
		return null;
	}

	private Collection<IMember> getPropertyJavaElements(String propertyKey) {
		PropertyInfo best = propertyFinder.findBestHoverMatch(propertyKey);
		if (best != null) {
			List<PropertySource> sources = best.getSources();
			if (sources != null) {
				ImmutableList.Builder<IMember> elements = ImmutableList.builder();
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
								method = getSetter(type, best);
							}
						}
						if (method!=null) {
							elements.add(method);
						} else if (type!=null) {
							elements.add(type);
						}
					}
				}
				return elements.build();
			}
		}
		return ImmutableList.of();
	}

	private IMethod getMethod(IType type, String methodSig) {
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
	private IMethod getSetter(IType type, PropertyInfo propertyInfo) {
		try {
			String propName = propertyInfo.getName();
			String setterName = "set"
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

	/**
	 * Convert hyphened name to camel case name. It is
	 * safe to call this on an already camel-cased name.
	 */
	private String toCamelCase(String name) {
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

}
