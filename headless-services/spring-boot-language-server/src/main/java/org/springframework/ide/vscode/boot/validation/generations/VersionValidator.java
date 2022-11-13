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
package org.springframework.ide.vscode.boot.validation.generations;

import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.Version;

public interface VersionValidator {

	/**
	 * 
	 * @param springProject contains information about the spring project associated
	 *                      with the generation and version to validate
	 * @param generation    to validate
	 * @param version       to validate
	 * @return validation if application. Null otherwise
	 * @throws Exception
	 */
	VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation, Version version)
			throws Exception;

}
