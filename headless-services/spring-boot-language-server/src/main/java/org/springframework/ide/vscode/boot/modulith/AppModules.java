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

import java.util.List;

public final class AppModules {
	
	private List<AppModule> modules;
	
	public AppModules(List<AppModule> modules) {
		this.modules = modules;
	}
	
	public boolean isReferenceAllowed(String targetPackage, String referenceFqName) {
		String referencePackage = ModulithService.getPackageNameFromTypeFQName(referenceFqName);
		return modules
				.stream()
				.filter(m -> m.basePackage().equals(referencePackage))
				.findFirst()
				.map(m -> m.namedInterfaces().contains(referenceFqName))
				.orElse(true);
	}

}
