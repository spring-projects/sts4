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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileException;
import org.springframework.ide.vscode.commons.util.EnumValueParser;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;

public class CFServicesValueParser extends EnumValueParser {

	public CFServicesValueParser(String typeName, Callable<Collection<String>> values) {
		super(typeName, true /*CF value parsers are potentially long running*/, values);
	}

	@Override
	protected String createErrorMessage(String parseString, Collection<String> values) {
		return "There is no service instance called '" + parseString + "'. Available service instances are: " + values;
	}

	@Override
	protected Exception errorOnParse(String message) {
		// Parse errors should be indicated differently than regular schema
		// problems (e.g. unknown service may be a warning)
		return new ReconcileException(message, ManifestYamlSchemaProblemsTypes.UNKNOWN_SERVICES_PROBLEM);
	}

	@Override
	protected Exception errorOnBlank(String message) {
		// Blank errors should be regular schema problems
		return new ReconcileException(message, YamlSchemaProblems.SCHEMA_PROBLEM);
	}
}
