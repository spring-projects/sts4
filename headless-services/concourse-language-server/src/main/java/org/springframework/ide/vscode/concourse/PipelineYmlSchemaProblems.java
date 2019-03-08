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
package org.springframework.ide.vscode.concourse;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

import static org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems.*;

public class PipelineYmlSchemaProblems {
	public static final ProblemType UNUSED_RESOURCE = problemType("PipelineYamlUnusedResource", ProblemSeverity.ERROR);
	public static final ProblemType UNKNOWN_GITHUB_ENTITY = problemType("UnknownGithubEntitity", ProblemSeverity.WARNING);
}
