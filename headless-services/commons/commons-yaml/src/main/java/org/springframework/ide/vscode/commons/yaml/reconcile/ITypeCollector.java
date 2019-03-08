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
package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.Node;

/**
 * A type collector can optionally be added to a {@link SchemaBasedYamlASTReconciler}.
 * It is notified of the types the reconciler infers for
 * any AST nodes it visits during reconciling.
 *
 * @author Kris De Volder
 */
public interface ITypeCollector {
	void beginCollecting(YamlFileAST ast);
	void accept(Node node, YType type, YamlPath path);
	void endCollecting(YamlFileAST ast);
}
