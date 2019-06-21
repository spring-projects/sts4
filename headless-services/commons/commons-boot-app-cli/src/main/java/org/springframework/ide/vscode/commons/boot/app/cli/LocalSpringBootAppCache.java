/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.ide.vscode.commons.util.MemoizingProxy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings("restriction")
public class LocalSpringBootAppCache {

	private static final Duration EXPIRE_AFTER = Duration.ofMillis(500); //Limits rate at which we refresh list of apps
	private long nextRefreshAfter = Long.MIN_VALUE;
	private final MemoizingProxy.Builder<LocalSpringBootApp> memoizingProxyBuilder = MemoizingProxy.builder(LocalSpringBootApp.class, Duration.ofMillis(4500), VirtualMachineDescriptor.class);

	private ImmutableMap<VirtualMachineDescriptor, SpringBootApp> apps = ImmutableMap.of();

	public synchronized Collection<SpringBootApp> getAllRunningJavaApps() {
		if (System.currentTimeMillis()>=nextRefreshAfter) {
			refresh();
		}
		return ImmutableList.copyOf(apps.values());
	}

	private void refresh() {
		List<VirtualMachineDescriptor> currentVms = VirtualMachine.list();
		ImmutableMap.Builder<VirtualMachineDescriptor, SpringBootApp> newAppsBuilder = ImmutableMap.builder();
		for (VirtualMachineDescriptor vm : currentVms) {
			SpringBootApp existingApp = apps.get(vm);
			if (existingApp!=null) {
				newAppsBuilder.put(vm, existingApp);
			} else {
				try {
					LocalSpringBootApp localApp = memoizingProxyBuilder.newInstance(vm);
					newAppsBuilder.put(vm, localApp);
				} catch (Exception e) {
					//Ignore problems attaching to a VM. We will try again on next polling loop, if vm still exists.
					//The most likely cause is that the VM already died since we obtained a reference to it.
				}
			}
		}
		HashSet<VirtualMachineDescriptor> oldVms = new HashSet<>(apps.keySet());
		ImmutableMap<VirtualMachineDescriptor, SpringBootApp> newApps = newAppsBuilder.build();
		oldVms.removeAll(newApps.keySet());
		for (VirtualMachineDescriptor oldVm : oldVms) {
			apps.get(oldVm).dispose();
		}
		apps = newApps;
		nextRefreshAfter = System.currentTimeMillis() + EXPIRE_AFTER.toMillis();
	}

}
