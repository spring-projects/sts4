package org.springframework.ide.vscode.commons.maven;

public interface IMavenConfiguration {
	
	public static IMavenConfiguration DEFAULT = new DefaultMavenCinfiguration();
	
	String getUserSettingsFile();
	
	String getGlobalSettingsFile();

}
