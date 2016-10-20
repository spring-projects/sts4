package org.springframework.ide.vscode.yaml.reconcile;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.languageserver.quickfix.ProblemFixer;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

public class SpringPropertyProblem extends ReconcileProblemImpl {

	private PropertyInfo property = null;
	private ProblemFixer fixer;
	private String propertyName;

	public SpringPropertyProblem(ProblemType type, String msg, int offset, int len) {
		super(type, msg, offset, len);
	}

	public static SpringPropertyProblem problem(SpringPropertiesProblemType type, String msg, int offset, int len) {
		return new SpringPropertyProblem(type, msg, offset, len);
	}

	public void setMetadata(PropertyInfo property) {
		this.property = property;
	}

	public void setProblemFixer(ProblemFixer fixer) {
		this.fixer = fixer;
	}

	public void setPropertyName(String name) {
		propertyName = name;
	}

}
