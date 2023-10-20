package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Udayani V
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Loggers {
	
	private List<String> levels;
    private Map<String, LoggerInfo> loggers;
    
	public List<String> getLevels() {
		return levels;
	}
	
	public Map<String, LoggerInfo> getLoggers() {
		return loggers;
	}
    
}
