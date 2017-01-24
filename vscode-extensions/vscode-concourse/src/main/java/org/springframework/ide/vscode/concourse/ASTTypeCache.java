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
package org.springframework.ide.vscode.concourse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.reconcile.ITypeCollector;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableMap;

/**
 * An implementation of {@link ITypeCollector} which keeps track of the
 * types of 'interesting' nodes in the ast.
 *
 * @author Kris De Volder
 */
public class ASTTypeCache implements ITypeCollector {

	/**
	 * Set upon commencing a reconciler session.
	 */
	private YamlFileAST currentAst = null;

	/**
	 * Collects types for the current session.
	 */
	private ImmutableMap.Builder<Node, YType> currentTypes = null;

	private final Set<YType> interestingTypes = new HashSet<>();
	private final Map<String, ImmutableMap<Node, YType>> typeIndex = new HashMap<>();

	@Override
	public void beginCollecting(YamlFileAST ast) {
		Assert.isNull("A session is already active. Concurrency isn't supported by ITypeCollector protocol", currentTypes);
		this.currentAst = ast;
		this.currentTypes = ImmutableMap.builder();
	}

	@Override
	public void endCollecting(YamlFileAST ast) {
		Assert.isLegal(currentAst==ast);
		String uri = ast.getDocument().getUri();
		typeIndex.put(uri, currentTypes.build());
		this.currentAst = null;
		this.currentTypes = null;
	}

	@Override
	public void accept(Node node, YType type) {
		if (interestingTypes.contains(type)) {
			currentTypes.put(node, type);
		}
	}

	public YType getType(YamlFileAST ast, Node node) {
		ImmutableMap<Node, YType> types = typeIndex.get(ast.getDocument().getUri());
		if (types!=null) {
			return types.get(node);
		}
		return null;
	}

	/**
	 * Declares a given YType as 'interesting'. This means that nodes of this type will be
	 * added to the index.
	 */
	public void addInterestingType(YAtomicType type) {
		this.interestingTypes.add(type);
	}


}
