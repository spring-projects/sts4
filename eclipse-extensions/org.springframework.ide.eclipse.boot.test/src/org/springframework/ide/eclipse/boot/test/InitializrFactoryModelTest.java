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
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.initializr.HttpRedirectionException;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

public class InitializrFactoryModelTest {

	public static class MockModel {
		public final String url;
		public MockModel(String url) {
			super();
			this.url = url;
		}
	}

	@Test
	public void handlesHttpRedirectionException() throws Exception {
		InitializrFactoryModel<MockModel> factoryModel = new InitializrFactoryModel<MockModel>((url) -> {
			if (url.startsWith("https")) {
				return new MockModel(url);
			} else {
				throw new HttpRedirectionException(url.replaceFirst("http", "https"));
			}
		});

		factoryModel.getServiceUrlField().setValue("http"+"://accept.only.https.com");
		ACondition.waitFor("Follows the redirection", 200, () -> {
			assertEquals("https://accept.only.https.com", factoryModel.getServiceUrlField().getValue());
			assertEquals(factoryModel.getModel().getValue().url, "https://accept.only.https.com");
		});
	}

	@Test
	public void handlesWrappedHttpRedirectionException() throws Exception {
		//Check that it also works if the exception becomes wrapped, as may happen if it passes
		// through futures or caches or whatnot.
		InitializrFactoryModel<MockModel> factoryModel = new InitializrFactoryModel<MockModel>((url) -> {
			if (url.startsWith("https")) {
				return new MockModel(url);
			} else {
				throw new ExecutionException(new HttpRedirectionException(url.replaceFirst("http", "https")));
			}
		});

		factoryModel.getServiceUrlField().setValue("http"+"://accept.only.https.com");
		ACondition.waitFor("Follows the redirection", 200, () -> {
			assertEquals("https://accept.only.https.com", factoryModel.getServiceUrlField().getValue());
			assertEquals(factoryModel.getModel().getValue().url, "https://accept.only.https.com");
		});
	}

}
