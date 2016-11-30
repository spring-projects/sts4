/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.yaml.completions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.ide.vscode.application.properties.metadata.hover.AbstractPropertyRenderableProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.Type;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil.EnumCaseMode;
import org.springframework.ide.vscode.application.properties.metadata.types.TypedProperty;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.util.Renderable;

import com.google.common.collect.ImmutableList;


/**
 * Example used as reference for explaingin the meaning of the instance variables:
 *
 * foo.bar[0].children.wavelen
 */
public class JavaTypeNavigationHoverInfo extends AbstractPropertyRenderableProvider {


	private static final Logger LOG = Logger.getLogger(JavaTypeNavigationHoverInfo.class.getName());

	/**
	 * Property expression that represents the full path to the point being hovered over.
	 * E.g. foo.bar[0].data.wavelen"
	 */
	private final String id;

	/**
	 * Last sgment of the path as a property name.
	 * This is only set if the naviation is accessing a property name (so, for example, will be null for navigation into
	 * indexed element in a sequence/list)
	 * <p>
	 * Example: "wavelen"
	 */
	private final String propName;

	/**
	 * The type from which we are navigating.
	 * E.g. the type of "foo.bar[0].data"
	 */
	private Type parentType;

	/**
	 * The type at which we arrive.
	 * E.g the type of "foo.bar[0].data.wavelen"
	 */
	private Type type;

	private TypeUtil typeUtil;

	private Deprecation deprecation;

	public JavaTypeNavigationHoverInfo(String id,  String propName, Type fromType, Type toType, TypeUtil typeUtil) {
		this.id = id;
		this.propName = propName;
		this.parentType = fromType;
		this.type = toType;
		this.typeUtil = typeUtil;
		//Note: If you are considerind caching the result of this method... don't.
		//The rendered html itself is cached by the superclass, which means this method only gets called once.
		Map<String, TypedProperty> props = typeUtil.getPropertiesMap(parentType, EnumCaseMode.ALIASED, BeanPropertyNameMode.ALIASED);
		if (props!=null) {
			TypedProperty prop = props.get(propName);
			if (prop!=null) {
				this.deprecation = prop.getDeprecation();
			}
		}
	}

//	@Override
//	protected String renderAsHtml() {
//		JavaTypeLinks jtLinks = new JavaTypeLinks(this);
//		HtmlBuffer html = new HtmlBuffer();
//
//		html.raw("<b>");
//		html.text(id);
//		html.raw("</b>");
//		html.raw("<br>");
//
//		if (type!=null) {
//			jtLinks.javaTypeLink(html, typeUtil, type);
//		} else {
//			jtLinks.javaTypeLink(html, typeUtil.getJavaProject(), Object.class.toString());
//		}
//
//		if (isDeprecated()) {
//			html.raw("<br><br>");
//			html.bold("Deprecated!");
//		}
//
//		//				String deflt = formatDefaultValue(data.getDefaultValue());
//		//				if (deflt!=null) {
//		//					html.raw("<br><br>");
//		//					html.text("Default: ");
//		//					html.raw("<i>");
//		//					html.text(deflt);
//		//					html.raw("</i>");
//		//				}
//
//		String description = getDescription();
//		if (description!=null) {
//			html.raw("<br><br>");
//			html.raw(description);
//		}
//
//		return html.toString();
//	}

	@Override
	protected boolean isDeprecated() {
		return deprecation!=null;
	}

	@Override
	protected Renderable getDescription() {
		try {
			List<IJavaElement> jes = getAllJavaElements();
			if (jes!=null) {
				for (IJavaElement je : jes) {
					if (je instanceof IMember) {
						IJavadoc javadoc = je.getJavaDoc();
						if (javadoc != null) {
							return javadoc.getRenderable();
						}			
					}
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public List<IJavaElement> getJavaElements() {
		if (propName!=null) {
			if (TypeUtil.isMap(parentType)) {
				Type enumType = typeUtil.getKeyType(parentType);
				if (typeUtil.isEnum(enumType)) {
					IField f = typeUtil.getEnumConstant(enumType, propName);
					if (f!=null) {
						return ImmutableList.of(f);
					}
				}
			} else {
				IJavaElement je;
				Type beanType = parentType;
				je = typeUtil.getSetter(beanType, propName).get();
				if (je!=null) {
					return Collections.singletonList(je);
				}
				je = typeUtil.getGetter(beanType, propName);
				if (je!=null) {
					return Collections.singletonList(je);
				}
				je = typeUtil.getField(beanType, propName);
				if (je!=null) {
					return Collections.singletonList(je);
				}
			}
		}
		return Collections.emptyList();
	}

	private List<IJavaElement> getAllJavaElements() {
		if (propName!=null) {
			Type beanType = parentType;
			if (TypeUtil.isMap(beanType)) {
				Type keyType = typeUtil.getKeyType(beanType);
				if (keyType!=null && typeUtil.isEnum(keyType)) {
					IField field = typeUtil.getEnumConstant(keyType, propName);
					if (field!=null) {
						return ImmutableList.of(field);
					}
				}
			} else {
				ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>(3);
				maybeAdd(elements, typeUtil.getField(beanType, propName));
				maybeAdd(elements, typeUtil.getSetter(beanType, propName).get());
				maybeAdd(elements, typeUtil.getGetter(beanType, propName));
				if (!elements.isEmpty()) {
					return elements;
				}
			}
		}
		return ImmutableList.of();
	}

	private void maybeAdd(ArrayList<IJavaElement> elements, IJavaElement e) {
		if (e!=null) {
			elements.add(e);
		}
	}

	@Override
	protected Object getDefaultValue() {
		//Not supported
		return null;
	}

	@Override
	protected IJavaProject getJavaProject() {
		return typeUtil.getJavaProject();
	}

	@Override
	protected String getType() {
		return typeUtil.niceTypeName(type);
	}

	@Override
	protected String getDeprecationReason() {
		if (deprecation!=null) {
			return deprecation.getReason();
		}
		return null;
	}

	@Override
	protected String getId() {
		return id;
	}

	@Override
	protected String getDeprecationReplacement() {
		if (deprecation!=null) {
			return deprecation.getReplacement();
		}
		return null;
	}
}