/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.rewrite.test;

import org.openrewrite.Recipe;
import org.openrewrite.java.ChangePackage;

public class OssPackageRecipe extends Recipe {
	
	public OssPackageRecipe() {
		doNext(new ChangePackage("com.example", "org.example", true));
	}

	@Override
	public String getDisplayName() {
		return "Test recipe class coming from the Jar";
	}

}
