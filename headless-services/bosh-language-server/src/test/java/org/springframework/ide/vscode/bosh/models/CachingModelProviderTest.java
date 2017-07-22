/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

@SuppressWarnings("unchecked")
public class CachingModelProviderTest {

	@Test public void goodValuesAreCached() throws Exception {
		DynamicModelProvider<String> modelProvider = mock(DynamicModelProvider.class);
		when(modelProvider.getModel(any())).thenReturn("RESULT");

		DynamicModelProvider<String> cached = new CachingModelProvider<>(modelProvider);

		assertEquals("RESULT", cached.getModel(null));
		assertEquals("RESULT", cached.getModel(null));
		assertEquals("RESULT", cached.getModel(null));

		verify(modelProvider, times(1)).getModel(any());
	}

	@Test public void timeoutExceptionsAreCached() throws Exception {
		DynamicModelProvider<String> modelProvider = mock(DynamicModelProvider.class);
		when(modelProvider.getModel(any())).thenThrow(new TimeoutException("timed out"));

		DynamicModelProvider<String> cached = new CachingModelProvider<>(modelProvider);
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
