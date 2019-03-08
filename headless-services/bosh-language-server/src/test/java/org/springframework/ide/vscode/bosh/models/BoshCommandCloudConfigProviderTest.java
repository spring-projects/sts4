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

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.bosh.BoshCliConfig;
import org.springframework.ide.vscode.bosh.mocks.MockCloudConfigProvider;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

import com.google.common.collect.ImmutableMultiset;

public class BoshCommandCloudConfigProviderTest {

	private BoshCliConfig cliConfig = new BoshCliConfig();
	public final MockCloudConfigProvider provider = new MockCloudConfigProvider(cliConfig);

// For local testing only... in CI builds we don't have the means to use a real bosh director and cli.
//	private BoshCommandCloudConfigProvider realProvider = new BoshCommandCloudConfigProvider();

	@Test public void getStuff() throws Exception {
		DynamicSchemaContext dc = Mockito.mock(DynamicSchemaContext.class);
		CloudConfigModel cloudConfig = provider.getModel(dc);

		assertEquals(ImmutableMultiset.of("default", "large"), cloudConfig.getVMTypes());
		assertEquals(ImmutableMultiset.of("default"), cloudConfig.getNetworkNames());
		assertEquals(ImmutableMultiset.of("default", "large"), cloudConfig.getDiskTypes());
		assertEquals(ImmutableMultiset.of("default", "large"), cloudConfig.getVMTypes());
		assertEquals(ImmutableMultiset.of(), cloudConfig.getVMExtensions());
		assertEquals(ImmutableMultiset.of("z1", "z2", "z3"), cloudConfig.getAvailabilityZones());
	}

	@Test public void getStuff2() throws Exception {
		DynamicSchemaContext dc = Mockito.mock(DynamicSchemaContext.class);
		provider.readWith(() ->
			"azs:\n" +
			"- cloud_properties:\n" +
			"    datacenters:\n" +
			"    - clusters:\n" +
			"      - AppFabric: {}\n" +
			"  name: z1\n" +
			"- cloud_properties:\n" +
			"    datacenters:\n" +
			"    - clusters:\n" +
			"      - AppFabric: {}\n" +
			"  name: z2\n" +
			"- cloud_properties:\n" +
			"    datacenters:\n" +
			"    - clusters:\n" +
			"      - AppFabric: {}\n" +
			"  name: z3\n" +
			"compilation:\n" +
			"  az: z1\n" +
			"  network: default-nw\n" +
			"  reuse_compilation_vms: true\n" +
			"  vm_type: default\n" +
			"  workers: 5\n" +
			"disk_types:\n" +
			"- disk_size: 3000\n" +
			"  name: default-dsk\n" +
			"- disk_size: 50000\n" +
			"  name: large-dsk\n" +
			"networks:\n" +
			"- name: default-nw\n" +
			"  subnets:\n" +
			"  - azs:\n" +
			"    - z1\n" +
			"    - z2\n" +
			"    - z3\n" +
			"    cloud_properties:\n" +
			"      name: VLAN 40 - AF\n" +
			"    dns:\n" +
			"    - 10.192.2.10\n" +
			"    - 8.8.8.8\n" +
			"    gateway: 10.194.4.1\n" +
			"    range: 10.194.4.0/23\n" +
			"    reserved:\n" +
			"    - 10.194.4.1-10.194.4.34\n" +
			"    - 10.194.4.40-10.194.5.255\n" +
			"  type: manual\n" +
			"vm_extensions:\n" +
			"- cloud_properties: {}\n" +
			"  name: fake-vmx-1\n" +
			"- cloud_properties: {}\n" +
			"  name: fake-vmx-2\n" +
			"vm_types:\n" +
			"- cloud_properties:\n" +
			"    cpu: 2\n" +
			"    disk: 3240\n" +
			"    ram: 1024\n" +
			"  name: default-vm\n" +
			"- cloud_properties:\n" +
			"    cpu: 2\n" +
			"    disk: 30240\n" +
			"    ram: 4096\n" +
			"  name: large-vm\n"
		);

		CloudConfigModel cloudConfig = provider.getModel(dc);
		assertEquals(ImmutableMultiset.of("default-vm", "large-vm"), cloudConfig.getVMTypes());
		assertEquals(ImmutableMultiset.of("default-nw"), cloudConfig.getNetworkNames());
		assertEquals(ImmutableMultiset.of("default-dsk", "large-dsk"), cloudConfig.getDiskTypes());
		assertEquals(ImmutableMultiset.of("fake-vmx-1", "fake-vmx-2"), cloudConfig.getVMExtensions());
		assertEquals(ImmutableMultiset.of("z1", "z2", "z3"), cloudConfig.getAvailabilityZones());

	}

}
