/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.modulith;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AppModules {
	
	List<AppModule> modules;
	
	public AppModules(List<AppModule> modules) {
		this.modules = modules;
	}
	
	public Optional<AppModule> getModuleNotExposingType(String targetPackage, String referenceFqName) {
		return getModuleForType(referenceFqName).flatMap(refModule -> {
			if (refModule.namedInterfaces().contains(referenceFqName)) {
				return Optional.empty();
			} else {
				if (getModuleForPackage(targetPackage).map(targetModule -> targetModule == refModule).orElse(false)) {
					// same module for target package and reference type
					return Optional.empty();
				}
				return Optional.of(refModule);
			}
		});
	}
	
	private Optional<AppModule> getModuleForPackage(String pkgName) {
		return generatePackageHierarchy(pkgName)
			.stream()
			.map(p -> modules.stream().filter(m -> m.basePackage().equals(p)).findFirst())
			.filter(o -> o.isPresent())
			.map(o -> o.get())
			.findFirst();
	}
	
	private Optional<AppModule> getModuleForType(String typeFqName) {
		return getModuleForPackage(ModulithService.getPackageNameFromTypeFQName(typeFqName));
	}
	
	private List<String> generatePackageHierarchy(String pkgName) {
		List<String> packageHierarchy = new ArrayList<>();
		packageHierarchy.add(pkgName);
		for (int i = pkgName.length() - 1; i >= 0; i--) {
			if (pkgName.charAt(i) == '.') {
				packageHierarchy.add(pkgName.substring(0, i));
			}
		}
		return packageHierarchy;
	}

}
