package org.springframework.ide.vscode.boot.java;

// Test fixture for agent-plugins/tools: a minimal problem-type enum with three codes.
public enum FakeProblemType implements ProblemType {

	GOOD_CODE(WARNING, "A code with a valid playbook", "Good code"),
	MISSING_CODE(WARNING, "A code without a playbook", "Missing code"),
	REF_TARGET(INFO, "A code with a malformed playbook", "Ref target");

	public static final int NOT_A_CODE = 30;

	private final String description;

	private FakeProblemType(Object severity, String description, String label) {
		this.description = description;
	}

	@Override
	public String getCode() {
		return name();
	}
}
