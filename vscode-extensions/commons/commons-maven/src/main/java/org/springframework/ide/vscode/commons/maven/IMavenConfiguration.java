package org.springframework.ide.vscode.commons.maven;

public interface IMavenConfiguration {
	
	public static IMavenConfiguration DEFAULT = new DefaultMavenConfiguration();
	
	String getUserSettingsFile();
	
	String getGlobalSettingsFile();

}
