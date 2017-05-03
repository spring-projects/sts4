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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.constraints.Constraint;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * Constraints for Manifest YAML structure
 *
 * @author Alex Boyko
 *
 */
public class ManifestConstraints {

	public static Constraint exclusiveWith(String... propertyIds) {
		return (dc, parent, node, type, problems) -> {
			Set<String> keys = new HashSet<>();
			Node root = dc.getAST().getNodes().get(0);
			// First add keys from the root node
			keys.addAll(NodeUtil.getScalarKeys(root));
			if (root == parent) {
				// Add keys from all applications
				SequenceNode apps = NodeUtil.asSequence(NodeUtil.getProperty(root, "applications"));
				if (apps != null) {
					apps.getValue().forEach(n -> keys.addAll(NodeUtil.getScalarKeys(n)));
				}
			} else {
				// Now add keys from application node, thus application node keys would replace root node keys if they present in both nodes
				keys.addAll(NodeUtil.getScalarKeys(parent));
			}
			Arrays.stream(propertyIds).filter(id -> keys.contains(id)).findFirst().ifPresent(propertyId -> {
				// Find key node, because the node parameter is the value node
				MappingNode mapNode = (MappingNode) parent;
				mapNode.getValue().stream().filter(t -> t.getValueNode() == node).findFirst().ifPresent(t -> {
					Node keyNode = t.getKeyNode();
					int start = keyNode.getStartMark().getIndex();
					int end = keyNode.getEndMark().getIndex();
					problems.accept(
							new ReconcileProblemImpl(ManifestYamlSchemaProblemsTypes.MUTUALLY_EXCLUSIVE_PROPERTY_PROBLEM,
									"Property cannot co-exist with property '" + propertyId + "'", start, end - start));
				});
			});
		};
	}

}
