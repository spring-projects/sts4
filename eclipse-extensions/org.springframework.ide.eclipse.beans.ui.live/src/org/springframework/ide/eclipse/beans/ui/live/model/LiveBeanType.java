/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

/**
 * Wrapper to mark bean type in the UI
 * 
 * @author Alex Boyko
 *
 */
public class LiveBeanType implements DisplayName {
	
	private LiveBean bean;
	
	public LiveBeanType(LiveBean bean) {
		this.bean = bean;
	}
	
	public String getDisplayName() {
		String type = bean.getBeanType();
		int idx = type.indexOf("$$");
		if (idx >= 0) {
			return type.substring(0, idx);
		}
		return type;
	}
	
	@Override
	public String toString() {
		return "LiveBeanType("+bean+")";
	}

	public LiveBean getBean() {
		return bean;
	}

}
