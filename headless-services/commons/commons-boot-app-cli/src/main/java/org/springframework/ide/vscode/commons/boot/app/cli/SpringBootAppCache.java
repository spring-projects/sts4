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
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.collect.ImmutableMap;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class SpringBootAppCache {

	private static final Duration EXPIRE_AFTER = Duration.ofMillis(500); //Limits rate at which we refresh list of apps
	private long nextRefreshAfter = Long.MIN_VALUE;

	private ImmutableMap<VirtualMachineDescriptor, SpringBootApp> apps = ImmutableMap.of();

	public synchronized Collection<SpringBootApp> getAllRunningJavaApps() {
		if (System.currentTimeMillis()>=nextRefreshAfter) {
			refresh();
		}
		return apps.values();
	}

	private void refresh() {
		List<VirtualMachineDescriptor> currentVms = VirtualMachine.list();
		HashSet<VirtualMachineDescriptor> oldVms = new HashSet<>(apps.keySet());
		ImmutableMap.Builder<VirtualMachineDescriptor, SpringBootApp> newApps = ImmutableMap.builder();
		for (VirtualMachineDescriptor vm : currentVms) {
			oldVms.remove(vm);
			SpringBootApp existingApp = apps.get(vm);
			if (existingApp!=null) {
				newApps.put(vm, existingApp);
			} else {
				try {
					newApps.put(vm, new SpringBootApp(vm));
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}
		for (VirtualMachineDescriptor oldVm : oldVms) {
			apps.get(oldVm).dispose();
		}
		apps = newApps.build();
		nextRefreshAfter = System.currentTimeMillis() + EXPIRE_AFTER.toMillis();
	}
}
