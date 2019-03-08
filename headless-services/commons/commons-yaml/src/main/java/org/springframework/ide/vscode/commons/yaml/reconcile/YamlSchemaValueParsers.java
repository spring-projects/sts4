/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileException;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.ValueParser;

/**
 * Reusable value parsers and helpers that are somewhat specific
 * to yaml schema validation.
 */
public class YamlSchemaValueParsers {

	public static final ValueParser OPT_STRING = (s) -> {
		if (StringUtil.hasText(s)) {
			return s;
		} else {
			throw new ReconcileException("Empty optional String attribute is useless and can be omitted.", YamlSchemaProblems.EMPTY_OPTIONAL_STRING);
		}
	};
}
