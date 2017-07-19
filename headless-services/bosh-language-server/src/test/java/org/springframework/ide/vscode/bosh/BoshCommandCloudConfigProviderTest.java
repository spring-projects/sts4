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
package org.springframework.ide.vscode.bosh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.bosh.mocks.MockCloudConfigProvider;
import org.springframework.ide.vscode.bosh.models.BoshCommandCloudConfigProvider;
import org.springframework.ide.vscode.bosh.models.CloudConfigModel;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

import com.google.common.collect.ImmutableMultiset;

public class BoshCommandCloudConfigProviderTest {

	public final MockCloudConfigProvider mockProvider = new MockCloudConfigProvider();

// For local testing only... in CI builds we don't have the means to use a real bosh director and cli.
//	private BoshCommandCloudConfigProvider realProvider = new BoshCommandCloudConfigProvider();

	@Test public void getVMTypes() throws Exception {
		BoshCommandCloudConfigProvider provider = mockProvider;
		DynamicSchemaContext dc = Mockito.mock(DynamicSchemaContext.class);
		CloudConfigModel cloudConfig = provider.getModel(dc);

		assertEquals(ImmutableMultiset.of("default", "large"), cloudConfig.getVMTypes());
	}

}
