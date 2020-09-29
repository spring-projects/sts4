/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.utils;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.live.model.AbstractLiveBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansResource;
import org.springframework.ide.eclipse.beans.ui.live.model.SpringResource;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

public class LiveBeanUtil {
	
	/**
	 * Navigates to the type IN the resource definition of the bean, as opposed to actual bean
	 * type.
	 * <p/> 
	 * This method specifically looks at the resource attribute in the live bean to obtain a type.
	 * <p/>
	 * This is in contrast to {@link #navigateToType(LiveBean)}, where navigation
	 * occurs to the actual type of the bean. 
	 * <p/>
	 * Type of the Bean and the type in the Resource Definition of the Bean may NOT
	 * necessarily be the same.
	 * 
	 * @param bean
	 */
	public static void navigateToResource(AbstractLiveBeansModelElement element) {
		LiveBean liveBean = null;

		if (element instanceof LiveBean) {
			liveBean = (LiveBean) element;
		} else if (element instanceof LiveBeanRelation) {
			liveBean = ((LiveBeanRelation) element).getBean();
		}
		
		// Parse the type from the resource in the live bean and then navigate to that type
		if (liveBean != null) {
			TypeLookup typeLookup = liveBean.getTypeLookup();
			String resource = liveBean.getResource();
			String parsedType = parseType(resource, typeLookup);
			openInEditor(typeLookup, parsedType);
		} else if (element instanceof LiveBeansResource) {
			LiveBeansResource resource = (LiveBeansResource) element;
			TypeLookup typeLookup = resource.getTypeLookup();
			String resourceVal = null;
			if (resource.getAttributes() != null) {
				resourceVal = resource.getAttributes().get(LiveBean.ATTR_RESOURCE);
			}
			if (resourceVal == null) {
				resourceVal = resource.getLabel();
			}
			if (typeLookup != null && resourceVal != null) {
				String parsedType = parseType(resourceVal, typeLookup);
				openInEditor(typeLookup, parsedType);
			}
		}
	}

	/**
	 * Navigates to the bean TYPE in the live bean. It does NOT look at the resource
	 * definition, UNLESS the type happens to be a proxy. In this case, it will
	 * navigate to the resource definition.
	 * 
	 * @param bean
	 */
	public static void navigateToType(LiveBean bean) {
		TypeLookup appName = bean.getTypeLookup();
		String beanClass = bean.getBeanType();
		if (appName != null) {
			// Remove $$EnhancerBySpringCGLIB$$
			beanClass = stripCGLib(beanClass);
			if (beanClass != null && beanClass.trim().length() > 0) {
				if (beanClass.startsWith("com.sun.proxy")) {
					// Special case for proxy beans, extract the type
					// from the resource field
					navigateToResource(bean);
				} else {
					openInEditor(appName, beanClass.replace('$', '.'));
				}
			} else {
				// No type field, so infer class from bean ID
				openInEditor(appName, bean.getId());
			}
		}
	}

	private static String stripCGLib(String beanClass) {
		if (beanClass != null) {
			int chop = beanClass.indexOf("$$EnhancerBySpringCGLIB$$");
			if (chop >= 0) {
				beanClass = beanClass.substring(0, chop);
			}

			chop = beanClass.indexOf("$$Lambda$");
			if (chop >= 0) {
				beanClass = beanClass.substring(0, chop);
			}
		}

		return beanClass;
	}

	private static String parseType(String resource, TypeLookup typeLookup) {
		if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
			SpringResource springResource = new SpringResource(resource, typeLookup.getProject());
			return springResource.getClassName();
		}
		return null;
	}

	private static void openInEditor(TypeLookup workspaceContext, String className) {
		if (className != null) {
			IType type = workspaceContext.findType(className);
			if (type != null) {
				SpringUIUtils.openInEditor(type);
			}
		}
	}
}
