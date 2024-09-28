package org.springframework.ide.vscode.boot.java.copilot;

/**
 * The type of artifacts returned from an AI generated response to create code
 */
public enum ProjectArtifactType {

	APPLICATION_PROPERTIES,

	MAIN_CLASS,

	SOURCE_CODE,

	TEST_CODE,

	MAVEN_DEPENDENCIES,

	HTML

}