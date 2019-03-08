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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.bosh.BoshCliConfig;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class BoshCommandStemcellsProviderTest {

	private static final String MOCK_DATA_RSRC = "/cmd-out/stemcells.json";
	private BoshCliConfig cliConfig = new BoshCliConfig();
	public BoshCommandStemcellsProvider provider = Mockito.spy(new BoshCommandStemcellsProvider(cliConfig));

	@Before
	public void setup() throws Exception {
		Mockito.doReturn(IOUtil.toString(BoshCommandCloudConfigProviderTest.class.getResourceAsStream(MOCK_DATA_RSRC)))
			.when(provider).executeCommand(Mockito.any());
	}

	@Test public void getStemcellNames() throws Exception {
		assertEquals(ImmutableSet.of(
				"bosh-vsphere-esxi-centos-7-go_agent",
				"bosh-vsphere-esxi-ubuntu-trusty-go_agent"
			),
			provider.getModel(mock(DynamicSchemaContext.class))
				.getStemcellNames()
		);
	}

	@Test public void getStemcells() throws Exception {
		assertEquals(ImmutableList.of(
				new StemcellData("bosh-vsphere-esxi-centos-7-go_agent", "3421.11", "centos-7"),
				new StemcellData("bosh-vsphere-esxi-ubuntu-trusty-go_agent", "3421.11", "ubuntu-trusty")
			),
			provider.getModel(mock(DynamicSchemaContext.class)).getStemcells()
		);
	}

	@Test public void getOss() throws Exception {
		assertEquals(ImmutableSet.of("centos-7", "ubuntu-trusty"),
			provider.getModel(mock(DynamicSchemaContext.class)).getStemcellOss());
	}

	@Test public void getVersions() throws Exception {
		assertEquals(ImmutableSet.of("3421.11"),
			provider.getModel(mock(DynamicSchemaContext.class)).getVersions());
	}

//	@Test public void defaultCliConfig() throws Exception {
//		assertEquals(ImmutableList.of(
//				new StemcellData("bosh-vsphere-esxi-centos-7-go_agent", "3421.11", "centos-7"),
//				new StemcellData("bosh-vsphere-esxi-ubuntu-trusty-go_agent", "3421.11", "ubuntu-trusty")
//			),
//			provider.getModel(mock(DynamicSchemaContext.class)).getStemcells()
//		);
//		verify(provider).executeCommand(eq(new ExternalCommand("bosh", "stemcells", "--json")));
//	}

	@Test public void obeysCliConfigCommandAndTarget() throws Exception {
		JsonElement settings = settings(ImmutableMap.of("bosh", ImmutableMap.of("cli",
				ImmutableMap.of(
						"command", "alternate-command",
						"target", "some-target"
				)
		)));
		cliConfig.handleConfigurationChange(new Settings(settings));
		assertEquals(ImmutableList.of(
				new StemcellData("bosh-vsphere-esxi-centos-7-go_agent", "3421.11", "centos-7"),
				new StemcellData("bosh-vsphere-esxi-ubuntu-trusty-go_agent", "3421.11", "ubuntu-trusty")
			),
			provider.getModel(mock(DynamicSchemaContext.class)).getStemcells()
		);
		verify(provider).executeCommand(eq(new ExternalCommand("alternate-command", "-e", "some-target", "stemcells", "--json")));
	}

	@Test public void obeysCliConfigTarget() throws Exception {
		JsonElement settings = settings(ImmutableMap.of("bosh", ImmutableMap.of("cli",
				ImmutableMap.of(
						"command", "alternate-command",
						"target", "explicit-target"
				)
		)));
		cliConfig.handleConfigurationChange(new Settings(settings));
		assertEquals(ImmutableList.of(
				new StemcellData("bosh-vsphere-esxi-centos-7-go_agent", "3421.11", "centos-7"),
				new StemcellData("bosh-vsphere-esxi-ubuntu-trusty-go_agent", "3421.11", "ubuntu-trusty")
			),
			provider.getModel(mock(DynamicSchemaContext.class)).getStemcells()
		);
		verify(provider).executeCommand(eq(new ExternalCommand("alternate-command", "-e", "explicit-target", "stemcells", "--json")));
	}

	private JsonElement settings(Object configObject) {
		Gson gson = new Gson();
		return gson.toJsonTree(configObject);
	}
}
