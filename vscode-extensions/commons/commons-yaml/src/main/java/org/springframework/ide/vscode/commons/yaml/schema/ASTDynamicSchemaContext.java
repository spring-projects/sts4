/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.Collections;
import java.util.Set;

import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;

import com.google.common.collect.ImmutableSet;

/**
 * Adapts a SnakeYaml ast node as a {@link DynamicSchemaContext} (so it
 * can be used in YamlSchema based reconciler.
 *
 * @author Kris De Volder
 */
public class ASTDynamicSchemaContext extends CachingSchemaContext {

	private MappingNode mapNode;

	public ASTDynamicSchemaContext(MappingNode map) {
		this.mapNode = map;
	}

	@Override
	protected Set<String> computeDefinedProperties() {
		if (mapNode!=null) {
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			for (NodeTuple entry : mapNode.getValue()) {
				String key = NodeUtil.asScalar(entry.getKeyNode());
				if (key!=null) { //key not a scalar? => something funky so skip it
					builder.add(key);
				}
			}
			return builder.build();
		}
		return Collections.emptySet();
	}
}
