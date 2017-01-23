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
package org.springframework.ide.vscode.manifest.yaml;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.ide.vscode.commons.util.EnumValueParser;

public class CFServicesValueParser extends EnumValueParser {

	public CFServicesValueParser(String typeName, Callable<Collection<String>> values) {
		super(typeName, values);
	}

	protected String createErrorMessage(String parseString, Collection<String> values) {
		return "There is no service instance called '" + parseString + "'. Available service instances are: " + values;
	}
	
	protected String createBlankTextErrorMessage() {
		return "At least one service instance name must be specified";
	}

}
