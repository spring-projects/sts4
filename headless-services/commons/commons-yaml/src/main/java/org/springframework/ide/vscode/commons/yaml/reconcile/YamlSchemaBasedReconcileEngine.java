/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.reconcile;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.quickfix.YamlQuickfixes;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public final class YamlSchemaBasedReconcileEngine extends YamlReconcileEngine {
	private final YamlSchema schema;

	private YamlQuickfixes quickfixes;

	private ApplicationContext appContext;

	public YamlSchemaBasedReconcileEngine(YamlASTProvider parser, YamlSchema schema, YamlQuickfixes quickfixes, ApplicationContext appContext) {
		super(parser);
		this.schema = schema;
		this.quickfixes = quickfixes;
		this.appContext = appContext;
	}

	@Override
	protected ReconcileProblem syntaxError(String msg, int offset, int length) {
		return YamlSchemaProblems.syntaxProblem(msg, offset, length);
	}

	@Override
	protected YamlASTReconciler getASTReconciler(IDocument doc, IProblemCollector problems) {
		Collection<ITypeCollector> typeCollectors = appContext.getBeansOfType(ITypeCollector.class).values();
		ITypeCollector typeCollector = null;
		if (CollectionUtil.hasElements(typeCollectors)) {
			typeCollector = new ITypeCollector() {

				@Override
				public void endCollecting(YamlFileAST ast) {
					for (ITypeCollector c : typeCollectors) {
						c.endCollecting(ast);
					}
				}

				@Override
				public void beginCollecting(YamlFileAST ast) {
					for (ITypeCollector c : typeCollectors) {
						c.beginCollecting(ast);
					}
				}

				@Override
				public void accept(Node node, YType type, YamlPath path) {
					for (ITypeCollector c : typeCollectors) {
						c.accept(node, type, path);
					}
				}
			};
		}
		return new SchemaBasedYamlASTReconciler(problems, schema, typeCollector, quickfixes);
	}

}