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

import java.util.List;

import org.springframework.ide.vscode.commons.rewrite.config.CodeActionRepository;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;

public class StsTestCodeActionRepo extends CodeActionRepository {

	@Override
	public List<RecipeCodeActionDescriptor> getCodeActionDescriptors() {
		return List.of(new HelloMethodRenameProblemDescriptor());
	}

}
