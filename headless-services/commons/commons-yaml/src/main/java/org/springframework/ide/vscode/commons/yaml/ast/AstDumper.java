/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.ast;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class AstDumper {

	public static void dump(Node node, int indent) {
		if (node instanceof MappingNode) {
			for (NodeTuple entry : ((MappingNode)node).getValue()) {
				println(indent, NodeUtil.asScalar(entry.getKeyNode())+":");
				dump(entry.getValueNode(), indent+1);
			}
		} else if (node instanceof SequenceNode) {
			for (Node el : ((SequenceNode)node).getValue()) {
				println(indent, "[");
				dump(el, indent+1);
				println(indent, "]");
			}
		} else if (node instanceof ScalarNode) {
			println(indent, NodeUtil.asScalar(node));
		} else {
			println(indent, "???"+node.getClass().getSimpleName());
		}
	}

	private static void println(int indent, String string) {
		indent(indent);
		System.out.println(string);
	}

	private static void indent(int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.print("  ");
		}
	}
}
