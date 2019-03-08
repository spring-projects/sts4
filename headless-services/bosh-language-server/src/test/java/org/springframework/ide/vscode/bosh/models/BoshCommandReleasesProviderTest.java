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
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.bosh.BoshCliConfig;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

import com.google.common.collect.ImmutableList;

public class BoshCommandReleasesProviderTest {

	private static final String MOCK_DATA_RSRC = "/cmd-out/releases.json";
	private BoshCliConfig cliConfig = new BoshCliConfig();
	public BoshCommandReleasesProvider provider = Mockito.spy(new BoshCommandReleasesProvider(cliConfig));

	@Before
	public void setup() throws Exception {
		Mockito.doReturn(IOUtil.toString(BoshCommandCloudConfigProviderTest.class.getResourceAsStream(MOCK_DATA_RSRC)))
			.when(provider).executeCommand(Mockito.any());
	}

	@Test
	public void getReleases() throws Exception {
		assertEquals(ImmutableList.of(new ReleaseData("learn-bosh", "0+dev.2")),
				provider.getModel(mock(DynamicSchemaContext.class)).getReleases()
		);
	}

}
