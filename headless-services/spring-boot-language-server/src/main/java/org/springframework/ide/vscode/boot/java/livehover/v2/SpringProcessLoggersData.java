package org.springframework.ide.vscode.boot.java.livehover.v2;

/**
 * @author Udayani V
 */
public class SpringProcessLoggersData {

	private final ProcessType processType;
	private final String processName;
	private final String processID;
	private final Loggers loggers;

	public SpringProcessLoggersData(ProcessType processType, String processName, String processID,
			Loggers loggers) {
		super();
		this.processType = processType;
		this.processName = processName;
		this.processID = processID;
		this.loggers = loggers;

	}

	public ProcessType getProcessType() {
		return processType;
	}

	public String getProcessName() {
		return processName;
	}

	public String getProcessID() {
		return processID;
	}

	public Loggers getLoggers() {
		return loggers;
	}

}
