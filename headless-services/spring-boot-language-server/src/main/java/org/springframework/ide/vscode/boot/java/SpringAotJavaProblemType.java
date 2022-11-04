package org.springframework.ide.vscode.boot.java;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.WARNING;

import org.springframework.ide.vscode.boot.common.SpringProblemCategories;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public enum SpringAotJavaProblemType implements ProblemType {
	
	JAVA_CONCRETE_BEAN_TYPE(WARNING, "Bean definition should have precise type", "Not precise bean defintion type"),
	
	JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT(WARNING, "'BeanPostProcessor' behaviour is ignored", "'BeanPostProcessor' behaviour is ignored in AOT"),
	
	JAVA_BEAN_NOT_REGISTERED_IN_AOT(WARNING, "Not registered as Bean", "Not registered as a Bean");

	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private SpringAotJavaProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	private SpringAotJavaProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
	}

	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public String getLabel() {
		if (label==null) {
			label = createDefaultLabel();
		}
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	private String createDefaultLabel() {
		String label = this.toString().substring(5).toLowerCase().replace('_', ' ');
		return Character.toUpperCase(label.charAt(0)) + label.substring(1);
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public ProblemCategory getCategory() {
		return SpringProblemCategories.SPRING_AOT;
	}

}
