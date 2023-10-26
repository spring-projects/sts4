package org.springframework.ide.vscode.commons.protocol;

public class LiveProcessLoggersSummary {
	
	private String processType;
	private String processKey;
	private String processName;
	private String processID;
	private String packageName;
	private String effectiveLevel;
	private String configuredLevel;
	

	public String getProcessType() {
		return processType;
	}

	public void setProcessType(String processType) {
		this.processType = processType;
	}

	public String getProcessKey() {
		return processKey;
	}

	public void setProcessKey(String processKey) {
		this.processKey = processKey;
	}
	

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getEffectiveLevel() {
		return effectiveLevel;
	}

	public void setEffectiveLevel(String effectiveLevel) {
		this.effectiveLevel = effectiveLevel;
	}

	public String getConfiguredLevel() {
		return configuredLevel;
	}

	public void setConfiguredLevel(String configuredLevel) {
		this.configuredLevel = configuredLevel;
	}


}
