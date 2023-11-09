package org.springframework.ide.vscode.commons.protocol;

public record LiveProcessLoggersSummary(String processType, String processKey, String processName, String processID,
		String packageName, String effectiveLevel, String configuredLevel) {

}
