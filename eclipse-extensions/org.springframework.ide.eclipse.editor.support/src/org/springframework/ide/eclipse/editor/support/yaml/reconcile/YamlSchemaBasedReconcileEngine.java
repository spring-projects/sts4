/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.reconcile;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YamlSchema;

/**
 * Note: Do NOT subclass this. It is meant to be 'complete' the parser and schema
 * are meant to be the drivers of its entire behavior. Subclassing should not be required.
 * <p>
 * Instead if we require more capabilities then we should extend the 'powers' of
 * the schema to express constraints.
 *
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