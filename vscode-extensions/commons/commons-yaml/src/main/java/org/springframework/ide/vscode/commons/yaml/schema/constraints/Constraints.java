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
package org.springframework.ide.vscode.commons.yaml.schema.constraints;

import static org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems.EXTRA_PROPERTY;
import static org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems.missingProperty;
import static org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems.problem;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

import com.google.common.collect.ImmutableSet;

/**
 * Various static methods for constructing/composing {@link Constraint}s.
 *
 * @author Kris De Volder
 */
public class Constraints {

	public static Constraint requireOneOf(String... properties) {
		return new RequireOneOf(properties);
	}

	public static Constraint requireAtMostOneOf(String... properties) {
		return new RequireOneOf(properties).allowFewer(true);
	}

	static private class RequireOneOf implements Constraint {

		private final String[] _requiredProps;
		private boolean allowFewer = false;

		public RequireOneOf(String[] properties) {
			Assert.isLegal(properties.length>1);
			this._requiredProps = properties;
		}

		public Constraint allowFewer(boolean b) {
			this.allowFewer = b;
			return this;
		}

		@Override
		public void verify(Node parent, MappingNode map, YType type, Set<String> foundProps, IProblemCollector problems) {
			List<String> requiredProps = Arrays.asList(_requiredProps);
			long foundPropsCount = requiredProps.stream()
				.filter(foundProps::contains)
				.count();
			if (foundPropsCount==0) {
				if (!allowFewer) {
					problems.accept(missingProperty(
							"One of "+requiredProps+" is required for '"+type+"'", parent, map));
				}
			} else if (foundPropsCount>1) {
				//Mark each of the found keys as a violation:
				for (NodeTuple entry : map.getValue()) {
					String key = NodeUtil.asScalar(entry.getKeyNode());
					if (key!=null && requiredProps.contains(key)) {
						problems.accept(problem(EXTRA_PROPERTY,
								"Only one of "+requiredProps+" should be defined for '"+type+"'",  entry.getKeyNode()));
					}
				}
			}
		}
	}

	public static Constraint deprecated(Function<String, String> messageFormatter, String... _deprecatedNames) {
		Set<String> deprecatedNames = ImmutableSet.copyOf(_deprecatedNames);
		return (Node parent, MappingNode map, YType type, Set<String> foundProps, IProblemCollector problems) -> {
			for (NodeTuple prop : map.getValue()) {
				Node keyNode = prop.getKeyNode();
				String name = NodeUtil.asScalar(keyNode);
				if (deprecatedNames.contains(name)) {
					problems.accept(YamlSchemaProblems.deprecatedProperty(messageFormatter.apply(name), keyNode));
				}
			}
		};
	}
}