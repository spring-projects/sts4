/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.jface.preference.IPreferenceStore;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

/**
 * A 'FactoryModelModel' for NewSpringBootWizardModel. I.e. this is a model for the NewSpringBootWizard
 * the dynamically creates a {@link NewSpringBootWizardModel} based on some 'static' inputs.
 *
 * @author Kris De Volder
 */
public class NewSpringBootWizardFactoryModel {

	public static InitializrFactoryModel<NewSpringBootWizardModel> create(URLConnectionFactory urlConnectionFactory, IPreferenceStore prefs) {
		return new InitializrFactoryModel<>((url) -> {
			if (StringUtil.hasText(url)) {
				return new NewSpringBootWizardModel(urlConnectionFactory, url, prefs);
			} else {
				throw new IllegalArgumentException("No URL entered");
			}
		});
	}

}
