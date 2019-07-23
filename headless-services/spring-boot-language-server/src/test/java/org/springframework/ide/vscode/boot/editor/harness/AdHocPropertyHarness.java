/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.editor.harness;

import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.util.FuzzyMap;

public class AdHocPropertyHarness {

	private FuzzyMap<PropertyInfo> adHocProperties = new FuzzyMap<PropertyInfo>() {
		@Override
		protected String getKey(PropertyInfo entry) {
			return entry.getId();
		}
		
	};

	protected final ProjectBasedPropertyIndexProvider adHocIndexProvider = project -> adHocProperties;

	public ProjectBasedPropertyIndexProvider getIndexProvider() {
		return adHocIndexProvider;
	}

	public void add(String adHocPropertyId) {
		adHocProperties.add(new PropertyInfo(adHocPropertyId, null, null, null, null, null, null, null, null, null, null));
	}
}
