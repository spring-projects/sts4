package org.springframework.ide.vscode.boot.yaml.reconcile;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.ERROR;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

public class ApplicationYamlProblems {
	
	public static enum Type implements ProblemType {
		
		YAML_SYNTAX_ERROR;

		Type() {
			this(ERROR);
		}

		Type(ProblemSeverity defaultSeverity) {
			this.severity = defaultSeverity;
		}
		
		private final ProblemSeverity severity;

		@Override
		public ProblemSeverity getDefaultSeverity() {
			return severity;
		}

		@Override
		public String getCode() {
			return name();
		}
		
	}
	
	public static final String YAML_SYNTAX_ERROR = "YAML_SYNTAX_ERROR";

	public static ReconcileProblem problem(Type type, String msg, int offset, int len) {
		return new ReconcileProblemImpl(type, msg, offset, len);
	}
}
