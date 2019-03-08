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
package org.springframework.ide.vscode.bosh.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

@SuppressWarnings("unchecked")
public class CachingModelProviderTest {

	interface BoxModel {
		String getContents();
	}

	@Test public void goodValuesAreCached() throws Exception {
		DynamicModelProvider<BoxModel> modelProvider = mock(DynamicModelProvider.class);
		BoxModel model = mock(BoxModel.class);
		when(modelProvider.getModel(any())).thenReturn(model);
		when(model.getContents()).thenReturn("RESULT");

		DynamicModelProvider<BoxModel> cached = new CachingModelProvider<>(modelProvider, BoxModel.class);

		assertEquals("RESULT", cached.getModel(null).getContents());
		assertEquals("RESULT", cached.getModel(null).getContents());
		assertEquals("RESULT", cached.getModel(null).getContents());

		verify(modelProvider, times(1)).getModel(any());
		verify(model, times(1)).getContents(); //model itself is also wrapped in a cache!
	}

	@Test public void timeoutExceptionsAreCached() throws Exception {
		DynamicModelProvider<String> modelProvider = mock(DynamicModelProvider.class);
		when(modelProvider.getModel(any())).thenThrow(new TimeoutException("timed out"));

		DynamicModelProvider<String> cached = new CachingModelProvider<>(modelProvider, String.class);
		for (int i = 0; i < 3; i++) {
			try {
				cached.getModel(null);
				fail("Should have thrown");
			} catch (Exception _e) {
				Throwable e = ExceptionUtil.getDeepestCause(_e);
				assertEquals(TimeoutException.class, e.getClass());
			}
		}
		verify(modelProvider, times(1)).getModel(any());
	}

}
