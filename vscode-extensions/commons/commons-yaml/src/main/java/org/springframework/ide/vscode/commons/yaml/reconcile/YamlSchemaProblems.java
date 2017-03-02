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

import java.util.Set;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableSet;

/**
 * Methods for creating reconciler problems for Schema based reconciler implementation.
 *
 * @author Kris De Volder
 */
public class YamlSchemaProblems {

	public static final ProblemType SYNTAX_PROBLEM = problemType("YamlSyntaxProblem");
	public static final ProblemType SCHEMA_PROBLEM = problemType("YamlSchemaProblem");
	public static final ProblemType DEPRECATED_PROPERTY = problemType("DeprecatedProperty", ProblemSeverity.WARNING);
	public static final ProblemType MISSING_PROPERTY = problemType("MissingProperty", ProblemSeverity.ERROR);
	public static final ProblemType EXTRA_PROPERTY = problemType("ExtraProperty", ProblemSeverity.ERROR);

	public static final Set<ProblemType> PROPERTY_CONSTRAINT = ImmutableSet.of(
			MISSING_PROPERTY, EXTRA_PROPERTY
	);

	public static ProblemType problemType(final String typeName, ProblemSeverity defaultSeverity) {

		return new ProblemType() {
			@Override
			public String toString() {
				return typeName;
			}
			@Override
			public ProblemSeverity getDefaultSeverity() {
				return defaultSeverity;
			}
			@Override
			public String getCode() {
				return typeName;
			}
		};
	}

	public static ProblemType problemType(final String typeName) {
		return problemType(typeName, ProblemSeverity.ERROR);
	}

	public static ReconcileProblem syntaxProblem(String msg, int offset, int len) {
		return new ReconcileProblemImpl(SYNTAX_PROBLEM, msg, offset, len);
	}

	public static ReconcileProblem schemaProblem(String msg, Node node) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return new ReconcileProblemImpl(SCHEMA_PROBLEM, msg, start, end-start);
	}

	public static ReconcileProblem schemaProblem(String msg, DocumentRegion node) {
		return new ReconcileProblemImpl(SCHEMA_PROBLEM, msg, node.getStart(), node.getLength());
	}

	public static ReconcileProblem deprecatedProperty(Node node, YType bean, YTypedProperty property) {
		return problem(DEPRECATED_PROPERTY, "Property '"+property.getName()+"' of '"+bean+"' is Deprecated", node);
	}

	public static ReconcileProblem problem(ProblemType problemType, String msg, Node node) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return new ReconcileProblemImpl(problemType, msg, start, end-start);
	}
}
