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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ChangePackage;

public class OssPackageRecipe extends Recipe {
	
	@Override
	public String getDescription() {
		return "Change package 'com.example' to 'org.example'";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new ChangePackage("com.example", "org.example", true).getVisitor();
	}

	@Override
	public String getDisplayName() {
		return "Test recipe class coming from the Jar";
	}

}
