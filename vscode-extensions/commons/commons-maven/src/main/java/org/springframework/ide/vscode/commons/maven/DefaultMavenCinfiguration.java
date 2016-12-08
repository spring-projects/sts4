package org.springframework.ide.vscode.commons.maven;

public class DefaultMavenCinfiguration implements IMavenConfiguration {
	
	private String userSettingsFile = null;
	
	private String globalSettingsFile = null;

	public void setUserSettingsFile(String userSettingsFile) {
		this.userSettingsFile = userSettingsFile;
	}

	public void setGlobalSettingsFile(String globalSettingsFile) {
		this.globalSettingsFile = globalSettingsFile;
	}

	@Override
	public String getUserSettingsFile() {
		return userSettingsFile;
	}

	@Override
	public String getGlobalSettingsFile() {
		return globalSettingsFile;
	}

}
