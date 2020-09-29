package org.springframework.ide.eclipse.boot.dash.console;

public class LogMessage {

	private LogType type;
	private String msg;

	public LogMessage(LogType type, String msg) {
		super();
		this.msg = msg;
		this.type = type;
	}

	public String getMessage() {
		return msg;
	}

	public LogType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "LogMessage [type=" + type + ", msg=" + msg + "]";
	}
}
