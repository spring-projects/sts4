/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBean extends AbstractLiveBeansModelElement {

	public static final String ATTR_BEAN = "bean";

	public static final String ATTR_SCOPE = "scope";

	public static final String ATTR_TYPE = "type";

	public static final String ATTR_RESOURCE = "resource";

	public static final String ATTR_DEPENDENCIES = "dependencies";

	public static final String ATTR_APPLICATION = "application name";

	private final TypeLookup typeLookup;

	private final String beanId;

	private String displayName;

	private final Set<LiveBean> dependencies;

	private final Set<LiveBean> injectedInto;

	private final boolean innerBean;

	public LiveBean(TypeLookup session, String id) {
		this(session, id, false);
	}

	public LiveBean(TypeLookup typeLookup, String id, boolean innerBean) {
		this.typeLookup = typeLookup;
		this.beanId = id;
		this.innerBean = innerBean;
		dependencies = new LinkedHashSet<LiveBean>();
		injectedInto = new LinkedHashSet<LiveBean>();
		attributes.put(ATTR_BEAN, id);
	}

	public void addDependency(LiveBean dependency) {
		dependencies.add(dependency);
		dependency.injectInto(this);
	}

	public String getApplicationName() {
		return attributes.get(ATTR_APPLICATION);
	}

	public String getBeanType() {
		return attributes.get(ATTR_TYPE);
	}

	public Set<LiveBean> getDependencies() {
		return dependencies;
	}

	public String getDisplayName() {
		// compute the display name the first time it's needed
		if (displayName == null) {
			// truncate Class names and name with multiple segments
			if ((getBeanType() != null && beanId.contains(getBeanType())) || StringUtils.countMatches(beanId, ".") > 1) {
				int index = beanId.lastIndexOf('.');
				if (index >= 0) {
					displayName = beanId.substring(index + 1, beanId.length());
				}
			}
			if (displayName == null) {
				displayName = beanId;
			}
		}
		return displayName;
	}

	public String getId() {
		return beanId;
	}

	public Set<LiveBean> getInjectedInto() {
		return injectedInto;
	}

	public String getResource() {
		return attributes.get(ATTR_RESOURCE);
	}

	public String getScope() {
		return attributes.get(ATTR_SCOPE);
	}

	private void injectInto(LiveBean bean) {
		injectedInto.add(bean);
	}

	public boolean isInnerBean() {
		return innerBean;
	}

	public TypeLookup getTypeLookup() {
		return typeLookup;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiveBean) {
			LiveBean other = (LiveBean) obj;
			return this.attributes.equals(other.attributes);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return beanId == null ? super.hashCode() : beanId.hashCode();
	}	
	
	@Override
	public String toString() {
		return "LiveBean("+getId()+")";
	}

}
