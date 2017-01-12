/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public final class YamlSchemaBasedReconcileEngine extends YamlReconcileEngine {
	private final YamlSchema schema;

	public YamlSchemaBasedReconcileEngine(YamlASTProvider parser, YamlSchema schema) {
		super(parser);
		this.schema = schema;
	}

	@Override
	protected ReconcileProblem syntaxError(String msg, int offset, int length) {
		return YamlSchemaProblems.syntaxProblem(msg, offset, length);
	}

	@Override
	protected YamlASTReconciler getASTReconciler(IDocument doc, IProblemCollector problems) {
		return new SchemaBasedYamlASTReconciler(problems, schema);
	}
}