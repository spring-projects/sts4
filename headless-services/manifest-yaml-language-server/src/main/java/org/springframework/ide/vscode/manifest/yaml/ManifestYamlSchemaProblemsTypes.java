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
package org.springframework.ide.vscode.manifest.yaml;

import static org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems.problemType;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

/**
 * 
 */
public class ManifestYamlSchemaProblemsTypes {

	public static final ProblemType UNKNOWN_SERVICES_PROBLEM = problemType("UnknownServicesProblem", ProblemSeverity.WARNING);
	public static final ProblemType UNKNOWN_DOMAIN_PROBLEM = problemType("UnknownDomainProblem", ProblemSeverity.WARNING);
	public static final ProblemType UNKNOWN_STACK_PROBLEM = problemType("UnknownStackProblem", ProblemSeverity.WARNING);
	public static final ProblemType IGNORED_PROPERTY = problemType("IgnoredProperty", ProblemSeverity.WARNING);
	public static final ProblemType MUTUALLY_EXCLUSIVE_PROPERTY_PROBLEM = problemType("MutuallyExclusiveProperty",
			ProblemSeverity.ERROR);
	
}
