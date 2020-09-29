/*******************************************************************************
 * Copyright (c) 2016 Spring IDE Developers
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial public API
 *     
 *******************************************************************************/
package org.springframework.ide.eclipse.imports.internal.statics;

public abstract class StaticImportCatalogue {

	public static final StaticImportCatalogue DEFAULT_CATALOGUE = new StaticImportCatalogue() {

		@Override
		public String[] getCatalogue() {
			return FAVOURITES;
		}
	};

	public static String[] FAVOURITES = new String[] { "org.assertj.core.api.Assertions", "org.mockito.Matchers",
			"org.mockito.Mockito",
			"org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders",
			"org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors",
			"org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers",
			"org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers",
			"org.springframework.restdocs.mockmvc.MockMvcRestDocumentation",
			"org.springframework.test.web.client.match.MockRestRequestMatchers",
			"org.springframework.test.web.client.response.MockRestResponseCreators",
			"org.springframework.test.web.servlet.request.MockMvcRequestBuilders",
			"org.springframework.test.web.servlet.result.MockMvcResultHandlers",
			"org.springframework.test.web.servlet.result.MockMvcResultMatchers", "org.hamcrest.CoreMatchers",
			"org.junit.Assert" };

	public abstract String[] getCatalogue();
}
