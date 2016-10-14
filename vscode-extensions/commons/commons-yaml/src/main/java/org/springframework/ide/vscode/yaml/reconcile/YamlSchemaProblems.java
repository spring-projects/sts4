package org.springframework.ide.vscode.yaml.reconcile;

import org.springframework.ide.vscode.commons.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.reconcile.ReconcileProblemImpl;
import org.yaml.snakeyaml.nodes.Node;

/**
 * Methods for creating reconciler problems for Schema based reconciler implementation.
 *
 * @author Kris De Volder
 */
public class YamlSchemaProblems {

	private static final ProblemType SCHEMA_PROBLEM = problemType("YamlSchemaProblem");
	private static final ProblemType SYNTAX_PROBLEM = problemType("YamlSyntaxProblem");

	private static ProblemType problemType(final String typeName) {
		return new ProblemType() {
			@Override
			public String toString() {
				return typeName;
			}
			@Override
			public ProblemSeverity getDefaultSeverity() {
				return ProblemSeverity.ERROR;
			}
			@Override
			public String getCode() {
				return typeName;
			}
		};
	}

	public static ReconcileProblem syntaxProblem(String msg, int offset, int len) {
		return new ReconcileProblemImpl(SYNTAX_PROBLEM, msg, offset, len);
	}

	public static ReconcileProblem schemaProblem(String msg, Node node) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return new ReconcileProblemImpl(SCHEMA_PROBLEM, msg, start, end-start);
	}
}
